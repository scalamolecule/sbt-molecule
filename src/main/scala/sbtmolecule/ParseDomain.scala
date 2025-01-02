package sbtmolecule

import java.nio.file.{Files, Paths}
import molecule.base.ast.*
import molecule.base.error.ModelError
import molecule.base.util.BaseHelpers
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.meta.*

object ParseDomain {
  def apply(filePath: String, scalaVersion: String = "3"): MetaDomain = {
    val bytes   = Files.readAllBytes(Paths.get(filePath))
    val pkgPath = new String(bytes, "UTF-8")
    new ParseDomain(filePath, pkgPath, scalaVersion).metaDomain
  }
}

class ParseDomain(filePath: String, pkgPath: String, scalaVersion: String) extends BaseHelpers {
  private val virtualFile = Input.VirtualFile(filePath, pkgPath)
  private val dialect     = scalaVersion match {
    case "3"   => dialects.Scala3(virtualFile)
    case "213" => dialects.Scala213(virtualFile)
    case "212" => dialects.Scala212(virtualFile)
  }

  private val tree = dialect.parse[Source].get

  private val reservedAttrNames = List(
    // Actions
    "query", "save", "insert", "update", "delete",

    // sorting
    "a1", "a2", "a3", "a4", "a5", "d1", "d2", "d3", "d4", "d5",

    // Expressions
    "apply", "not", "has", "hasNo", "add", "remove", "getV",

    // Generic attributes
    "id",

    // Model elements access
    "elements"
  )

  private var backRefs   = Map.empty[String, Seq[String]]
  private val valueAttrs = ListBuffer.empty[String]

  private def noMix() = throw ModelError(
    "Mixing prefixed and non-prefixed entities is not allowed."
  )
  private def unexpected(c: Tree, msg: String = ":") = throw ModelError(
    s"Unexpected Domain definition code in file $filePath$msg\n" + c
  )

  private val (pkg, afterPkg) = tree.children.collectFirst {
    case Pkg(pkg, afterPkg) => (pkg.toString, afterPkg)
  }.getOrElse(unexpected(tree, ". Couldn't find package definition in code:\n"))


  private val (domain, maxArity, body) = {
    afterPkg.collectFirst {
      case Defn.Object(_, Term.Name(domain),
      Template.internal.Latest(_,
      List(Init.internal.Latest(Type.Name("Domain"), _,
      List(Term.ArgClause(List(Lit.Int(maxArity)), _))
      )), _, body, _)) => (domain, maxArity, body)
    }.getOrElse(
      unexpected(tree, ". Couldn't find `object <YourDomain> extends Domain(<arity>) {...}` in code:\n")
    )
  }

  private def err(msg: String, entity: String = "", attr: String = "") = {
    val fullEntity = if (entity.isEmpty && attr.isEmpty) "" else
      s" for attribute $entity.$attr"
    throw ModelError(
      s"""Problem in data model $pkg.$domain$fullEntity:
         |$msg
         |""".stripMargin
    )
  }

  def metaDomain: MetaDomain = {
    val hasGroups = body.exists {
      case q"object $_ { ..$_ }" => true
      case _                     => false
    }
    val groups    = if (hasGroups) {
      body.map {
        case q"object $scope { ..$entities }" =>
          MetaGroup(scope.toString, getEntities(scope.toString + "_", entities))

        case q"trait $entity $template" => noMix()
      }
    } else {
      Seq(MetaGroup("", getEntities("", body)))
    }
    checkCircularMandatoryRefs(groups)
    val scopes1 = addBackRefs(groups)
    MetaDomain(pkg, domain, maxArity, scopes1)
  }

  private def checkCircularMandatoryRefs(groups: Seq[MetaGroup]): Unit = {
    val mappings: Map[String, Seq[(String, String)]] = groups
      .flatMap(_.ents
        .filter(_.attrs.exists(ref =>
          ref.ref.nonEmpty && ref.options.contains("mandatory")
        )))
      .map(metaEntity => metaEntity.ent -> metaEntity.attrs.collect {
        case ref if ref.ref.nonEmpty && ref.options.contains("mandatory") =>
          s"${metaEntity.ent}.${ref.attr}" -> ref.ref.get
      }).toMap

    def check(prevEntities: Seq[String], graph: Seq[String], entity: String): Unit = {
      mappings.get(entity).foreach { refs =>
        // Referenced entity has mandatory refs. Keep checking
        refs.foreach {
          case (refAttr, refEnt) if prevEntities.contains(refEnt) =>
            val last = if (graph.length == 1) refEnt else s"$refAttr --> $refEnt"
            err(
              s"""Circular mandatory references not allowed. Found:
                 |  ${graph.mkString(" --> ")} --> $last
                 |""".stripMargin
            )
          case (refAttr, refEnt)                                  =>
            check(prevEntities :+ refEnt, graph :+ refAttr, refEnt)
        }
      }
    }
    mappings.foreach {
      case (entity, refs) => refs.foreach {
        case (refAttr, refEnt) =>
          // Recursively check each mandatory ref. Can likely be optimized...
          check(Seq(entity), Seq(refAttr), refEnt)
      }
    }
  }

  private def fullEntity(scopePrefix: String, entity: String): String = {
    if (entity.contains(".")) entity.replace(".", "_") else scopePrefix + entity
  }

  private def addBackRefs(scopes: Seq[MetaGroup]): Seq[MetaGroup] = {
    scopes.map { scope =>
      val entities1 = scope.ents.map { entity =>
        entity.copy(backRefs = backRefs.getOrElse(entity.ent, Nil).distinct.sorted)
      }
      scope.copy(ents = entities1)
    }
  }

  private def getEntities(scopePrefix: String, entities: Seq[Stat]): Seq[MetaEntity] = {
    entities.map {
      case q"trait $entityTpe { ..$attrs }" => getEntity(scopePrefix, entityTpe, attrs)
      case q"object $o { ..$_ }"            => noMix()
      case other                            => unexpected(other)
    }
  }

  private def getEntity(scopePrefix: String, entityTpe: Name, attrs: Seq[Stat]): MetaEntity = {
    val entity = entityTpe.toString
    if (entity.head.isLower) {
      err(s"Please change entity trait name `$entity` to start with upper case letter.")
    }
    if (attrs.isEmpty) {
      err(s"Please define attribute(s) in entity $entity")
    }
    val metaAttrs      = getAttrs(scopePrefix, entity, attrs)
    val mandatoryAttrs = metaAttrs.collect {
      case a if a.ref.isEmpty && a.options.contains("mandatory") => a.attr
    }
    val mandatoryRefs  = metaAttrs.collect {
      case a if a.ref.nonEmpty && a.options.contains("mandatory") => a.attr -> a.ref.get
    }

    // Merge required groups with common attributes
    val reqGroups       = metaAttrs.collect {
      case a if a.requiredAttrs.nonEmpty => a.requiredAttrs
    }
    val reqGroupsMerged = reqGroups.map { group =>
      reqGroups.flatMap {
        case otherGroup if otherGroup.intersect(group).nonEmpty => (otherGroup ++ group).distinct.sorted
        case _                                                  => group
      }.distinct.sorted
    }.distinct

    val reqAttrs   = reqGroupsMerged.flatten
    val metaAttrs1 = MetaAttribute("id", CardOne, "ID") +: metaAttrs.map { a =>
      val attr = a.attr
      if (reqAttrs.contains(attr)) {
        val otherAttrs = reqGroupsMerged.collectFirst {
          case group if group.contains(attr) => group.filterNot(_ == attr)
        }
        a.copy(requiredAttrs = otherAttrs.get)
      } else a
    }

    MetaEntity(scopePrefix + entity, metaAttrs1, Nil, mandatoryAttrs, mandatoryRefs)
  }

  private def getAttrs(scopePrefix: String, entity: String, attrs: Seq[Stat]): Seq[MetaAttribute] = attrs.map {
    case q"val $attr = $defs" =>
      val a = attr.toString
      if (reservedAttrNames.contains(a)) {
        err(
          s"Please change attribute name $entity.$a to avoid colliding with reserved attribute names:\n  " +
            reservedAttrNames.mkString("\n  ")
        )
      }
      acc(scopePrefix, entity, defs, MetaAttribute(a, CardOne, ""))

    case other => unexpected(other)
  }

  private def saveDescr(scopePrefix: String, entity: String, prev: Tree, a: MetaAttribute, attr: String, s: String) = {
    if (s.isEmpty)
      err(s"Can't apply empty String as description option for attribute $attr")
    else if (s.contains("\""))
      err(s"Description option for attribute $attr can't contain quotation marks.")
    else
      acc(scopePrefix, entity, prev, a.copy(description = Some(s)))
  }

  @tailrec
  private def acc(pp: String, entity: String, t: Tree, a: MetaAttribute): MetaAttribute = {
    val attr = entity + "." + a.attr
    t match {

      // Options ................................................

      case q"$prev.index"          => acc(pp, entity, prev, a.copy(options = a.options :+ "index"))
      case q"$prev.noHistory"      => acc(pp, entity, prev, a.copy(options = a.options :+ "noHistory"))
      case q"$prev.uniqueIdentity" => acc(pp, entity, prev, a.copy(options = a.options :+ "uniqueIdentity"))
      case q"$prev.unique"         => acc(pp, entity, prev, a.copy(options = a.options :+ "unique"))
      case q"$prev.fulltext"       => acc(pp, entity, prev, a.copy(options = a.options :+ "fulltext"))
      case q"$prev.owner"          => acc(pp, entity, prev, a.copy(options = a.options :+ "owner"))
      case q"$prev.mandatory"      => acc(pp, entity, prev, a.copy(options = a.options :+ "mandatory"))

      case q"$prev.descr(${Lit.String(s)})" => saveDescr(pp, entity, prev, a, attr, s)
      case q"$prev.apply(${Lit.String(s)})" => saveDescr(pp, entity, prev, a, attr, s)

      case q"$prev.alias(${Lit.String(s)})" => s match {
        case r"([a-zA-Z0-9]+)$alias" =>
          if (reservedAttrNames.contains(alias)) {
            err(
              s"Alias `$alias` for attribute $attr can't be any of the reserved molecule attribute names:\n  " +
                reservedAttrNames.mkString("\n  ")
            )
          } else {
            acc(pp, entity, prev, a.copy(alias = Some(alias)))
          }
        case other                   =>
          err(s"Invalid alias for attribute $attr: " + other)
      }


      // Refs ................................................

      case q"one[$refEnt0]" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(card = CardOne, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)))

      case q"one[$refEnt0](${Lit.String(s)})" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(card = CardOne, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)), description = Some(s))

      case q"many[$refEnt0]" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(card = CardSet, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)))

      case q"many[$refEnt0](${Lit.String(s)})" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(card = CardSet, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)), description = Some(s))


      // Attributes ................................................

      case q"oneString"         => a.copy(card = CardOne, baseTpe = "String")
      case q"oneInt"            => a.copy(card = CardOne, baseTpe = "Int")
      case q"oneLong"           => a.copy(card = CardOne, baseTpe = "Long")
      case q"oneFloat"          => a.copy(card = CardOne, baseTpe = "Float")
      case q"oneDouble"         => a.copy(card = CardOne, baseTpe = "Double")
      case q"oneBoolean"        => a.copy(card = CardOne, baseTpe = "Boolean")
      case q"oneBigInt"         => a.copy(card = CardOne, baseTpe = "BigInt")
      case q"oneBigDecimal"     => a.copy(card = CardOne, baseTpe = "BigDecimal")
      case q"oneDate"           => a.copy(card = CardOne, baseTpe = "Date")
      case q"oneDuration"       => a.copy(card = CardOne, baseTpe = "Duration")
      case q"oneInstant"        => a.copy(card = CardOne, baseTpe = "Instant")
      case q"oneLocalDate"      => a.copy(card = CardOne, baseTpe = "LocalDate")
      case q"oneLocalTime"      => a.copy(card = CardOne, baseTpe = "LocalTime")
      case q"oneLocalDateTime"  => a.copy(card = CardOne, baseTpe = "LocalDateTime")
      case q"oneOffsetTime"     => a.copy(card = CardOne, baseTpe = "OffsetTime")
      case q"oneOffsetDateTime" => a.copy(card = CardOne, baseTpe = "OffsetDateTime")
      case q"oneZonedDateTime"  => a.copy(card = CardOne, baseTpe = "ZonedDateTime")
      case q"oneUUID"           => a.copy(card = CardOne, baseTpe = "UUID")
      case q"oneURI"            => a.copy(card = CardOne, baseTpe = "URI")
      case q"oneByte"           => a.copy(card = CardOne, baseTpe = "Byte")
      case q"oneShort"          => a.copy(card = CardOne, baseTpe = "Short")
      case q"oneChar"           => a.copy(card = CardOne, baseTpe = "Char")

      case q"oneBigDecimal($precision, $scale)" => a.copy(
        card = CardOne,
        baseTpe = "BigDecimal",
        options = a.options :+ s"$precision,$scale"
      )

      case q"oneString(${Lit.String(s)})"         => a.copy(card = CardOne, baseTpe = "String", description = Some(s))
      case q"oneInt(${Lit.String(s)})"            => a.copy(card = CardOne, baseTpe = "Int", description = Some(s))
      case q"oneLong(${Lit.String(s)})"           => a.copy(card = CardOne, baseTpe = "Long", description = Some(s))
      case q"oneFloat(${Lit.String(s)})"          => a.copy(card = CardOne, baseTpe = "Float", description = Some(s))
      case q"oneDouble(${Lit.String(s)})"         => a.copy(card = CardOne, baseTpe = "Double", description = Some(s))
      case q"oneBoolean(${Lit.String(s)})"        => a.copy(card = CardOne, baseTpe = "Boolean", description = Some(s))
      case q"oneBigInt(${Lit.String(s)})"         => a.copy(card = CardOne, baseTpe = "BigInt", description = Some(s))
      case q"oneBigDecimal(${Lit.String(s)})"     => a.copy(card = CardOne, baseTpe = "BigDecimal", description = Some(s))
      case q"oneDate(${Lit.String(s)})"           => a.copy(card = CardOne, baseTpe = "Date", description = Some(s))
      case q"oneDuration(${Lit.String(s)})"       => a.copy(card = CardOne, baseTpe = "Duration", description = Some(s))
      case q"oneInstant(${Lit.String(s)})"        => a.copy(card = CardOne, baseTpe = "Instant", description = Some(s))
      case q"oneLocalDate(${Lit.String(s)})"      => a.copy(card = CardOne, baseTpe = "LocalDate", description = Some(s))
      case q"oneLocalTime(${Lit.String(s)})"      => a.copy(card = CardOne, baseTpe = "LocalTime", description = Some(s))
      case q"oneLocalDateTime(${Lit.String(s)})"  => a.copy(card = CardOne, baseTpe = "LocalDateTime", description = Some(s))
      case q"oneOffsetTime(${Lit.String(s)})"     => a.copy(card = CardOne, baseTpe = "OffsetTime", description = Some(s))
      case q"oneOffsetDateTime(${Lit.String(s)})" => a.copy(card = CardOne, baseTpe = "OffsetDateTime", description = Some(s))
      case q"oneZonedDateTime(${Lit.String(s)})"  => a.copy(card = CardOne, baseTpe = "ZonedDateTime", description = Some(s))
      case q"oneUUID(${Lit.String(s)})"           => a.copy(card = CardOne, baseTpe = "UUID", description = Some(s))
      case q"oneURI(${Lit.String(s)})"            => a.copy(card = CardOne, baseTpe = "URI", description = Some(s))
      case q"oneByte(${Lit.String(s)})"           => a.copy(card = CardOne, baseTpe = "Byte", description = Some(s))
      case q"oneShort(${Lit.String(s)})"          => a.copy(card = CardOne, baseTpe = "Short", description = Some(s))
      case q"oneChar(${Lit.String(s)})"           => a.copy(card = CardOne, baseTpe = "Char", description = Some(s))


      case q"setString"         => a.copy(card = CardSet, baseTpe = "String")
      case q"setInt"            => a.copy(card = CardSet, baseTpe = "Int")
      case q"setLong"           => a.copy(card = CardSet, baseTpe = "Long")
      case q"setFloat"          => a.copy(card = CardSet, baseTpe = "Float")
      case q"setDouble"         => a.copy(card = CardSet, baseTpe = "Double")
      case q"setBoolean"        => a.copy(card = CardSet, baseTpe = "Boolean")
      case q"setBigInt"         => a.copy(card = CardSet, baseTpe = "BigInt")
      case q"setBigDecimal"     => a.copy(card = CardSet, baseTpe = "BigDecimal")
      case q"setDate"           => a.copy(card = CardSet, baseTpe = "Date")
      case q"setDuration"       => a.copy(card = CardSet, baseTpe = "Duration")
      case q"setInstant"        => a.copy(card = CardSet, baseTpe = "Instant")
      case q"setLocalDate"      => a.copy(card = CardSet, baseTpe = "LocalDate")
      case q"setLocalTime"      => a.copy(card = CardSet, baseTpe = "LocalTime")
      case q"setLocalDateTime"  => a.copy(card = CardSet, baseTpe = "LocalDateTime")
      case q"setOffsetTime"     => a.copy(card = CardSet, baseTpe = "OffsetTime")
      case q"setOffsetDateTime" => a.copy(card = CardSet, baseTpe = "OffsetDateTime")
      case q"setZonedDateTime"  => a.copy(card = CardSet, baseTpe = "ZonedDateTime")
      case q"setUUID"           => a.copy(card = CardSet, baseTpe = "UUID")
      case q"setURI"            => a.copy(card = CardSet, baseTpe = "URI")
      case q"setByte"           => a.copy(card = CardSet, baseTpe = "Byte")
      case q"setShort"          => a.copy(card = CardSet, baseTpe = "Short")
      case q"setChar"           => a.copy(card = CardSet, baseTpe = "Char")

      case q"setString(${Lit.String(s)})"         => a.copy(card = CardSet, baseTpe = "String", description = Some(s))
      case q"setInt(${Lit.String(s)})"            => a.copy(card = CardSet, baseTpe = "Int", description = Some(s))
      case q"setLong(${Lit.String(s)})"           => a.copy(card = CardSet, baseTpe = "Long", description = Some(s))
      case q"setFloat(${Lit.String(s)})"          => a.copy(card = CardSet, baseTpe = "Float", description = Some(s))
      case q"setDouble(${Lit.String(s)})"         => a.copy(card = CardSet, baseTpe = "Double", description = Some(s))
      case q"setBoolean(${Lit.String(s)})"        => a.copy(card = CardSet, baseTpe = "Boolean", description = Some(s))
      case q"setBigInt(${Lit.String(s)})"         => a.copy(card = CardSet, baseTpe = "BigInt", description = Some(s))
      case q"setBigDecimal(${Lit.String(s)})"     => a.copy(card = CardSet, baseTpe = "BigDecimal", description = Some(s))
      case q"setDate(${Lit.String(s)})"           => a.copy(card = CardSet, baseTpe = "Date", description = Some(s))
      case q"setDuration(${Lit.String(s)})"       => a.copy(card = CardSet, baseTpe = "Duration", description = Some(s))
      case q"setInstant(${Lit.String(s)})"        => a.copy(card = CardSet, baseTpe = "Instant", description = Some(s))
      case q"setLocalDate(${Lit.String(s)})"      => a.copy(card = CardSet, baseTpe = "LocalDate", description = Some(s))
      case q"setLocalTime(${Lit.String(s)})"      => a.copy(card = CardSet, baseTpe = "LocalTime", description = Some(s))
      case q"setLocalDateTime(${Lit.String(s)})"  => a.copy(card = CardSet, baseTpe = "LocalDateTime", description = Some(s))
      case q"setOffsetTime(${Lit.String(s)})"     => a.copy(card = CardSet, baseTpe = "OffsetTime", description = Some(s))
      case q"setOffsetDateTime(${Lit.String(s)})" => a.copy(card = CardSet, baseTpe = "OffsetDateTime", description = Some(s))
      case q"setZonedDateTime(${Lit.String(s)})"  => a.copy(card = CardSet, baseTpe = "ZonedDateTime", description = Some(s))
      case q"setUUID(${Lit.String(s)})"           => a.copy(card = CardSet, baseTpe = "UUID", description = Some(s))
      case q"setURI(${Lit.String(s)})"            => a.copy(card = CardSet, baseTpe = "URI", description = Some(s))
      case q"setByte(${Lit.String(s)})"           => a.copy(card = CardSet, baseTpe = "Byte", description = Some(s))
      case q"setShort(${Lit.String(s)})"          => a.copy(card = CardSet, baseTpe = "Short", description = Some(s))
      case q"setChar(${Lit.String(s)})"           => a.copy(card = CardSet, baseTpe = "Char", description = Some(s))


      case q"seqString"         => a.copy(card = CardSeq, baseTpe = "String")
      case q"seqInt"            => a.copy(card = CardSeq, baseTpe = "Int")
      case q"seqLong"           => a.copy(card = CardSeq, baseTpe = "Long")
      case q"seqFloat"          => a.copy(card = CardSeq, baseTpe = "Float")
      case q"seqDouble"         => a.copy(card = CardSeq, baseTpe = "Double")
      case q"seqBoolean"        => a.copy(card = CardSeq, baseTpe = "Boolean")
      case q"seqBigInt"         => a.copy(card = CardSeq, baseTpe = "BigInt")
      case q"seqBigDecimal"     => a.copy(card = CardSeq, baseTpe = "BigDecimal")
      case q"seqDate"           => a.copy(card = CardSeq, baseTpe = "Date")
      case q"seqDuration"       => a.copy(card = CardSeq, baseTpe = "Duration")
      case q"seqInstant"        => a.copy(card = CardSeq, baseTpe = "Instant")
      case q"seqLocalDate"      => a.copy(card = CardSeq, baseTpe = "LocalDate")
      case q"seqLocalTime"      => a.copy(card = CardSeq, baseTpe = "LocalTime")
      case q"seqLocalDateTime"  => a.copy(card = CardSeq, baseTpe = "LocalDateTime")
      case q"seqOffsetTime"     => a.copy(card = CardSeq, baseTpe = "OffsetTime")
      case q"seqOffsetDateTime" => a.copy(card = CardSeq, baseTpe = "OffsetDateTime")
      case q"seqZonedDateTime"  => a.copy(card = CardSeq, baseTpe = "ZonedDateTime")
      case q"seqUUID"           => a.copy(card = CardSeq, baseTpe = "UUID")
      case q"seqURI"            => a.copy(card = CardSeq, baseTpe = "URI")
      case q"arrayByte"         => a.copy(card = CardSeq, baseTpe = "Byte")
      case q"seqShort"          => a.copy(card = CardSeq, baseTpe = "Short")
      case q"seqChar"           => a.copy(card = CardSeq, baseTpe = "Char")

      case q"seqString(${Lit.String(s)})"         => a.copy(card = CardSeq, baseTpe = "String", description = Some(s))
      case q"seqInt(${Lit.String(s)})"            => a.copy(card = CardSeq, baseTpe = "Int", description = Some(s))
      case q"seqLong(${Lit.String(s)})"           => a.copy(card = CardSeq, baseTpe = "Long", description = Some(s))
      case q"seqFloat(${Lit.String(s)})"          => a.copy(card = CardSeq, baseTpe = "Float", description = Some(s))
      case q"seqDouble(${Lit.String(s)})"         => a.copy(card = CardSeq, baseTpe = "Double", description = Some(s))
      case q"seqBoolean(${Lit.String(s)})"        => a.copy(card = CardSeq, baseTpe = "Boolean", description = Some(s))
      case q"seqBigInt(${Lit.String(s)})"         => a.copy(card = CardSeq, baseTpe = "BigInt", description = Some(s))
      case q"seqBigDecimal(${Lit.String(s)})"     => a.copy(card = CardSeq, baseTpe = "BigDecimal", description = Some(s))
      case q"seqDate(${Lit.String(s)})"           => a.copy(card = CardSeq, baseTpe = "Date", description = Some(s))
      case q"seqDuration(${Lit.String(s)})"       => a.copy(card = CardSeq, baseTpe = "Duration", description = Some(s))
      case q"seqInstant(${Lit.String(s)})"        => a.copy(card = CardSeq, baseTpe = "Instant", description = Some(s))
      case q"seqLocalDate(${Lit.String(s)})"      => a.copy(card = CardSeq, baseTpe = "LocalDate", description = Some(s))
      case q"seqLocalTime(${Lit.String(s)})"      => a.copy(card = CardSeq, baseTpe = "LocalTime", description = Some(s))
      case q"seqLocalDateTime(${Lit.String(s)})"  => a.copy(card = CardSeq, baseTpe = "LocalDateTime", description = Some(s))
      case q"seqOffsetTime(${Lit.String(s)})"     => a.copy(card = CardSeq, baseTpe = "OffsetTime", description = Some(s))
      case q"seqOffsetDateTime(${Lit.String(s)})" => a.copy(card = CardSeq, baseTpe = "OffsetDateTime", description = Some(s))
      case q"seqZonedDateTime(${Lit.String(s)})"  => a.copy(card = CardSeq, baseTpe = "ZonedDateTime", description = Some(s))
      case q"seqUUID(${Lit.String(s)})"           => a.copy(card = CardSeq, baseTpe = "UUID", description = Some(s))
      case q"seqURI(${Lit.String(s)})"            => a.copy(card = CardSeq, baseTpe = "URI", description = Some(s))
      case q"arrayByte(${Lit.String(s)})"         => a.copy(card = CardSeq, baseTpe = "Byte", description = Some(s))
      case q"seqShort(${Lit.String(s)})"          => a.copy(card = CardSeq, baseTpe = "Short", description = Some(s))
      case q"seqChar(${Lit.String(s)})"           => a.copy(card = CardSeq, baseTpe = "Char", description = Some(s))


      case q"mapString"         => a.copy(card = CardMap, baseTpe = "String")
      case q"mapInt"            => a.copy(card = CardMap, baseTpe = "Int")
      case q"mapLong"           => a.copy(card = CardMap, baseTpe = "Long")
      case q"mapFloat"          => a.copy(card = CardMap, baseTpe = "Float")
      case q"mapDouble"         => a.copy(card = CardMap, baseTpe = "Double")
      case q"mapBoolean"        => a.copy(card = CardMap, baseTpe = "Boolean")
      case q"mapBigInt"         => a.copy(card = CardMap, baseTpe = "BigInt")
      case q"mapBigDecimal"     => a.copy(card = CardMap, baseTpe = "BigDecimal")
      case q"mapDate"           => a.copy(card = CardMap, baseTpe = "Date")
      case q"mapDuration"       => a.copy(card = CardMap, baseTpe = "Duration")
      case q"mapInstant"        => a.copy(card = CardMap, baseTpe = "Instant")
      case q"mapLocalDate"      => a.copy(card = CardMap, baseTpe = "LocalDate")
      case q"mapLocalTime"      => a.copy(card = CardMap, baseTpe = "LocalTime")
      case q"mapLocalDateTime"  => a.copy(card = CardMap, baseTpe = "LocalDateTime")
      case q"mapOffsetTime"     => a.copy(card = CardMap, baseTpe = "OffsetTime")
      case q"mapOffsetDateTime" => a.copy(card = CardMap, baseTpe = "OffsetDateTime")
      case q"mapZonedDateTime"  => a.copy(card = CardMap, baseTpe = "ZonedDateTime")
      case q"mapUUID"           => a.copy(card = CardMap, baseTpe = "UUID")
      case q"mapURI"            => a.copy(card = CardMap, baseTpe = "URI")
      case q"mapByte"           => a.copy(card = CardMap, baseTpe = "Byte")
      case q"mapShort"          => a.copy(card = CardMap, baseTpe = "Short")
      case q"mapChar"           => a.copy(card = CardMap, baseTpe = "Char")

      case q"mapString(${Lit.String(s)})"         => a.copy(card = CardMap, baseTpe = "String", description = Some(s))
      case q"mapInt(${Lit.String(s)})"            => a.copy(card = CardMap, baseTpe = "Int", description = Some(s))
      case q"mapLong(${Lit.String(s)})"           => a.copy(card = CardMap, baseTpe = "Long", description = Some(s))
      case q"mapFloat(${Lit.String(s)})"          => a.copy(card = CardMap, baseTpe = "Float", description = Some(s))
      case q"mapDouble(${Lit.String(s)})"         => a.copy(card = CardMap, baseTpe = "Double", description = Some(s))
      case q"mapBoolean(${Lit.String(s)})"        => a.copy(card = CardMap, baseTpe = "Boolean", description = Some(s))
      case q"mapBigInt(${Lit.String(s)})"         => a.copy(card = CardMap, baseTpe = "BigInt", description = Some(s))
      case q"mapBigDecimal(${Lit.String(s)})"     => a.copy(card = CardMap, baseTpe = "BigDecimal", description = Some(s))
      case q"mapDate(${Lit.String(s)})"           => a.copy(card = CardMap, baseTpe = "Date", description = Some(s))
      case q"mapDuration(${Lit.String(s)})"       => a.copy(card = CardMap, baseTpe = "Duration", description = Some(s))
      case q"mapInstant(${Lit.String(s)})"        => a.copy(card = CardMap, baseTpe = "Instant", description = Some(s))
      case q"mapLocalDate(${Lit.String(s)})"      => a.copy(card = CardMap, baseTpe = "LocalDate", description = Some(s))
      case q"mapLocalTime(${Lit.String(s)})"      => a.copy(card = CardMap, baseTpe = "LocalTime", description = Some(s))
      case q"mapLocalDateTime(${Lit.String(s)})"  => a.copy(card = CardMap, baseTpe = "LocalDateTime", description = Some(s))
      case q"mapOffsetTime(${Lit.String(s)})"     => a.copy(card = CardMap, baseTpe = "OffsetTime", description = Some(s))
      case q"mapOffsetDateTime(${Lit.String(s)})" => a.copy(card = CardMap, baseTpe = "OffsetDateTime", description = Some(s))
      case q"mapZonedDateTime(${Lit.String(s)})"  => a.copy(card = CardMap, baseTpe = "ZonedDateTime", description = Some(s))
      case q"mapUUID(${Lit.String(s)})"           => a.copy(card = CardMap, baseTpe = "UUID", description = Some(s))
      case q"mapURI(${Lit.String(s)})"            => a.copy(card = CardMap, baseTpe = "URI", description = Some(s))
      case q"mapByte(${Lit.String(s)})"           => a.copy(card = CardMap, baseTpe = "Byte", description = Some(s))
      case q"mapShort(${Lit.String(s)})"          => a.copy(card = CardMap, baseTpe = "Short", description = Some(s))
      case q"mapChar(${Lit.String(s)})"           => a.copy(card = CardMap, baseTpe = "Char", description = Some(s))


      // Validations ................................................

      case q"$prev.validate { ..case $cases }" =>
        handleValidationCases(prev, pp, entity, a, cases, attr)

      case q"$prev.validate($test)" =>
        test match {
          case q"{ ..case $cases }: PartialFunction[$_, $_]" =>
            handleValidationCases(prev, pp, entity, a, cases, attr)

          case _ =>
            oneValidationCall(entity, a)
            val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
            val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attr +: valueAttrs1).distinct.sorted
            val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
            val validations1 = Seq(indent(test.toString()) -> "")
            acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))
        }

      case q"$prev.validate($test, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attr +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = Seq(indent(test.toString()) -> error)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))

      case q"$prev.validate($test, ${Term.Select(Lit.String(multilineMsg), Term.Name("stripMargin"))})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attr +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = Seq(indent(test.toString()) -> multilineMsg)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))

      case q"$prev.validate($test, ${Term.Interpolate(Term.Name("s"), _, _)})" =>
        err(
          s"String interpolation not allowed for validation error messages of `$attr`. " +
            s"Please remove the s prefix."
        )

      case q"$prev.email" =>
        oneValidationCall(entity, a)
        val test  = "(s: String) => emailRegex.findFirstMatchIn(s).isDefined"
        val error = s"""`$$v` is not a valid email"""
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.email(${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = "(s: String) => emailRegex.findFirstMatchIn(s).isDefined"
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.regex(${Lit.String(regex)})" =>
        oneValidationCall(entity, a)
        val test  = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        val error = s"""\"$$v\" doesn't match regex pattern: ${regex.replace("$", "$$")}"""
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.regex(${Lit.String(regex)}, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.enums(Seq(..$vs), ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""v => Seq$vs.contains(v)"""
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.enums(..$vs)" =>
        oneValidationCall(entity, a)
        val test  = s"""v => Seq$vs.contains(v)"""
        val error = s"""Value `$$v` is not one of the allowed values in Seq$vs"""
        acc(pp, entity, prev, a.copy(validations = Seq(test -> error)))

      case q"$prev.require(..$otherAttrs)" =>
        val reqAttrs1 = a.attr +: otherAttrs.map(_.toString)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1))

      case q"$prev.value" => err(
        s"Calling `value` on attribute `$attr` is only allowed in validations code of other attributes."
      )

      case other =>
        println("UNEXPECTED TREE STRUCTURE:\n" + other.structure)
        unexpected(other)
    }
  }

  private def addBackRef(scopePrefix: String, backRefNs: String, entity: String): Unit = {
    val fullN              = fullEntity(scopePrefix, entity)
    val backRefNs1         = fullEntity(scopePrefix, backRefNs)
    val curBackRefEntities = backRefs.getOrElse(fullN, Nil)
    backRefs = backRefs + (fullN -> (curBackRefEntities :+ backRefNs1))
  }

  private def handleValidationCases(
    prev: Tree,
    scopePrefix: String,
    entity: String,
    a: MetaAttribute,
    cases: Seq[Case],
    attr: String
  ) = {
    oneValidationCall(entity, a)
    val (valueAttrs, validations) = cases.map {
      case Case(v, Some(test), Lit.String(error)) =>
        val valueAttrs = extractValueAttrs(entity, a, q"$test")
        val validation = (indent(s"$v => $test"), error)
        (valueAttrs, validation)

      case Case(v, Some(test), Term.Select(Lit.String(multilineMsg), Term.Name("stripMargin"))) =>
        val valueAttrs = extractValueAttrs(entity, a, q"$test")
        val validation = (indent(s"$v => $test"), multilineMsg)
        (valueAttrs, validation)

      case Case(v, Some(test), Term.Interpolate(Term.Name("s"), _, _)) =>
        err(
          s"String interpolation not allowed for validation error messages of `$attr`. " +
            s"Please remove the s prefix."
        )

      case Case(v, None, Lit.String(error)) =>
        err(s"""Please provide if-expression: case $v if <test..> = "$error"""", entity, a.attr)

      case other => err("Unexpected validation case: " + other, entity, a.attr)
    }.unzip

    val valueAttrs1 = valueAttrs.flatten.distinct.sorted
    val valueAttrs2 = if (valueAttrs1.isEmpty) Nil else (a.attr +: valueAttrs1).distinct.sorted
    val reqAttrs1   = a.requiredAttrs ++ valueAttrs2
    val attr1       = a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations)
    acc(scopePrefix, entity, prev, attr1)
  }

  private def oneValidationCall(entity: String, a: MetaAttribute) = if (a.validations.nonEmpty) {
    throw ModelError(
      s"Please use `validate { ..<pattern matches> }` for multiple validations of attribute `$entity.${a.attr}`"
    )
  }

  private def indent(code0: String): String = {
    val code = code0.replaceAll("\\.value", "")
    if (code.contains('\n')) {
      val testIndented = {
        val lines  = code.split('\n').toList
        val indent = lines.map(_.takeWhile(_ == ' ').length).filterNot(_ == 0).min
        lines.map(_.replaceFirst(s"\\s{$indent}", "")).mkString("\n")
      }
      testIndented
    } else {
      code
    }
  }

  // Recursively traverse test code trees to extract attribute names
  private lazy val traverser = (entity: String) => new Traverser {
    override def apply(tree: Tree): Unit = tree match {
      case Term.Select(Term.Name(attr), Term.Name("value")) => valueAttrs += attr
      case node                                             => super.apply(node)
    }
  }

  private def extractValueAttrs(entity: String, a: MetaAttribute, test: Stat): List[String] = {
    valueAttrs.clear()
    traverser(entity)(test)
    valueAttrs.result().distinct.sorted
  }

}

