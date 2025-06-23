package sbtmolecule.db

import molecule.base.metaModel.*
import molecule.base.util.BaseHelpers
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.meta.*

case class ParseDomainStructure(
  filePath: String,
  pkg: String,
  domain: String,
  maxArity: Int,
  body: List[Stat]
) extends BaseHelpers {

  private val reservedAttrNames = List(
    // Actions
    "query", "save", "insert", "update", "delete",

    // sorting
    "a1", "a2", "a3", "a4", "a5", "d1", "d2", "d3", "d4", "d5",

    // Expressions
    "apply", "not", "has", "hasNo", "add", "remove", "getV",

    // Generic attributes
    "id",

    // Data model elements
    "elements"
  )

  private var backRefs   = Map.empty[String, List[String]]
  private val valueAttrs = ListBuffer.empty[String]

  private def noMix() = throw new Exception(
    "Mixing prefixed and non-prefixed entities is not allowed."
  )
  private def unexpected(c: Tree, msg: String = ":") = throw new Exception(
    s"Unexpected Domain definition code in file $filePath$msg\n" + c
  )


  private def err(msg: String, entity: String = "", attr: String = "") = {
    val fullEntity = if (entity.isEmpty && attr.isEmpty) "" else
      s" for attribute $entity.$attr"
    throw new Exception(
      s"""Problem in data model $pkg.$domain$fullEntity:
         |$msg
         |""".stripMargin
    )
  }

  // Collect enum definitions on all levels
  var enums = Map.empty[String, List[String]]

  def getMetaDomain: MetaDomain = {
    val hasSegements = body.exists {
      case q"object $_ { ..$_ }" => true
      case _                     => false
    }
    val segments     = if (hasSegements) {
      body.flatMap {
        case q"object $segment { ..$entities }" =>
          Some(MetaSegment(segment.toString, getEntities(segment.toString + "_", entities.toList)))

        case Defn.Enum.After_4_6_0(_, name, _, _, templ) => parseEnum[MetaSegment](name, templ)
        case q"trait $entity $template"                  => noMix()
      }
    } else {
      List(MetaSegment("", getEntities("", body)))
    }
    checkCircularMandatoryRefs(segments)
    val segments1 = addBackRefs(segments)
    val segments2 = if (enums.isEmpty) segments1 else {
      // Add segment with all enum definitions
      segments1 :+ MetaSegment("_enums",
        enums.toList.map {
          case (enumName, enumCases) =>
            MetaEntity(enumName, enumCases.map(c => MetaAttribute(c, CardOne, "String")))
        }
      )
    }
    MetaDomain(pkg, domain, maxArity, segments2)
  }

  private def checkCircularMandatoryRefs(segments: List[MetaSegment]): Unit = {
    val mappings: Map[String, List[(String, String)]] = segments
      .flatMap(_.entities
        .filter(_.attributes.exists(ref =>
          ref.ref.nonEmpty && ref.options.contains("mandatory")
        )))
      .map(metaEntity => metaEntity.entity -> metaEntity.attributes.collect {
        case ref if ref.ref.nonEmpty && ref.options.contains("mandatory") =>
          s"${metaEntity.entity}.${ref.attribute}" -> ref.ref.get
      }).toMap

    def check(prevEntities: List[String], graph: List[String], entity: String): Unit = {
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
          check(List(entity), List(refAttr), refEnt)
      }
    }
  }

  private def fullEntity(segmentPrefix: String, entity: String): String = {
    if (entity.contains(".")) entity.replace(".", "_") else segmentPrefix + entity
  }

  private def addBackRefs(segments: List[MetaSegment]): List[MetaSegment] = {
    segments.map { segment =>
      val entities1 = segment.entities.map { entity =>
        entity.copy(backRefs = backRefs.getOrElse(entity.entity, Nil).distinct.sorted)
      }
      segment.copy(entities = entities1)
    }
  }

  private def getEntities(segmentPrefix: String, entities: List[Stat]): List[MetaEntity] = {
    entities.flatMap {
      case q"trait $entityTpe { ..$attrs }" =>
        Some(getEntity(segmentPrefix, entityTpe, attrs.toList))

      case Defn.Enum.After_4_6_0(_, name, _, _, templ) => parseEnum[MetaEntity](name, templ)
      case q"object $o { ..$_ }"                       => noMix()
      case other                                       => unexpected(other)
    }
  }

  private def getEntity(segmentPrefix: String, entityTpe: Name, attrs: List[Stat]): MetaEntity = {
    val entity = entityTpe.toString
    if (entity.head.isLower) {
      err(s"Please change entity trait name `$entity` to start with upper case letter.")
    }
    if (attrs.isEmpty) {
      err(s"Please define attribute(s) in entity $entity")
    }
    val metaAttrs      = getAttrs(segmentPrefix, entity, attrs)
    val mandatoryAttrs = metaAttrs.collect {
      case a if a.ref.isEmpty && a.options.contains("mandatory") => a.attribute
    }
    val mandatoryRefs  = metaAttrs.collect {
      case a if a.ref.nonEmpty && a.options.contains("mandatory") => a.attribute -> a.ref.get
    }

    // Merge required groups of attributes with common attributes
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
      val attr = a.attribute
      if (reqAttrs.contains(attr)) {
        val otherAttrs = reqGroupsMerged.collectFirst {
          case group if group.contains(attr) => group.filterNot(_ == attr)
        }
        a.copy(requiredAttrs = otherAttrs.get)
      } else a
    }

    MetaEntity(segmentPrefix + entity, metaAttrs1, Nil, mandatoryAttrs, mandatoryRefs)
  }

  private def getAttrs(segmentPrefix: String, entity: String, attrs: List[Stat]): List[MetaAttribute] = {
    attrs.flatMap {
      case q"val $attr = $defs" =>
        val a = attr.toString
        if (reservedAttrNames.contains(a)) {
          err(
            s"Please change attribute name $entity.$a to avoid colliding with reserved attribute names:\n  " +
              reservedAttrNames.mkString("\n  ")
          )
        }
        Some(acc(segmentPrefix, entity, defs, MetaAttribute(a, CardOne, "")))

      case Defn.Enum.After_4_6_0(_, name, _, _, templ) => parseEnum[MetaAttribute](name, templ)
      case other                                       => unexpected(other)
    }
  }

  def parseEnum[T](name: Type.Name, templ: Template): Option[T] = {
    val enumName = name.value
    if (enums.keys.toList.contains(enumName))
      throw new Exception(
        s"Can't define enum $enumName because it's already defined in the same file."
      )
    val enumCases = templ.stats.flatMap {
      case Defn.RepeatedEnumCase(Nil, caseNames) => caseNames.map(_.value)
      case unexpected                            =>
        throw new Exception(s"Unexpected enum case: $unexpected\n${unexpected.structure}")
    }
    enums = enums + (enumName -> enumCases)
    None
  }

  private def saveDescr(segmentPrefix: String, entity: String, prev: Tree, a: MetaAttribute, attr: String, s: String) = {
    if (s.isEmpty)
      err(s"Can't apply empty String as description option for attribute $attr")
    else if (s.contains("\""))
      err(s"Description option for attribute $attr can't contain quotation marks.")
    else
      acc(segmentPrefix, entity, prev, a.copy(description = Some(s)))
  }

  @tailrec
  private def acc(pp: String, entity: String, t: Tree, a: MetaAttribute): MetaAttribute = {
    val attr = entity + "." + a.attribute
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
        a.copy(cardinality = CardOne, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)))

      case q"one[$refEnt0](${Lit.String(s)})" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(cardinality = CardOne, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)), description = Some(s))

      case q"many[$refEnt0]" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(cardinality = CardSet, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)))

      case q"many[$refEnt0](${Lit.String(s)})" =>
        val refEnt = refEnt0.toString
        addBackRef(pp, entity, refEnt)
        a.copy(cardinality = CardSet, baseTpe = "ID", ref = Some(fullEntity(pp, refEnt)), description = Some(s))


      // Enums ................................................

      case q"oneEnum[$enumTpe]" => a.copy(cardinality = CardOne, baseTpe = "String", enumTpe = Some(enumTpe.toString))
      case q"setEnum[$enumTpe]" => a.copy(cardinality = CardSet, baseTpe = "String", enumTpe = Some(enumTpe.toString))
      case q"seqEnum[$enumTpe]" => a.copy(cardinality = CardSeq, baseTpe = "String", enumTpe = Some(enumTpe.toString))

      case q"oneEnum[$enumTpe](${Lit.String(s)})" => a.copy(cardinality = CardOne, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))
      case q"setEnum[$enumTpe](${Lit.String(s)})" => a.copy(cardinality = CardSet, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))
      case q"seqEnum[$enumTpe](${Lit.String(s)})" => a.copy(cardinality = CardSeq, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))


      // Attributes ................................................

      case q"oneString"         => a.copy(cardinality = CardOne, baseTpe = "String")
      case q"oneInt"            => a.copy(cardinality = CardOne, baseTpe = "Int")
      case q"oneLong"           => a.copy(cardinality = CardOne, baseTpe = "Long")
      case q"oneFloat"          => a.copy(cardinality = CardOne, baseTpe = "Float")
      case q"oneDouble"         => a.copy(cardinality = CardOne, baseTpe = "Double")
      case q"oneBoolean"        => a.copy(cardinality = CardOne, baseTpe = "Boolean")
      case q"oneBigInt"         => a.copy(cardinality = CardOne, baseTpe = "BigInt")
      case q"oneBigDecimal"     => a.copy(cardinality = CardOne, baseTpe = "BigDecimal")
      case q"oneDate"           => a.copy(cardinality = CardOne, baseTpe = "Date")
      case q"oneDuration"       => a.copy(cardinality = CardOne, baseTpe = "Duration")
      case q"oneInstant"        => a.copy(cardinality = CardOne, baseTpe = "Instant")
      case q"oneLocalDate"      => a.copy(cardinality = CardOne, baseTpe = "LocalDate")
      case q"oneLocalTime"      => a.copy(cardinality = CardOne, baseTpe = "LocalTime")
      case q"oneLocalDateTime"  => a.copy(cardinality = CardOne, baseTpe = "LocalDateTime")
      case q"oneOffsetTime"     => a.copy(cardinality = CardOne, baseTpe = "OffsetTime")
      case q"oneOffsetDateTime" => a.copy(cardinality = CardOne, baseTpe = "OffsetDateTime")
      case q"oneZonedDateTime"  => a.copy(cardinality = CardOne, baseTpe = "ZonedDateTime")
      case q"oneUUID"           => a.copy(cardinality = CardOne, baseTpe = "UUID")
      case q"oneURI"            => a.copy(cardinality = CardOne, baseTpe = "URI")
      case q"oneByte"           => a.copy(cardinality = CardOne, baseTpe = "Byte")
      case q"oneShort"          => a.copy(cardinality = CardOne, baseTpe = "Short")
      case q"oneChar"           => a.copy(cardinality = CardOne, baseTpe = "Char")

      case q"oneBigDecimal($precision, $scale)" => a.copy(
        cardinality = CardOne,
        baseTpe = "BigDecimal",
        options = a.options :+ s"$precision,$scale"
      )

      case q"oneString(${Lit.String(s)})"         => a.copy(cardinality = CardOne, baseTpe = "String", description = Some(s))
      case q"oneInt(${Lit.String(s)})"            => a.copy(cardinality = CardOne, baseTpe = "Int", description = Some(s))
      case q"oneLong(${Lit.String(s)})"           => a.copy(cardinality = CardOne, baseTpe = "Long", description = Some(s))
      case q"oneFloat(${Lit.String(s)})"          => a.copy(cardinality = CardOne, baseTpe = "Float", description = Some(s))
      case q"oneDouble(${Lit.String(s)})"         => a.copy(cardinality = CardOne, baseTpe = "Double", description = Some(s))
      case q"oneBoolean(${Lit.String(s)})"        => a.copy(cardinality = CardOne, baseTpe = "Boolean", description = Some(s))
      case q"oneBigInt(${Lit.String(s)})"         => a.copy(cardinality = CardOne, baseTpe = "BigInt", description = Some(s))
      case q"oneBigDecimal(${Lit.String(s)})"     => a.copy(cardinality = CardOne, baseTpe = "BigDecimal", description = Some(s))
      case q"oneDate(${Lit.String(s)})"           => a.copy(cardinality = CardOne, baseTpe = "Date", description = Some(s))
      case q"oneDuration(${Lit.String(s)})"       => a.copy(cardinality = CardOne, baseTpe = "Duration", description = Some(s))
      case q"oneInstant(${Lit.String(s)})"        => a.copy(cardinality = CardOne, baseTpe = "Instant", description = Some(s))
      case q"oneLocalDate(${Lit.String(s)})"      => a.copy(cardinality = CardOne, baseTpe = "LocalDate", description = Some(s))
      case q"oneLocalTime(${Lit.String(s)})"      => a.copy(cardinality = CardOne, baseTpe = "LocalTime", description = Some(s))
      case q"oneLocalDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardOne, baseTpe = "LocalDateTime", description = Some(s))
      case q"oneOffsetTime(${Lit.String(s)})"     => a.copy(cardinality = CardOne, baseTpe = "OffsetTime", description = Some(s))
      case q"oneOffsetDateTime(${Lit.String(s)})" => a.copy(cardinality = CardOne, baseTpe = "OffsetDateTime", description = Some(s))
      case q"oneZonedDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardOne, baseTpe = "ZonedDateTime", description = Some(s))
      case q"oneUUID(${Lit.String(s)})"           => a.copy(cardinality = CardOne, baseTpe = "UUID", description = Some(s))
      case q"oneURI(${Lit.String(s)})"            => a.copy(cardinality = CardOne, baseTpe = "URI", description = Some(s))
      case q"oneByte(${Lit.String(s)})"           => a.copy(cardinality = CardOne, baseTpe = "Byte", description = Some(s))
      case q"oneShort(${Lit.String(s)})"          => a.copy(cardinality = CardOne, baseTpe = "Short", description = Some(s))
      case q"oneChar(${Lit.String(s)})"           => a.copy(cardinality = CardOne, baseTpe = "Char", description = Some(s))


      case q"setString"         => a.copy(cardinality = CardSet, baseTpe = "String")
      case q"setInt"            => a.copy(cardinality = CardSet, baseTpe = "Int")
      case q"setLong"           => a.copy(cardinality = CardSet, baseTpe = "Long")
      case q"setFloat"          => a.copy(cardinality = CardSet, baseTpe = "Float")
      case q"setDouble"         => a.copy(cardinality = CardSet, baseTpe = "Double")
      case q"setBoolean"        => a.copy(cardinality = CardSet, baseTpe = "Boolean")
      case q"setBigInt"         => a.copy(cardinality = CardSet, baseTpe = "BigInt")
      case q"setBigDecimal"     => a.copy(cardinality = CardSet, baseTpe = "BigDecimal")
      case q"setDate"           => a.copy(cardinality = CardSet, baseTpe = "Date")
      case q"setDuration"       => a.copy(cardinality = CardSet, baseTpe = "Duration")
      case q"setInstant"        => a.copy(cardinality = CardSet, baseTpe = "Instant")
      case q"setLocalDate"      => a.copy(cardinality = CardSet, baseTpe = "LocalDate")
      case q"setLocalTime"      => a.copy(cardinality = CardSet, baseTpe = "LocalTime")
      case q"setLocalDateTime"  => a.copy(cardinality = CardSet, baseTpe = "LocalDateTime")
      case q"setOffsetTime"     => a.copy(cardinality = CardSet, baseTpe = "OffsetTime")
      case q"setOffsetDateTime" => a.copy(cardinality = CardSet, baseTpe = "OffsetDateTime")
      case q"setZonedDateTime"  => a.copy(cardinality = CardSet, baseTpe = "ZonedDateTime")
      case q"setUUID"           => a.copy(cardinality = CardSet, baseTpe = "UUID")
      case q"setURI"            => a.copy(cardinality = CardSet, baseTpe = "URI")
      case q"setByte"           => a.copy(cardinality = CardSet, baseTpe = "Byte")
      case q"setShort"          => a.copy(cardinality = CardSet, baseTpe = "Short")
      case q"setChar"           => a.copy(cardinality = CardSet, baseTpe = "Char")

      case q"setString(${Lit.String(s)})"         => a.copy(cardinality = CardSet, baseTpe = "String", description = Some(s))
      case q"setInt(${Lit.String(s)})"            => a.copy(cardinality = CardSet, baseTpe = "Int", description = Some(s))
      case q"setLong(${Lit.String(s)})"           => a.copy(cardinality = CardSet, baseTpe = "Long", description = Some(s))
      case q"setFloat(${Lit.String(s)})"          => a.copy(cardinality = CardSet, baseTpe = "Float", description = Some(s))
      case q"setDouble(${Lit.String(s)})"         => a.copy(cardinality = CardSet, baseTpe = "Double", description = Some(s))
      case q"setBoolean(${Lit.String(s)})"        => a.copy(cardinality = CardSet, baseTpe = "Boolean", description = Some(s))
      case q"setBigInt(${Lit.String(s)})"         => a.copy(cardinality = CardSet, baseTpe = "BigInt", description = Some(s))
      case q"setBigDecimal(${Lit.String(s)})"     => a.copy(cardinality = CardSet, baseTpe = "BigDecimal", description = Some(s))
      case q"setDate(${Lit.String(s)})"           => a.copy(cardinality = CardSet, baseTpe = "Date", description = Some(s))
      case q"setDuration(${Lit.String(s)})"       => a.copy(cardinality = CardSet, baseTpe = "Duration", description = Some(s))
      case q"setInstant(${Lit.String(s)})"        => a.copy(cardinality = CardSet, baseTpe = "Instant", description = Some(s))
      case q"setLocalDate(${Lit.String(s)})"      => a.copy(cardinality = CardSet, baseTpe = "LocalDate", description = Some(s))
      case q"setLocalTime(${Lit.String(s)})"      => a.copy(cardinality = CardSet, baseTpe = "LocalTime", description = Some(s))
      case q"setLocalDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardSet, baseTpe = "LocalDateTime", description = Some(s))
      case q"setOffsetTime(${Lit.String(s)})"     => a.copy(cardinality = CardSet, baseTpe = "OffsetTime", description = Some(s))
      case q"setOffsetDateTime(${Lit.String(s)})" => a.copy(cardinality = CardSet, baseTpe = "OffsetDateTime", description = Some(s))
      case q"setZonedDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardSet, baseTpe = "ZonedDateTime", description = Some(s))
      case q"setUUID(${Lit.String(s)})"           => a.copy(cardinality = CardSet, baseTpe = "UUID", description = Some(s))
      case q"setURI(${Lit.String(s)})"            => a.copy(cardinality = CardSet, baseTpe = "URI", description = Some(s))
      case q"setByte(${Lit.String(s)})"           => a.copy(cardinality = CardSet, baseTpe = "Byte", description = Some(s))
      case q"setShort(${Lit.String(s)})"          => a.copy(cardinality = CardSet, baseTpe = "Short", description = Some(s))
      case q"setChar(${Lit.String(s)})"           => a.copy(cardinality = CardSet, baseTpe = "Char", description = Some(s))


      case q"seqString"         => a.copy(cardinality = CardSeq, baseTpe = "String")
      case q"seqInt"            => a.copy(cardinality = CardSeq, baseTpe = "Int")
      case q"seqLong"           => a.copy(cardinality = CardSeq, baseTpe = "Long")
      case q"seqFloat"          => a.copy(cardinality = CardSeq, baseTpe = "Float")
      case q"seqDouble"         => a.copy(cardinality = CardSeq, baseTpe = "Double")
      case q"seqBoolean"        => a.copy(cardinality = CardSeq, baseTpe = "Boolean")
      case q"seqBigInt"         => a.copy(cardinality = CardSeq, baseTpe = "BigInt")
      case q"seqBigDecimal"     => a.copy(cardinality = CardSeq, baseTpe = "BigDecimal")
      case q"seqDate"           => a.copy(cardinality = CardSeq, baseTpe = "Date")
      case q"seqDuration"       => a.copy(cardinality = CardSeq, baseTpe = "Duration")
      case q"seqInstant"        => a.copy(cardinality = CardSeq, baseTpe = "Instant")
      case q"seqLocalDate"      => a.copy(cardinality = CardSeq, baseTpe = "LocalDate")
      case q"seqLocalTime"      => a.copy(cardinality = CardSeq, baseTpe = "LocalTime")
      case q"seqLocalDateTime"  => a.copy(cardinality = CardSeq, baseTpe = "LocalDateTime")
      case q"seqOffsetTime"     => a.copy(cardinality = CardSeq, baseTpe = "OffsetTime")
      case q"seqOffsetDateTime" => a.copy(cardinality = CardSeq, baseTpe = "OffsetDateTime")
      case q"seqZonedDateTime"  => a.copy(cardinality = CardSeq, baseTpe = "ZonedDateTime")
      case q"seqUUID"           => a.copy(cardinality = CardSeq, baseTpe = "UUID")
      case q"seqURI"            => a.copy(cardinality = CardSeq, baseTpe = "URI")
      case q"arrayByte"         => a.copy(cardinality = CardSeq, baseTpe = "Byte")
      case q"seqShort"          => a.copy(cardinality = CardSeq, baseTpe = "Short")
      case q"seqChar"           => a.copy(cardinality = CardSeq, baseTpe = "Char")

      case q"seqString(${Lit.String(s)})"         => a.copy(cardinality = CardSeq, baseTpe = "String", description = Some(s))
      case q"seqInt(${Lit.String(s)})"            => a.copy(cardinality = CardSeq, baseTpe = "Int", description = Some(s))
      case q"seqLong(${Lit.String(s)})"           => a.copy(cardinality = CardSeq, baseTpe = "Long", description = Some(s))
      case q"seqFloat(${Lit.String(s)})"          => a.copy(cardinality = CardSeq, baseTpe = "Float", description = Some(s))
      case q"seqDouble(${Lit.String(s)})"         => a.copy(cardinality = CardSeq, baseTpe = "Double", description = Some(s))
      case q"seqBoolean(${Lit.String(s)})"        => a.copy(cardinality = CardSeq, baseTpe = "Boolean", description = Some(s))
      case q"seqBigInt(${Lit.String(s)})"         => a.copy(cardinality = CardSeq, baseTpe = "BigInt", description = Some(s))
      case q"seqBigDecimal(${Lit.String(s)})"     => a.copy(cardinality = CardSeq, baseTpe = "BigDecimal", description = Some(s))
      case q"seqDate(${Lit.String(s)})"           => a.copy(cardinality = CardSeq, baseTpe = "Date", description = Some(s))
      case q"seqDuration(${Lit.String(s)})"       => a.copy(cardinality = CardSeq, baseTpe = "Duration", description = Some(s))
      case q"seqInstant(${Lit.String(s)})"        => a.copy(cardinality = CardSeq, baseTpe = "Instant", description = Some(s))
      case q"seqLocalDate(${Lit.String(s)})"      => a.copy(cardinality = CardSeq, baseTpe = "LocalDate", description = Some(s))
      case q"seqLocalTime(${Lit.String(s)})"      => a.copy(cardinality = CardSeq, baseTpe = "LocalTime", description = Some(s))
      case q"seqLocalDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardSeq, baseTpe = "LocalDateTime", description = Some(s))
      case q"seqOffsetTime(${Lit.String(s)})"     => a.copy(cardinality = CardSeq, baseTpe = "OffsetTime", description = Some(s))
      case q"seqOffsetDateTime(${Lit.String(s)})" => a.copy(cardinality = CardSeq, baseTpe = "OffsetDateTime", description = Some(s))
      case q"seqZonedDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardSeq, baseTpe = "ZonedDateTime", description = Some(s))
      case q"seqUUID(${Lit.String(s)})"           => a.copy(cardinality = CardSeq, baseTpe = "UUID", description = Some(s))
      case q"seqURI(${Lit.String(s)})"            => a.copy(cardinality = CardSeq, baseTpe = "URI", description = Some(s))
      case q"arrayByte(${Lit.String(s)})"         => a.copy(cardinality = CardSeq, baseTpe = "Byte", description = Some(s))
      case q"seqShort(${Lit.String(s)})"          => a.copy(cardinality = CardSeq, baseTpe = "Short", description = Some(s))
      case q"seqChar(${Lit.String(s)})"           => a.copy(cardinality = CardSeq, baseTpe = "Char", description = Some(s))


      case q"mapString"         => a.copy(cardinality = CardMap, baseTpe = "String")
      case q"mapInt"            => a.copy(cardinality = CardMap, baseTpe = "Int")
      case q"mapLong"           => a.copy(cardinality = CardMap, baseTpe = "Long")
      case q"mapFloat"          => a.copy(cardinality = CardMap, baseTpe = "Float")
      case q"mapDouble"         => a.copy(cardinality = CardMap, baseTpe = "Double")
      case q"mapBoolean"        => a.copy(cardinality = CardMap, baseTpe = "Boolean")
      case q"mapBigInt"         => a.copy(cardinality = CardMap, baseTpe = "BigInt")
      case q"mapBigDecimal"     => a.copy(cardinality = CardMap, baseTpe = "BigDecimal")
      case q"mapDate"           => a.copy(cardinality = CardMap, baseTpe = "Date")
      case q"mapDuration"       => a.copy(cardinality = CardMap, baseTpe = "Duration")
      case q"mapInstant"        => a.copy(cardinality = CardMap, baseTpe = "Instant")
      case q"mapLocalDate"      => a.copy(cardinality = CardMap, baseTpe = "LocalDate")
      case q"mapLocalTime"      => a.copy(cardinality = CardMap, baseTpe = "LocalTime")
      case q"mapLocalDateTime"  => a.copy(cardinality = CardMap, baseTpe = "LocalDateTime")
      case q"mapOffsetTime"     => a.copy(cardinality = CardMap, baseTpe = "OffsetTime")
      case q"mapOffsetDateTime" => a.copy(cardinality = CardMap, baseTpe = "OffsetDateTime")
      case q"mapZonedDateTime"  => a.copy(cardinality = CardMap, baseTpe = "ZonedDateTime")
      case q"mapUUID"           => a.copy(cardinality = CardMap, baseTpe = "UUID")
      case q"mapURI"            => a.copy(cardinality = CardMap, baseTpe = "URI")
      case q"mapByte"           => a.copy(cardinality = CardMap, baseTpe = "Byte")
      case q"mapShort"          => a.copy(cardinality = CardMap, baseTpe = "Short")
      case q"mapChar"           => a.copy(cardinality = CardMap, baseTpe = "Char")

      case q"mapString(${Lit.String(s)})"         => a.copy(cardinality = CardMap, baseTpe = "String", description = Some(s))
      case q"mapInt(${Lit.String(s)})"            => a.copy(cardinality = CardMap, baseTpe = "Int", description = Some(s))
      case q"mapLong(${Lit.String(s)})"           => a.copy(cardinality = CardMap, baseTpe = "Long", description = Some(s))
      case q"mapFloat(${Lit.String(s)})"          => a.copy(cardinality = CardMap, baseTpe = "Float", description = Some(s))
      case q"mapDouble(${Lit.String(s)})"         => a.copy(cardinality = CardMap, baseTpe = "Double", description = Some(s))
      case q"mapBoolean(${Lit.String(s)})"        => a.copy(cardinality = CardMap, baseTpe = "Boolean", description = Some(s))
      case q"mapBigInt(${Lit.String(s)})"         => a.copy(cardinality = CardMap, baseTpe = "BigInt", description = Some(s))
      case q"mapBigDecimal(${Lit.String(s)})"     => a.copy(cardinality = CardMap, baseTpe = "BigDecimal", description = Some(s))
      case q"mapDate(${Lit.String(s)})"           => a.copy(cardinality = CardMap, baseTpe = "Date", description = Some(s))
      case q"mapDuration(${Lit.String(s)})"       => a.copy(cardinality = CardMap, baseTpe = "Duration", description = Some(s))
      case q"mapInstant(${Lit.String(s)})"        => a.copy(cardinality = CardMap, baseTpe = "Instant", description = Some(s))
      case q"mapLocalDate(${Lit.String(s)})"      => a.copy(cardinality = CardMap, baseTpe = "LocalDate", description = Some(s))
      case q"mapLocalTime(${Lit.String(s)})"      => a.copy(cardinality = CardMap, baseTpe = "LocalTime", description = Some(s))
      case q"mapLocalDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardMap, baseTpe = "LocalDateTime", description = Some(s))
      case q"mapOffsetTime(${Lit.String(s)})"     => a.copy(cardinality = CardMap, baseTpe = "OffsetTime", description = Some(s))
      case q"mapOffsetDateTime(${Lit.String(s)})" => a.copy(cardinality = CardMap, baseTpe = "OffsetDateTime", description = Some(s))
      case q"mapZonedDateTime(${Lit.String(s)})"  => a.copy(cardinality = CardMap, baseTpe = "ZonedDateTime", description = Some(s))
      case q"mapUUID(${Lit.String(s)})"           => a.copy(cardinality = CardMap, baseTpe = "UUID", description = Some(s))
      case q"mapURI(${Lit.String(s)})"            => a.copy(cardinality = CardMap, baseTpe = "URI", description = Some(s))
      case q"mapByte(${Lit.String(s)})"           => a.copy(cardinality = CardMap, baseTpe = "Byte", description = Some(s))
      case q"mapShort(${Lit.String(s)})"          => a.copy(cardinality = CardMap, baseTpe = "Short", description = Some(s))
      case q"mapChar(${Lit.String(s)})"           => a.copy(cardinality = CardMap, baseTpe = "Char", description = Some(s))


      // Validations ................................................

      case q"$prev.validate { ..case $cases }" =>
        handleValidationCases(prev, pp, entity, a, cases.toList, attr)

      case q"$prev.validate($test)" =>
        test match {
          case q"{ ..case $cases }: PartialFunction[$_, $_]" =>
            handleValidationCases(prev, pp, entity, a, cases.toList, attr)

          case _ =>
            oneValidationCall(entity, a)
            val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
            val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
            val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
            val validations1 = List(indent(test.toString()) -> "")
            acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))
        }

      case q"$prev.validate($test, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = List(indent(test.toString()) -> error)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))

      case q"$prev.validate($test, ${Term.Select(Lit.String(multilineMsg), Term.Name("stripMargin"))})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, q"$test")
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = List(indent(test.toString()) -> multilineMsg)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1))

      case q"$prev.validate($test, ${Term.Interpolate(Term.Name("s"), _, _)})" =>
        err(
          s"String interpolation not allowed for validation error messages of `$attr`. " +
            s"Please remove the s prefix."
        )

      case q"$prev.email" =>
        oneValidationCall(entity, a)
        val test  = "(s: String) => _dm.emailRegex.findFirstMatchIn(s).isDefined"
        val error = s"""`$$v` is not a valid email"""
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.email(${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = "(s: String) => _dm.emailRegex.findFirstMatchIn(s).isDefined"
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.regex(${Lit.String(regex)})" =>
        oneValidationCall(entity, a)
        val test  = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        val error = s"""\"$$v\" doesn't match regex pattern: ${regex.replace("$", "$$")}"""
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.regex(${Lit.String(regex)}, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.allowedValues(Seq(..$vs), ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""v => Seq$vs.contains(v)"""
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.allowedValues(..$vs)" =>
        oneValidationCall(entity, a)
        val test  = s"""v => Seq$vs.contains(v)"""
        val error = s"""Value `$$v` is not one of the allowed values in Seq$vs"""
        acc(pp, entity, prev, a.copy(validations = List(test -> error)))

      case q"$prev.require(..$otherAttrs)" =>
        val reqAttrs1 = a.attribute +: otherAttrs.toList.map(_.toString)
        acc(pp, entity, prev, a.copy(requiredAttrs = reqAttrs1))

      case q"$prev.value" => err(
        s"Calling `value` on attribute `$attr` is only allowed in validations code of other attributes."
      )

      case other =>
        println("UNEXPECTED TREE STRUCTURE:\n" + other.structure)
        unexpected(other)
    }
  }

  private def addBackRef(segmentPrefix: String, backRefEntity: String, entity: String): Unit = {
    val fullN              = fullEntity(segmentPrefix, entity)
    val backRefEntity1     = fullEntity(segmentPrefix, backRefEntity)
    val curBackRefEntities = backRefs.getOrElse(fullN, Nil)
    backRefs = backRefs + (fullN -> (curBackRefEntities :+ backRefEntity1))
  }

  private def handleValidationCases(
    prev: Tree,
    segmentPrefix: String,
    entity: String,
    a: MetaAttribute,
    cases: List[Case],
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
        err(s"""Please provide if-expression: case $v if <test..> = "$error"""", entity, a.attribute)

      case other => err("Unexpected validation case: " + other, entity, a.attribute)
    }.unzip

    val valueAttrs1 = valueAttrs.flatten.distinct.sorted
    val valueAttrs2 = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
    val reqAttrs1   = a.requiredAttrs ++ valueAttrs2
    val attr1       = a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations)
    acc(segmentPrefix, entity, prev, attr1)
  }

  private def oneValidationCall(entity: String, a: MetaAttribute) = if (a.validations.nonEmpty) {
    throw new Exception(
      s"Please use `validate { ..<pattern matches> }` for multiple validations of attribute `$entity.${a.attribute}`"
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

