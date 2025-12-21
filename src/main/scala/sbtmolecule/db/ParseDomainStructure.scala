package sbtmolecule.db

import scala.collection.mutable.ListBuffer
import scala.meta.*
import molecule.base.metaModel.*
import molecule.base.util.BaseHelpers
import molecule.core.dataModel.*
import org.atteo.evo.inflector.English


case class ParseDomainStructure(
  filePath: String,
  pkg: String,
  domain: String,
  body: Seq[Stat]
) extends BaseHelpers {

  private val reservedAttrNames = List(
    // Actions
    "query", "save", "insert", "update", "upsert", "delete",

    // sorting
    "a1", "a2", "a3", "a4", "a5", "d1", "d2", "d3", "d4", "d5",

    // Expressions
    "apply", "not", "has", "hasNo", "add", "remove",

    // Generic attributes
    "id",

    // Data Model
    "dataModel"
  )

  private val reservedScalaKeywords = List(
    "abstract", "case", "catch", "class", "def", "do", "else", "extends", "false",
    "final", "finally", "for", "forSome", "if", "implicit", "import", "lazy",
    "macro", "match", "new", "null", "object", "override", "package", "private",
    "protected", "return", "sealed", "super", "this", "throw", "trait", "try",
    "true", "type", "val", "var", "while", "with", "yield",
    "_", ":", "=", "=>", "<-", "<:", "<%", ">:", "#", "@"
  )

  private val allActions  = List("query", "save", "insert", "update", "delete", "rawQuery", "rawTransact")
  private val coreActions = List("query", "save", "insert", "update", "delete") // Core CRUD actions that must be available for role-restricted entities


  private var backRefs       = Map.empty[String, List[String]]
  private var reverseRefs    = Map.empty[String, List[MetaAttribute]]
  private var ent2joinTables = Map.empty[String, List[String]]
  private var joinTable2refs = Map.empty[String, List[(String, String, String)]]
  private val valueAttrs     = ListBuffer.empty[String]

  private def noMix() = throw new Exception(
    "Mixing prefixed and non-prefixed entities is not allowed."
  )
  private def unexpected(c: Tree, msg: String = ":") = throw new Exception(
    s"Unexpected domain structure definition code in file $filePath$msg\n" + c
  )


  private def err(msg: String, entity: String = "", attr: String = "") = {
    val fullEntity = if (entity.isEmpty && attr.isEmpty) "" else
      s" for attribute $entity.$attr"
    throw new Exception(
      s"""Problem in domain structure definition $pkg.$domain$fullEntity:
         |$msg
         |""".stripMargin
    )
  }

  // Collect enum definitions on all levels
  var enums = Map.empty[String, List[String]]

  // Collect role definitions
  var roles = List.empty[MetaRole]

  private def parseRoles(): Unit = {
    body.foreach {
      case q"trait $roleName extends Role with ..$actions" =>
        val roleActions = extractActions(actions.toList)
        roles = roles :+ MetaRole(roleName.toString, roleActions)
      case _                                               => // Ignore non-role definitions
    }
  }

  private def parseGeneralDbColumnProperties(): Map[String, Map[String, String]] = {
    body.collectFirst {
      case q"generalDbColumnProperties(..$props)" =>
        if (props.isEmpty)
          err("Please add at least one column property definition for a database.")
        props.map {
          case q"(Db.$db, Set(..$set))" => (db, set)
          case q"Db.$db -> Set(..$set)" => (db, set)
          case other                    => unexpected(other)
        }.map {
          case (db, set) =>
            if (set.isEmpty)
              err("Please add at least one attr -> props pair.")

            // Now parse each tuple in the set
            val columnProps = set.map {
              case q"($baseType, ${Lit.String(props)})" => (baseType, props)
              case q"$baseType -> ${Lit.String(props)}" => (baseType, props)
            }.map {
              case (baseType, props) =>
                val baseTypeName = baseType match {
                  case Term.Name(name) => name.stripPrefix("one")
                  case _               => baseType.toString.stripPrefix("one")
                }
                (baseTypeName, props)
            }
            (db.value, columnProps.toMap)
        }.toMap
    }.getOrElse(Map.empty[String, Map[String, String]])
  }

  def getMetaDomain: MetaDomain = {
    // First pass: collect role definitions and general db column properties (separate from entity parsing)
    parseRoles()
    val generalProps = parseGeneralDbColumnProperties()

    val hasSegements = body.exists {
      case q"object $_ { ..$_ }" => true
      case _                     => false
    }
    val segments     = if (hasSegements) {
      body.flatMap {
        // Object with template (may have extends Remove or Rename)
        case Defn.Object(_, segmentName, Template.After_4_4_0(_, inits, _, stats, _)) =>
          var segmentMigration: Option[SegmentMigration] = None
          inits.foreach {
            case init"Remove"                                                                                 =>
              segmentMigration = Some(SegmentMigration.Remove)
            case Init.After_4_6_0(Type.Apply.After_4_6_0(Type.Name("Rename"), Seq(Type.Name(newName))), _, _) =>
              segmentMigration = Some(SegmentMigration.Rename(newName))
            case init"RenameToDummySegment"                                                                   =>
              segmentMigration = Some(SegmentMigration.Rename("seg"))
            case _                                                                                            => // Other extends clauses ignored
          }
          Some(MetaSegment(segmentName.toString, getEntities(segmentName.toString + "_", stats), segmentMigration))

        case Defn.Enum.After_4_6_0(_, name, _, _, templ) => parseEnum[MetaSegment](name, templ)
        case q"trait $_ extends Role with ..$_"          => None // Skip role definitions in entity parsing
        case q"generalDbColumnProperties(..$_)"          => None // Skip general db props in entity parsing
        case q"trait $entity $template"                  => noMix()
      }
    } else {
      // No segments - parse entities directly, but skip role definitions and general db props
      List(MetaSegment("", getEntities("", body), None))
    }
    checkCircularMandatoryRefs(segments)
    val segments1 = addBackRefs(segments)
    val segments2 = if (enums.isEmpty) segments1 else {
      // Add segment with all enum definitions
      segments1 :+ MetaSegment("_enums",
        enums.toList.map {
          case (enumName, enumCases) =>
            MetaEntity(enumName, enumCases.map(c => MetaAttribute(c, OneValue, "String")))
        },
        None // Enum segment cannot be migrated
      )
    }
    val segments3 = addReverseRefs(segments2)
    val segments4 = addBridges(segments3)
    MetaDomain(pkg, domain, segments4, roles, generalProps)
  }

  private def checkCircularMandatoryRefs(segments: Seq[MetaSegment]): Unit = {
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

  private def addBackRefs(segments: Seq[MetaSegment]): List[MetaSegment] = {
    segments.map { segment =>
      val entities1 = segment.entities.map { entity =>
        entity.copy(backRefs = backRefs.getOrElse(entity.entity, Nil).distinct.sorted)
      }
      segment.copy(entities = entities1)
    }
  }.toList

  private def addReverseRefs(segments: Seq[MetaSegment]): List[MetaSegment] = {
    segments.map { segment =>
      val entities1 = segment.entities.map { entity =>
        val curAttrs        = entity.attributes
        val reverseRefAttrs = reverseRefs.getOrElse(entity.entity, Nil).distinct.sortBy(_.attribute)
        entity.copy(attributes = curAttrs ++ reverseRefAttrs)
      }
      segment.copy(entities = entities1)
    }
  }.toList

  private def addBridges(segments: Seq[MetaSegment]): List[MetaSegment] = {
    segments.map { segment =>
      val entities1 = segment.entities.map { entity =>
        val ent        = entity.entity
        val curAttrs   = entity.attributes
        val joinTables = ent2joinTables.getOrElse(ent, Nil).distinct.sorted
        val bridges    = joinTables.flatMap { joinTable =>
          val refCoordinates             = joinTable2refs.getOrElse(joinTable, Nil).distinct
          val Some((attr, ref1, revRef)) = refCoordinates.find(_._2 == ent)
          //          println("==========================")
          //          println(joinTable)
          //          println(ent)
          //          println((attr, ref1, revRef))
          refCoordinates.filterNot(_._2 == ent).sorted.map {
            case (refAttr2, targetRef, _) =>
              //              println(s"----- 1 -----   $ent    $revRef  $joinTable  $attr")
              //              println(s"----- 2 -----   $joinTable  $refAttr2      $targetRef     $revRef")
              val refData = s"$ent-$revRef-$joinTable-$attr-$refAttr2-$targetRef"
              MetaAttribute(English.plural(targetRef), SetValue, "ID", Nil, Some(targetRef), Some(refData), Some(ManyToMany))
          }
        }
        entity.copy(attributes = curAttrs ++ bridges)
      }
      segment.copy(entities = entities1)
    }
  }.toList

  private def getEntities(segmentPrefix: String, entities: Seq[Stat]): List[MetaEntity] = {
    def parseEntity(entityTpe: Name, template: Template, attrs: Seq[Stat], isJoinTable: Boolean): Option[MetaEntity] = {
      entityTpe.toString match {
        case r"([A-Z][a-zA-Z0-9]*)$entity" =>
          val msg =
            s"""Entity trait '$entity' should:
               |- follow the pattern [A-Z][a-zA-Z0-9]* (i.e. start with a Capital letter A-Z and only contain english letters or digits)
               |- in lower case not be a molecule keyword: ${marked(reservedAttrNames, entity).mkString(", ")}
               |- in lower case not be a Scala keyword: ${marked(reservedScalaKeywords, entity).mkString(", ")}
               |""".stripMargin
          clean(entity.toLowerCase, msg)
          val (entityRoles, entityUpdatingGrants, entityDeletingGrants, entityMigration) = extractEntityRoles(template)
          val entityActions                                                              = entityRoles.flatMap(roleName => roles.find(_.role == roleName).map(_.actions).getOrElse(Nil)).distinct.sorted

          // Validate entity has access to all 5 core CRUD actions through its roles (Layer 1)
          validateEntityActionCoverage(entity, entityRoles, entityActions)

          // Validate action grants (Layer 2)
          validateEntityGrants(entity, entityRoles, entityUpdatingGrants, "updating")
          validateEntityGrants(entity, entityRoles, entityDeletingGrants, "deleting")

          Some(getEntity(segmentPrefix, entityTpe, attrs.toList, isJoinTable, entityRoles, entityActions, entityUpdatingGrants, entityDeletingGrants, entityMigration))
        case other                         =>
          err("Entity trait name should match [A-Z][a-zA-Z0-9]* - found: " + other)
      }
    }

    def isRoleDefinition(template: Template): Boolean = {
      template.inits.exists {
        case init"Role" => true
        case _          => false
      }
    }

    entities.flatMap {
      case Defn.Trait.After_4_6_0(_, entityTpe, _, _, template@Template.After_4_4_0(_, _, _, stats, _)) =>
        // Skip role definitions
        if (isRoleDefinition(template)) {
          None
        } else {
          val isJoinTable = template.inits.exists {
            case init"Join" => true
            case _          => false
          }
          parseEntity(entityTpe, template, stats, isJoinTable)
        }
      case Defn.Enum.After_4_6_0(_, name, _, _, templ)                                                  => parseEnum[MetaEntity](name, templ)
      case q"object $o { ..$_ }"                                                                        => noMix()
      case q"generalDbColumnProperties(..$_)"                                                           => None // Skip general db column properties
      case other                                                                                        => unexpected(other)
    }
  }.toList

  private def getEntity(
    segmentPrefix: String,
    entityTpe: Name,
    attrs: Seq[Stat],
    isJoinTable: Boolean,
    entityRoles: List[String] = Nil,
    entityActions: List[String] = Nil,
    entityUpdatingGrants: List[String] = Nil,
    entityDeletingGrants: List[String] = Nil,
    entityMigration: Option[EntityMigration] = None
  ): MetaEntity = {
    val entity = entityTpe.toString
    if (entity.head.isLower) {
      err(s"Please change entity trait name `$entity` to start with upper case letter.")
    }
    if (attrs.isEmpty) {
      err(s"Please define attribute(s) in entity $entity")
    }
    val metaAttrs = getAttrs(segmentPrefix, entity, attrs, isJoinTable)

    // Validate attribute authorization (Layers 3 & 4)
    metaAttrs.foreach { attr =>
      // Validate Layer 3: Attribute restrictions
      validateAttributeRoleRestrictions(entity, attr.attribute, entityRoles, attr.onlyRoles, attr.excludedRoles)

      // Validate Layer 4: Attribute update grants
      validateAttributeUpdateGrants(entity, attr.attribute, entityRoles, attr.attrUpdatingGrants, attr.onlyRoles, attr.excludedRoles)
    }

    // Validate action grants have access to all attributes
    validateEntityGrantsAttributeAccess(entity, entityRoles, entityUpdatingGrants, entityDeletingGrants, metaAttrs)

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
    val metaAttrs1 = MetaAttribute("id", OneValue, "ID") +: metaAttrs.map { a =>
      val attr = a.attribute
      if (reqAttrs.contains(attr)) {
        val otherAttrs = reqGroupsMerged.collectFirst {
          case group if group.contains(attr) => group.filterNot(_ == attr)
        }
        a.copy(requiredAttrs = otherAttrs.get)
      } else a
    }

    MetaEntity(segmentPrefix + entity, metaAttrs1, Nil, mandatoryAttrs, mandatoryRefs, isJoinTable, None, entityRoles, entityActions, entityUpdatingGrants, entityDeletingGrants, entityMigration)
  }

  def marked(keywords: Seq[String], keyword: String) = keywords.map {
    case `keyword` => s"\u001b[41;37;1m $keyword \u001b[0m" // red background + white text + bold
    case other     => other
  }

  private def getAttrs(segmentPrefix: String, entity: String, attrs: Seq[Stat], isJoinTable: Boolean): List[MetaAttribute] = {
    attrs.flatMap {
      case q"val $attr = $defs" =>
        val attrStr = attr.toString
        val rawAttr = if (attrStr.contains("`")) {
          if (!defs.structure.contains("""Term.Name("alias")""")) {
            err(s"Please make an alias for back ticked attribute name $entity.$attr\n")
          } else {
            // strip back ticks and keep raw possibly keyword-conflicting name
            attrStr.tail.init
          }
        } else {
          attrStr
        }
        if (reservedAttrNames.contains(attrStr)) {
          err(s"Please change attribute name $entity.$attr to avoid collision with reserved Molecule names:\n" +
            marked(reservedAttrNames, attrStr).mkString(",  "))
        }
        Some(acc(segmentPrefix, entity, defs, MetaAttribute(rawAttr, OneValue, ""), isJoinTable))

      case Defn.Enum.After_4_6_0(_, name, _, _, templ) => parseEnum[MetaAttribute](name, templ)
      case other                                       => unexpected(other)
    }
  }.toList

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

  private def saveDescr(segmentPrefix: String, entity: String, prev: Tree, a: MetaAttribute, attr: String, s: String, isJoinTable: Boolean) = {
    if (s.isEmpty)
      err(s"Can't apply empty String as description option for attribute $attr")
    else if (s.contains("\""))
      err(s"Description option for attribute $attr can't contain quotation marks.")
    else
      acc(segmentPrefix, entity, prev, a.copy(description = Some(s)), isJoinTable)
  }

  def formatEntityName(tpe: Type) = tpe.toString.split('.').takeRight(2).mkString(".")

  def clean(token: String, errorMsg: String): String = {
    if (reservedAttrNames.contains(token) || reservedScalaKeywords.contains(token)) {
      err(errorMsg)
    } else {
      token
    }
  }

  //  @tailrec
  private def acc(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    accOptions(segmentPrefix, entity, t, a, isJoinTable)
  }

  // Chained pattern matching methods to avoid method size limits ................................................

  private def accOptions(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    val attr = entity + "." + a.attribute
    t match {
      case q"$prev.index"     => acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "index"), isJoinTable)
      case q"$prev.unique"    => acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "unique"), isJoinTable)
      case q"$prev.owner"     => acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "owner"), isJoinTable)
      case q"$prev.mandatory" => acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "mandatory"), isJoinTable)

      case q"$prev.index(${Lit.String(s)})"     => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable); acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "index"), isJoinTable)
      case q"$prev.unique(${Lit.String(s)})"    => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable); acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "unique"), isJoinTable)
      case q"$prev.owner(${Lit.String(s)})"     => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable); acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "owner"), isJoinTable)
      case q"$prev.mandatory(${Lit.String(s)})" => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable); acc(segmentPrefix, entity, prev, a.copy(options = a.options :+ "mandatory"), isJoinTable)

      case q"$prev.description(${Lit.String(s)})" => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable)
      case q"$prev.apply(${Lit.String(s)})"       => saveDescr(segmentPrefix, entity, prev, a, attr, s, isJoinTable)

      case q"$prev.alias(${Lit.String(s)})" =>
        def msg(alias: String): String =
          s"""Alias '$alias' for attribute $attr should:
             |- follow the pattern [a-z][a-zA-Z0-9]* (i.e. start with a lower case letter a-z)
             |- not be a molecule keyword: ${marked(reservedAttrNames, alias).mkString(", ")}
             |- not be a Scala keyword: ${marked(reservedScalaKeywords, alias).mkString(", ")}
             |""".stripMargin
        s match {
          case r"([a-z][a-zA-Z0-9]*)$alias" =>
            acc(segmentPrefix, entity, prev, a.copy(alias = Some(clean(alias, msg(alias)))), isJoinTable)
          case other                        => err(msg(other))
        }

      case q"$prev.dbColumnProperties(..$props)" =>
        val dbColumnProps = props.map {
          case q"(Db.$db, ${Lit.String(colDef)})" => db.toString() -> colDef
          case q"Db.$db -> ${Lit.String(colDef)}" => db.toString() -> colDef
        }.toMap
        acc(segmentPrefix, entity, prev, a.copy(dbColumnProps = dbColumnProps), isJoinTable)

      case _ => accMigration(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accMigration(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case t@q"$prev.rename(${Lit.String(newName)})" =>
        // Store explicit rename (attribute field has current name, Rename has new name)
        // Capture source position from the entire attribute definition (from val to .rename)
        val sourcePos = Some((t.pos.start, t.pos.end))
        acc(segmentPrefix, entity, prev, a.copy(
          migration = Some(AttrMigration.Rename(newName)),
          sourcePosition = sourcePos
        ), isJoinTable)

      case t@q"$prev.becomes(${Term.Name(newName)})" =>
        // Store explicit name of new attribute
        val sourcePos = Some((t.pos.start, t.pos.end))
        acc(segmentPrefix, entity, prev, a.copy(
          migration = Some(AttrMigration.Rename(newName)),
          sourcePosition = sourcePos
        ), isJoinTable)

      case t@q"$prev.remove" =>
        // Store explicit removal
        // Capture source position from the entire attribute definition (from val to .remove)
        val sourcePos = Some((t.pos.start, t.pos.end))
        acc(segmentPrefix, entity, prev, a.copy(
          migration = Some(AttrMigration.Remove),
          sourcePosition = sourcePos
        ), isJoinTable)

      case _ => accRelationships(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accRelationships(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"manyToOne[$ref0]" =>
        val ref = formatEntityName(ref0)
        addBackRef(segmentPrefix, entity, ref)
        val reverseRef      = fullEntity(segmentPrefix, English.plural(entity))
        val fullEnt         = fullEntity(segmentPrefix, entity)
        val fullRef         = fullEntity(segmentPrefix, ref)
        val options         = if (a.options.contains("owner")) List("owned") else Nil
        val reverseMetaAttr = MetaAttribute(reverseRef, SetValue, "ID", Nil, Some(fullEnt), Some(a.attribute), Some(OneToMany), options = options)
        addReverseRef(fullRef, reverseMetaAttr)
        if (isJoinTable) {
          addManyToManyBridges(fullEnt, a.attribute, fullRef, reverseRef)
        }
        a.copy(value = OneValue, baseTpe = "ID", ref = Some(fullRef), reverseRef = Some(reverseRef), relationship = Some(ManyToOne))

      case q"manyToOne[$ref0].oneToMany(${Lit.String(reverseRef0)})" =>
        val reverseRef1 = reverseRef0 match {
          case r"([A-Z][a-zA-Z0-9]*)$ref" => ref
          case other                      => err(
            s"""oneToMany name should start with capital letter and contain only english letters and digits. Found: "$other""""
          )
        }
        val ref         = formatEntityName(ref0)
        addBackRef(segmentPrefix, entity, ref)
        val reverseRef      = fullEntity(segmentPrefix, reverseRef1)
        val fullEnt         = fullEntity(segmentPrefix, entity)
        val fullRef         = fullEntity(segmentPrefix, ref)
        val options         = if (a.options.contains("owner")) List("owned") else Nil
        val reverseMetaAttr = MetaAttribute(reverseRef, SetValue, "ID", Nil, Some(fullEnt), Some(a.attribute), Some(OneToMany), options = options)
        addReverseRef(fullRef, reverseMetaAttr)
        if (isJoinTable) {
          addManyToManyBridges(fullEnt, a.attribute, fullRef, reverseRef)
        }
        a.copy(value = OneValue, baseTpe = "ID", ref = Some(fullRef), reverseRef = Some(reverseRef), relationship = Some(ManyToOne))

      case _ => accEnums(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accEnums(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"oneEnum[$enumTpe]" => a.copy(value = OneValue, baseTpe = "String", enumTpe = Some(enumTpe.toString))
      case q"setEnum[$enumTpe]" => a.copy(value = SetValue, baseTpe = "String", enumTpe = Some(enumTpe.toString))
      case q"seqEnum[$enumTpe]" => a.copy(value = SeqValue, baseTpe = "String", enumTpe = Some(enumTpe.toString))

      case q"oneEnum[$enumTpe](${Lit.String(s)})" => a.copy(value = OneValue, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))
      case q"setEnum[$enumTpe](${Lit.String(s)})" => a.copy(value = SetValue, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))
      case q"seqEnum[$enumTpe](${Lit.String(s)})" => a.copy(value = SeqValue, baseTpe = "String", enumTpe = Some(enumTpe.toString), description = Some(s))

      case _ => accAttributesOne(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAttributesOne(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"oneString"         => a.copy(value = OneValue, baseTpe = "String")
      case q"oneInt"            => a.copy(value = OneValue, baseTpe = "Int")
      case q"oneLong"           => a.copy(value = OneValue, baseTpe = "Long")
      case q"oneFloat"          => a.copy(value = OneValue, baseTpe = "Float")
      case q"oneDouble"         => a.copy(value = OneValue, baseTpe = "Double")
      case q"oneBoolean"        => a.copy(value = OneValue, baseTpe = "Boolean")
      case q"oneBigInt"         => a.copy(value = OneValue, baseTpe = "BigInt")
      case q"oneBigDecimal"     => a.copy(value = OneValue, baseTpe = "BigDecimal")
      case q"oneDate"           => a.copy(value = OneValue, baseTpe = "Date")
      case q"oneDuration"       => a.copy(value = OneValue, baseTpe = "Duration")
      case q"oneInstant"        => a.copy(value = OneValue, baseTpe = "Instant")
      case q"oneLocalDate"      => a.copy(value = OneValue, baseTpe = "LocalDate")
      case q"oneLocalTime"      => a.copy(value = OneValue, baseTpe = "LocalTime")
      case q"oneLocalDateTime"  => a.copy(value = OneValue, baseTpe = "LocalDateTime")
      case q"oneOffsetTime"     => a.copy(value = OneValue, baseTpe = "OffsetTime")
      case q"oneOffsetDateTime" => a.copy(value = OneValue, baseTpe = "OffsetDateTime")
      case q"oneZonedDateTime"  => a.copy(value = OneValue, baseTpe = "ZonedDateTime")
      case q"oneUUID"           => a.copy(value = OneValue, baseTpe = "UUID")
      case q"oneURI"            => a.copy(value = OneValue, baseTpe = "URI")
      case q"oneByte"           => a.copy(value = OneValue, baseTpe = "Byte")
      case q"oneShort"          => a.copy(value = OneValue, baseTpe = "Short")
      case q"oneChar"           => a.copy(value = OneValue, baseTpe = "Char")

      case q"oneBigDecimal($precision, $scale)" => a.copy(
        value = OneValue,
        baseTpe = "BigDecimal",
        options = a.options :+ s"$precision,$scale"
      )

      case q"oneString(${Lit.String(s)})"         => a.copy(value = OneValue, baseTpe = "String", description = Some(s))
      case q"oneInt(${Lit.String(s)})"            => a.copy(value = OneValue, baseTpe = "Int", description = Some(s))
      case q"oneLong(${Lit.String(s)})"           => a.copy(value = OneValue, baseTpe = "Long", description = Some(s))
      case q"oneFloat(${Lit.String(s)})"          => a.copy(value = OneValue, baseTpe = "Float", description = Some(s))
      case q"oneDouble(${Lit.String(s)})"         => a.copy(value = OneValue, baseTpe = "Double", description = Some(s))
      case q"oneBoolean(${Lit.String(s)})"        => a.copy(value = OneValue, baseTpe = "Boolean", description = Some(s))
      case q"oneBigInt(${Lit.String(s)})"         => a.copy(value = OneValue, baseTpe = "BigInt", description = Some(s))
      case q"oneBigDecimal(${Lit.String(s)})"     => a.copy(value = OneValue, baseTpe = "BigDecimal", description = Some(s))
      case q"oneDate(${Lit.String(s)})"           => a.copy(value = OneValue, baseTpe = "Date", description = Some(s))
      case q"oneDuration(${Lit.String(s)})"       => a.copy(value = OneValue, baseTpe = "Duration", description = Some(s))
      case q"oneInstant(${Lit.String(s)})"        => a.copy(value = OneValue, baseTpe = "Instant", description = Some(s))
      case q"oneLocalDate(${Lit.String(s)})"      => a.copy(value = OneValue, baseTpe = "LocalDate", description = Some(s))
      case q"oneLocalTime(${Lit.String(s)})"      => a.copy(value = OneValue, baseTpe = "LocalTime", description = Some(s))
      case q"oneLocalDateTime(${Lit.String(s)})"  => a.copy(value = OneValue, baseTpe = "LocalDateTime", description = Some(s))
      case q"oneOffsetTime(${Lit.String(s)})"     => a.copy(value = OneValue, baseTpe = "OffsetTime", description = Some(s))
      case q"oneOffsetDateTime(${Lit.String(s)})" => a.copy(value = OneValue, baseTpe = "OffsetDateTime", description = Some(s))
      case q"oneZonedDateTime(${Lit.String(s)})"  => a.copy(value = OneValue, baseTpe = "ZonedDateTime", description = Some(s))
      case q"oneUUID(${Lit.String(s)})"           => a.copy(value = OneValue, baseTpe = "UUID", description = Some(s))
      case q"oneURI(${Lit.String(s)})"            => a.copy(value = OneValue, baseTpe = "URI", description = Some(s))
      case q"oneByte(${Lit.String(s)})"           => a.copy(value = OneValue, baseTpe = "Byte", description = Some(s))
      case q"oneShort(${Lit.String(s)})"          => a.copy(value = OneValue, baseTpe = "Short", description = Some(s))
      case q"oneChar(${Lit.String(s)})"           => a.copy(value = OneValue, baseTpe = "Char", description = Some(s))

      case _ => accAttributesSet(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAttributesSet(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"setString"         => a.copy(value = SetValue, baseTpe = "String")
      case q"setInt"            => a.copy(value = SetValue, baseTpe = "Int")
      case q"setLong"           => a.copy(value = SetValue, baseTpe = "Long")
      case q"setFloat"          => a.copy(value = SetValue, baseTpe = "Float")
      case q"setDouble"         => a.copy(value = SetValue, baseTpe = "Double")
      case q"setBoolean"        => a.copy(value = SetValue, baseTpe = "Boolean")
      case q"setBigInt"         => a.copy(value = SetValue, baseTpe = "BigInt")
      case q"setBigDecimal"     => a.copy(value = SetValue, baseTpe = "BigDecimal")
      case q"setDate"           => a.copy(value = SetValue, baseTpe = "Date")
      case q"setDuration"       => a.copy(value = SetValue, baseTpe = "Duration")
      case q"setInstant"        => a.copy(value = SetValue, baseTpe = "Instant")
      case q"setLocalDate"      => a.copy(value = SetValue, baseTpe = "LocalDate")
      case q"setLocalTime"      => a.copy(value = SetValue, baseTpe = "LocalTime")
      case q"setLocalDateTime"  => a.copy(value = SetValue, baseTpe = "LocalDateTime")
      case q"setOffsetTime"     => a.copy(value = SetValue, baseTpe = "OffsetTime")
      case q"setOffsetDateTime" => a.copy(value = SetValue, baseTpe = "OffsetDateTime")
      case q"setZonedDateTime"  => a.copy(value = SetValue, baseTpe = "ZonedDateTime")
      case q"setUUID"           => a.copy(value = SetValue, baseTpe = "UUID")
      case q"setURI"            => a.copy(value = SetValue, baseTpe = "URI")
      case q"setByte"           => a.copy(value = SetValue, baseTpe = "Byte")
      case q"setShort"          => a.copy(value = SetValue, baseTpe = "Short")
      case q"setChar"           => a.copy(value = SetValue, baseTpe = "Char")

      case q"setString(${Lit.String(s)})"         => a.copy(value = SetValue, baseTpe = "String", description = Some(s))
      case q"setInt(${Lit.String(s)})"            => a.copy(value = SetValue, baseTpe = "Int", description = Some(s))
      case q"setLong(${Lit.String(s)})"           => a.copy(value = SetValue, baseTpe = "Long", description = Some(s))
      case q"setFloat(${Lit.String(s)})"          => a.copy(value = SetValue, baseTpe = "Float", description = Some(s))
      case q"setDouble(${Lit.String(s)})"         => a.copy(value = SetValue, baseTpe = "Double", description = Some(s))
      case q"setBoolean(${Lit.String(s)})"        => a.copy(value = SetValue, baseTpe = "Boolean", description = Some(s))
      case q"setBigInt(${Lit.String(s)})"         => a.copy(value = SetValue, baseTpe = "BigInt", description = Some(s))
      case q"setBigDecimal(${Lit.String(s)})"     => a.copy(value = SetValue, baseTpe = "BigDecimal", description = Some(s))
      case q"setDate(${Lit.String(s)})"           => a.copy(value = SetValue, baseTpe = "Date", description = Some(s))
      case q"setDuration(${Lit.String(s)})"       => a.copy(value = SetValue, baseTpe = "Duration", description = Some(s))
      case q"setInstant(${Lit.String(s)})"        => a.copy(value = SetValue, baseTpe = "Instant", description = Some(s))
      case q"setLocalDate(${Lit.String(s)})"      => a.copy(value = SetValue, baseTpe = "LocalDate", description = Some(s))
      case q"setLocalTime(${Lit.String(s)})"      => a.copy(value = SetValue, baseTpe = "LocalTime", description = Some(s))
      case q"setLocalDateTime(${Lit.String(s)})"  => a.copy(value = SetValue, baseTpe = "LocalDateTime", description = Some(s))
      case q"setOffsetTime(${Lit.String(s)})"     => a.copy(value = SetValue, baseTpe = "OffsetTime", description = Some(s))
      case q"setOffsetDateTime(${Lit.String(s)})" => a.copy(value = SetValue, baseTpe = "OffsetDateTime", description = Some(s))
      case q"setZonedDateTime(${Lit.String(s)})"  => a.copy(value = SetValue, baseTpe = "ZonedDateTime", description = Some(s))
      case q"setUUID(${Lit.String(s)})"           => a.copy(value = SetValue, baseTpe = "UUID", description = Some(s))
      case q"setURI(${Lit.String(s)})"            => a.copy(value = SetValue, baseTpe = "URI", description = Some(s))
      case q"setByte(${Lit.String(s)})"           => a.copy(value = SetValue, baseTpe = "Byte", description = Some(s))
      case q"setShort(${Lit.String(s)})"          => a.copy(value = SetValue, baseTpe = "Short", description = Some(s))
      case q"setChar(${Lit.String(s)})"           => a.copy(value = SetValue, baseTpe = "Char", description = Some(s))

      case _ => accAttributesSeq(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAttributesSeq(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"seqString"         => a.copy(value = SeqValue, baseTpe = "String")
      case q"seqInt"            => a.copy(value = SeqValue, baseTpe = "Int")
      case q"seqLong"           => a.copy(value = SeqValue, baseTpe = "Long")
      case q"seqFloat"          => a.copy(value = SeqValue, baseTpe = "Float")
      case q"seqDouble"         => a.copy(value = SeqValue, baseTpe = "Double")
      case q"seqBoolean"        => a.copy(value = SeqValue, baseTpe = "Boolean")
      case q"seqBigInt"         => a.copy(value = SeqValue, baseTpe = "BigInt")
      case q"seqBigDecimal"     => a.copy(value = SeqValue, baseTpe = "BigDecimal")
      case q"seqDate"           => a.copy(value = SeqValue, baseTpe = "Date")
      case q"seqDuration"       => a.copy(value = SeqValue, baseTpe = "Duration")
      case q"seqInstant"        => a.copy(value = SeqValue, baseTpe = "Instant")
      case q"seqLocalDate"      => a.copy(value = SeqValue, baseTpe = "LocalDate")
      case q"seqLocalTime"      => a.copy(value = SeqValue, baseTpe = "LocalTime")
      case q"seqLocalDateTime"  => a.copy(value = SeqValue, baseTpe = "LocalDateTime")
      case q"seqOffsetTime"     => a.copy(value = SeqValue, baseTpe = "OffsetTime")
      case q"seqOffsetDateTime" => a.copy(value = SeqValue, baseTpe = "OffsetDateTime")
      case q"seqZonedDateTime"  => a.copy(value = SeqValue, baseTpe = "ZonedDateTime")
      case q"seqUUID"           => a.copy(value = SeqValue, baseTpe = "UUID")
      case q"seqURI"            => a.copy(value = SeqValue, baseTpe = "URI")
      case q"arrayByte"         => a.copy(value = SeqValue, baseTpe = "Byte")
      case q"seqShort"          => a.copy(value = SeqValue, baseTpe = "Short")
      case q"seqChar"           => a.copy(value = SeqValue, baseTpe = "Char")

      case q"seqString(${Lit.String(s)})"         => a.copy(value = SeqValue, baseTpe = "String", description = Some(s))
      case q"seqInt(${Lit.String(s)})"            => a.copy(value = SeqValue, baseTpe = "Int", description = Some(s))
      case q"seqLong(${Lit.String(s)})"           => a.copy(value = SeqValue, baseTpe = "Long", description = Some(s))
      case q"seqFloat(${Lit.String(s)})"          => a.copy(value = SeqValue, baseTpe = "Float", description = Some(s))
      case q"seqDouble(${Lit.String(s)})"         => a.copy(value = SeqValue, baseTpe = "Double", description = Some(s))
      case q"seqBoolean(${Lit.String(s)})"        => a.copy(value = SeqValue, baseTpe = "Boolean", description = Some(s))
      case q"seqBigInt(${Lit.String(s)})"         => a.copy(value = SeqValue, baseTpe = "BigInt", description = Some(s))
      case q"seqBigDecimal(${Lit.String(s)})"     => a.copy(value = SeqValue, baseTpe = "BigDecimal", description = Some(s))
      case q"seqDate(${Lit.String(s)})"           => a.copy(value = SeqValue, baseTpe = "Date", description = Some(s))
      case q"seqDuration(${Lit.String(s)})"       => a.copy(value = SeqValue, baseTpe = "Duration", description = Some(s))
      case q"seqInstant(${Lit.String(s)})"        => a.copy(value = SeqValue, baseTpe = "Instant", description = Some(s))
      case q"seqLocalDate(${Lit.String(s)})"      => a.copy(value = SeqValue, baseTpe = "LocalDate", description = Some(s))
      case q"seqLocalTime(${Lit.String(s)})"      => a.copy(value = SeqValue, baseTpe = "LocalTime", description = Some(s))
      case q"seqLocalDateTime(${Lit.String(s)})"  => a.copy(value = SeqValue, baseTpe = "LocalDateTime", description = Some(s))
      case q"seqOffsetTime(${Lit.String(s)})"     => a.copy(value = SeqValue, baseTpe = "OffsetTime", description = Some(s))
      case q"seqOffsetDateTime(${Lit.String(s)})" => a.copy(value = SeqValue, baseTpe = "OffsetDateTime", description = Some(s))
      case q"seqZonedDateTime(${Lit.String(s)})"  => a.copy(value = SeqValue, baseTpe = "ZonedDateTime", description = Some(s))
      case q"seqUUID(${Lit.String(s)})"           => a.copy(value = SeqValue, baseTpe = "UUID", description = Some(s))
      case q"seqURI(${Lit.String(s)})"            => a.copy(value = SeqValue, baseTpe = "URI", description = Some(s))
      case q"arrayByte(${Lit.String(s)})"         => a.copy(value = SeqValue, baseTpe = "Byte", description = Some(s))
      case q"seqShort(${Lit.String(s)})"          => a.copy(value = SeqValue, baseTpe = "Short", description = Some(s))
      case q"seqChar(${Lit.String(s)})"           => a.copy(value = SeqValue, baseTpe = "Char", description = Some(s))

      case _ => accAttributesMap(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAttributesMap(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      case q"mapString"         => a.copy(value = MapValue, baseTpe = "String")
      case q"mapInt"            => a.copy(value = MapValue, baseTpe = "Int")
      case q"mapLong"           => a.copy(value = MapValue, baseTpe = "Long")
      case q"mapFloat"          => a.copy(value = MapValue, baseTpe = "Float")
      case q"mapDouble"         => a.copy(value = MapValue, baseTpe = "Double")
      case q"mapBoolean"        => a.copy(value = MapValue, baseTpe = "Boolean")
      case q"mapBigInt"         => a.copy(value = MapValue, baseTpe = "BigInt")
      case q"mapBigDecimal"     => a.copy(value = MapValue, baseTpe = "BigDecimal")
      case q"mapDate"           => a.copy(value = MapValue, baseTpe = "Date")
      case q"mapDuration"       => a.copy(value = MapValue, baseTpe = "Duration")
      case q"mapInstant"        => a.copy(value = MapValue, baseTpe = "Instant")
      case q"mapLocalDate"      => a.copy(value = MapValue, baseTpe = "LocalDate")
      case q"mapLocalTime"      => a.copy(value = MapValue, baseTpe = "LocalTime")
      case q"mapLocalDateTime"  => a.copy(value = MapValue, baseTpe = "LocalDateTime")
      case q"mapOffsetTime"     => a.copy(value = MapValue, baseTpe = "OffsetTime")
      case q"mapOffsetDateTime" => a.copy(value = MapValue, baseTpe = "OffsetDateTime")
      case q"mapZonedDateTime"  => a.copy(value = MapValue, baseTpe = "ZonedDateTime")
      case q"mapUUID"           => a.copy(value = MapValue, baseTpe = "UUID")
      case q"mapURI"            => a.copy(value = MapValue, baseTpe = "URI")
      case q"mapByte"           => a.copy(value = MapValue, baseTpe = "Byte")
      case q"mapShort"          => a.copy(value = MapValue, baseTpe = "Short")
      case q"mapChar"           => a.copy(value = MapValue, baseTpe = "Char")

      case q"mapString(${Lit.String(s)})"         => a.copy(value = MapValue, baseTpe = "String", description = Some(s))
      case q"mapInt(${Lit.String(s)})"            => a.copy(value = MapValue, baseTpe = "Int", description = Some(s))
      case q"mapLong(${Lit.String(s)})"           => a.copy(value = MapValue, baseTpe = "Long", description = Some(s))
      case q"mapFloat(${Lit.String(s)})"          => a.copy(value = MapValue, baseTpe = "Float", description = Some(s))
      case q"mapDouble(${Lit.String(s)})"         => a.copy(value = MapValue, baseTpe = "Double", description = Some(s))
      case q"mapBoolean(${Lit.String(s)})"        => a.copy(value = MapValue, baseTpe = "Boolean", description = Some(s))
      case q"mapBigInt(${Lit.String(s)})"         => a.copy(value = MapValue, baseTpe = "BigInt", description = Some(s))
      case q"mapBigDecimal(${Lit.String(s)})"     => a.copy(value = MapValue, baseTpe = "BigDecimal", description = Some(s))
      case q"mapDate(${Lit.String(s)})"           => a.copy(value = MapValue, baseTpe = "Date", description = Some(s))
      case q"mapDuration(${Lit.String(s)})"       => a.copy(value = MapValue, baseTpe = "Duration", description = Some(s))
      case q"mapInstant(${Lit.String(s)})"        => a.copy(value = MapValue, baseTpe = "Instant", description = Some(s))
      case q"mapLocalDate(${Lit.String(s)})"      => a.copy(value = MapValue, baseTpe = "LocalDate", description = Some(s))
      case q"mapLocalTime(${Lit.String(s)})"      => a.copy(value = MapValue, baseTpe = "LocalTime", description = Some(s))
      case q"mapLocalDateTime(${Lit.String(s)})"  => a.copy(value = MapValue, baseTpe = "LocalDateTime", description = Some(s))
      case q"mapOffsetTime(${Lit.String(s)})"     => a.copy(value = MapValue, baseTpe = "OffsetTime", description = Some(s))
      case q"mapOffsetDateTime(${Lit.String(s)})" => a.copy(value = MapValue, baseTpe = "OffsetDateTime", description = Some(s))
      case q"mapZonedDateTime(${Lit.String(s)})"  => a.copy(value = MapValue, baseTpe = "ZonedDateTime", description = Some(s))
      case q"mapUUID(${Lit.String(s)})"           => a.copy(value = MapValue, baseTpe = "UUID", description = Some(s))
      case q"mapURI(${Lit.String(s)})"            => a.copy(value = MapValue, baseTpe = "URI", description = Some(s))
      case q"mapByte(${Lit.String(s)})"           => a.copy(value = MapValue, baseTpe = "Byte", description = Some(s))
      case q"mapShort(${Lit.String(s)})"          => a.copy(value = MapValue, baseTpe = "Short", description = Some(s))
      case q"mapChar(${Lit.String(s)})"           => a.copy(value = MapValue, baseTpe = "Char", description = Some(s))

      case _ => accValidations(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accValidations(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    val attr = entity + "." + a.attribute
    t match {

      case q"$prev.validate { ..case $cases }" =>
        handleValidationCases(prev, segmentPrefix, entity, a, cases.toList, attr, isJoinTable)

      case q"$prev.validate($test)" =>
        test match {
          case q"{ ..case $cases }: PartialFunction[$_, $_]" =>
            handleValidationCases(prev, segmentPrefix, entity, a, cases.toList, attr, isJoinTable)
          case _                                             =>
            oneValidationCall(entity, a)
            val valueAttrs1  = extractValueAttrs(entity, a, test.asInstanceOf[Stat])
            val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
            val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
            val validations1 = List(indent(test.toString()) -> "")
            acc(segmentPrefix, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1), isJoinTable)
        }

      case q"$prev.validate($test, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, test.asInstanceOf[Stat])
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = List(indent(test.toString()) -> error)
        acc(segmentPrefix, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1), isJoinTable)

      case q"$prev.validate($test, ${Term.Select(Lit.String(multilineMsg), Term.Name("stripMargin"))})" =>
        oneValidationCall(entity, a)
        val valueAttrs1  = extractValueAttrs(entity, a, test.asInstanceOf[Stat])
        val valueAttrs2  = if (valueAttrs1.isEmpty) Nil else (a.attribute +: valueAttrs1).distinct.sorted
        val reqAttrs1    = a.requiredAttrs ++ valueAttrs2
        val validations1 = List(indent(test.toString()) -> multilineMsg)
        acc(segmentPrefix, entity, prev, a.copy(requiredAttrs = reqAttrs1, valueAttrs = valueAttrs1, validations = validations1), isJoinTable)

      case q"$prev.validate($test, ${Term.Interpolate(Term.Name("s"), _, _)})" =>
        err(
          s"String interpolation not allowed for validation error messages of `$attr`. " +
            s"Please remove the s prefix."
        )

      case q"$prev.email" =>
        oneValidationCall(entity, a)
        val test  = "(s: String) => emailRegex.findFirstMatchIn(s).isDefined"
        val error = s"""`$$v` is not a valid email"""
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.email(${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = "(s: String) => emailRegex.findFirstMatchIn(s).isDefined"
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.regex(${Lit.String(regex)})" =>
        oneValidationCall(entity, a)
        val test  = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        val error = s"""\"$$v\" doesn't match regex pattern: ${regex.replace("$", "$$")}"""
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.regex(${Lit.String(regex)}, ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""(s: String) => "$regex".r.findFirstMatchIn(s).isDefined"""
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.allowedValues(Seq(..$vs), ${Lit.String(error)})" =>
        oneValidationCall(entity, a)
        val test = s"""v => Seq$vs.contains(v)"""
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.allowedValues(..$vs)" =>
        oneValidationCall(entity, a)
        val test  = s"""v => Seq$vs.contains(v)"""
        val error = s"""Value `$$v` is not one of the allowed values in Seq$vs"""
        acc(segmentPrefix, entity, prev, a.copy(validations = List(test -> error)), isJoinTable)

      case q"$prev.require(..$otherAttrs)" =>
        val reqAttrs1 = a.attribute +: otherAttrs.toList.map(_.toString)
        acc(segmentPrefix, entity, prev, a.copy(requiredAttrs = reqAttrs1), isJoinTable)

      case q"$prev.value" => err(
        s"Calling `value` on attribute `$attr` is only allowed in validations code of other attributes."
      )

      case _ => accAccessControl(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAccessControl(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      // Authorization model - Scala 3 patterns (with using)
      // Layer 3: Attribute restrictions
      case q"$prev.exclude[$roleType](using $_)" =>
        val roles = extractRolesFromType(roleType)
        if (a.onlyRoles.nonEmpty) {
          err(s"Cannot use both .only and .exclude on same attribute $entity.${a.attribute}")
        }
        acc(segmentPrefix, entity, prev, a.copy(excludedRoles = roles), isJoinTable)

      case q"$prev.only[$roleType](using $_)" =>
        val roles = extractRolesFromType(roleType)
        if (a.excludedRoles.nonEmpty) {
          err(s"Cannot use both .only and .exclude on same attribute $entity.${a.attribute}")
        }
        acc(segmentPrefix, entity, prev, a.copy(onlyRoles = roles), isJoinTable)

      // Layer 4: Attribute update grants
      case q"$prev.updating[$roleType](using $_)" =>
        val roles = extractRolesFromType(roleType)
        acc(segmentPrefix, entity, prev, a.copy(attrUpdatingGrants = roles), isJoinTable)

      case _ => accAccessControlScala2(segmentPrefix, entity, t, a, isJoinTable)
    }
  }

  private def accAccessControlScala2(segmentPrefix: String, entity: String, t: Tree, a: MetaAttribute, isJoinTable: Boolean): MetaAttribute = {
    t match {
      // Authorization model - Scala 2 compatible patterns
      // Layer 3: Attribute restrictions
      case q"$prev.exclude[$roleType]" =>
        val roles = extractRolesFromType(roleType)
        if (a.onlyRoles.nonEmpty) {
          err(s"Cannot use both .only and .exclude on same attribute $entity.${a.attribute}")
        }
        acc(segmentPrefix, entity, prev, a.copy(excludedRoles = roles), isJoinTable)

      case q"$prev.only[$roleType]" =>
        val roles = extractRolesFromType(roleType)
        if (a.excludedRoles.nonEmpty) {
          err(s"Cannot use both .only and .exclude on same attribute $entity.${a.attribute}")
        }
        acc(segmentPrefix, entity, prev, a.copy(onlyRoles = roles), isJoinTable)

      // Layer 4: Attribute update grants
      case q"$prev.updating[$roleType]" =>
        val roles = extractRolesFromType(roleType)
        acc(segmentPrefix, entity, prev, a.copy(attrUpdatingGrants = roles), isJoinTable)

      // End of chain!
      case other =>
        println("UNEXPECTED TREE STRUCTURE:\n" + other + "\n\n" + other.structure)
        unexpected(other)
    }
  }

  // Refactored Handler Methods for Method Size Reduction ................................................

  // Helper methods ................................................

  private def addBackRef(segmentPrefix: String, backRefEntity: String, entity: String): Unit = {
    val fullEntityName     = fullEntity(segmentPrefix, entity)
    val fullBackRefEntity  = fullEntity(segmentPrefix, backRefEntity)
    val curBackRefEntities = backRefs.getOrElse(fullEntityName, Nil)
    backRefs = backRefs + (fullEntityName -> (curBackRefEntities :+ fullBackRefEntity))
  }

  private def addReverseRef(reverseEntity: String, reverseRef: MetaAttribute): Unit = {
    val curReverseEntities = reverseRefs.getOrElse(reverseEntity, Nil)
    reverseRefs = reverseRefs + (reverseEntity -> (curReverseEntities :+ reverseRef))
  }

  private def addManyToManyBridges(joinTable: String, refAttr: String, ref: String, reverseRef: String): Unit = {
    val curJoinTables = ent2joinTables.getOrElse(joinTable, Nil)
    ent2joinTables = ent2joinTables + (ref -> (curJoinTables :+ joinTable))
    val curRefs = joinTable2refs.getOrElse(joinTable, Nil)
    joinTable2refs = joinTable2refs + (joinTable -> (curRefs :+ (refAttr, ref, reverseRef)))
  }

  private def handleValidationCases(
    prev: Tree,
    segmentPrefix: String,
    entity: String,
    a: MetaAttribute,
    cases: List[Case],
    attr: String,
    isJoinTable: Boolean
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
    acc(segmentPrefix, entity, prev, attr1, isJoinTable)
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

  // Access Control Parsing Methods ................................................

  /** Extract action names from role definition traits */
  private def extractActions(actionTraits: List[Init]): List[String] = {
    actionTraits.flatMap {
      case init"$action" if allActions.contains(action.toString) => List(action.toString)
      case _                                                     => Nil
    }.distinct.sorted
  }

  /** Extract roles, updating grants, deleting grants, and entity migration from entity trait definition */
  private def extractEntityRoles(template: Template): (List[String], List[String], List[String], Option[EntityMigration]) = {
    val inits                = template.inits
    var entityRoles          = List.empty[String]
    var entityUpdatingGrants = List.empty[String]
    var entityDeletingGrants = List.empty[String]
    var entityMigration      = Option.empty[EntityMigration]

    inits.foreach {
      case init"Role"   => // Entity is itself a role
      case init"Join"   => // Join table marker
      case init"Remove" => // Entity marked for removal
        entityMigration = Some(EntityMigration.Remove)

      // Extract Rename marker - Scala 3 syntax with parameter
      case Init.After_4_6_0(Type.Apply.After_4_6_0(Type.Name("Rename"), Seq(Type.Name(newName))), _, _) =>
        entityMigration = Some(EntityMigration.Rename(newName))

      // Extract RenameTo* markers - Scala 2.12 compatible dummy markers
      case init"RenameToDummyEntity" =>
        entityMigration = Some(EntityMigration.Rename("Entity"))

      // Extract updating[R] marker traits
      case init"updating[$roleType]" =>
        entityUpdatingGrants = entityUpdatingGrants ++ extractRolesFromType(roleType)

      // Extract deleting[R] marker traits
      case init"deleting[$roleType]" =>
        entityDeletingGrants = entityDeletingGrants ++ extractRolesFromType(roleType)

      case init"$roleName" if roles.exists(_.role == roleName.toString) =>
        // Entity extends a defined role
        entityRoles = entityRoles :+ roleName.toString
      case _                                                            => // Regular entity or unknown trait
    }

    (entityRoles.distinct.sorted, entityUpdatingGrants.distinct.sorted, entityDeletingGrants.distinct.sorted, entityMigration)
  }

  /** Extract roles from type parameter (handles single role and tuple of roles) */
  private def extractRolesFromType(tpe: Type): List[String] = tpe match {
    case Type.Name(roleName) => List(roleName)
    case Type.Tuple(types)   => types.flatMap(extractRolesFromType)
    case _                   => Nil
  }

  /** Extract actions from type parameter (handles single action and tuple of actions) */
  private def extractActionsFromType(tpe: Type): List[String] = {
    tpe match {
      case Type.Name(action) if allActions.contains(action) => List(action)
      case Type.Tuple(types)                                => types.flatMap(extractActionsFromType)
      case _                                                => Nil
    }
  }

  // Authorization Validation Methods ................................................

  /** Validate entity has access to all 5 core CRUD actions through its roles (Layer 1)
   * Only applies to role-restricted entities. Public entities (no roles) skip all validation.
   * Note: rawQuery and rawTransact are escape hatches and not required.
   */
  private def validateEntityActionCoverage(entity: String, entityRoles: List[String], entityActions: List[String]): Unit = {
    // Public entities (no roles) are unrestricted - skip validation
    if (entityRoles.isEmpty) {
      return
    }

    // Role-restricted entities must have all 5 core CRUD actions available
    val missingActions = coreActions.filterNot(entityActions.contains)
    if (missingActions.nonEmpty) {
      val roleDetails = entityRoles.map { roleName =>
        val role = roles.find(_.role == roleName).get
        s"  - $roleName: ${role.actions.mkString(", ")}"
      }.mkString("\n")

      err(
        s"""Authorization error in entity '$entity':
           |Entity roles do not provide access to all 5 required core actions (query, save, insert, update, delete).
           |Missing actions: ${missingActions.mkString(", ")}
           |Available actions from roles: ${if (entityActions.isEmpty) "(none)" else entityActions.mkString(", ")}
           |
           |Entity roles and their actions:
           |$roleDetails
           |
           |To fix: Add role(s) that provide the missing actions. For example:
           |  - Add a role with all core CRUD actions, OR
           |  - Add combination of roles that together provide all 5 core actions
           |
           |Example fix:
           |  trait $entity extends ${(entityRoles :+ "Admin").distinct.sorted.mkString(" with ")}  // assuming Admin has all core actions
           |""".stripMargin
      )
    }
  }

  /** Validate action grants (Layer 2) */
  private def validateEntityGrants(entity: String, entityRoles: List[String], grants: List[String], grantType: String): Unit = {
    grants.foreach { roleName =>
      if (!entityRoles.contains(roleName)) {
        err(
          s"""Authorization error in entity '$entity':
             |Role '$roleName' in '$grantType[$roleName]' grant is not in the entity roles.
             |Entity roles: ${if (entityRoles.isEmpty) "(public entity)" else entityRoles.mkString(", ")}
             |
             |To fix: Add '$roleName' to entity definition:
             |  trait $entity extends ${(entityRoles :+ roleName).distinct.sorted.mkString(" with ")} with $grantType[$roleName]
             |""".stripMargin
        )
      }
    }
  }

  /** Validate attribute restrictions (Layer 3) */
  private def validateAttributeRoleRestrictions(entity: String, attr: String, entityRoles: List[String],
                                                onlyRoles: List[String], excludedRoles: List[String]): Unit = {
    // Check only[R] roles are in entity roles
    onlyRoles.foreach { roleName =>
      if (!entityRoles.contains(roleName)) {
        err(
          s"""Authorization error in attribute '$entity.$attr':
             |Role '$roleName' in '.only[$roleName]' is not in the entity roles.
             |Entity roles: ${if (entityRoles.isEmpty) "(public entity)" else entityRoles.mkString(", ")}
             |
             |To fix: Add '$roleName' to entity definition:
             |  trait $entity extends ${(entityRoles :+ roleName).distinct.sorted.mkString(" with ")}
             |""".stripMargin
        )
      }
    }

    // Check exclude[R] roles are in entity roles
    excludedRoles.foreach { roleName =>
      if (!entityRoles.contains(roleName)) {
        err(
          s"""Authorization error in attribute '$entity.$attr':
             |Role '$roleName' in '.exclude[$roleName]' is not in the entity roles.
             |Entity roles: ${if (entityRoles.isEmpty) "(public entity)" else entityRoles.mkString(", ")}
             |
             |To fix: Add '$roleName' to entity definition:
             |  trait $entity extends ${(entityRoles :+ roleName).distinct.sorted.mkString(" with ")}
             |""".stripMargin
        )
      }
    }

    // Check if using only[R] results in empty role set
    if (onlyRoles.nonEmpty && onlyRoles.forall(!entityRoles.contains(_))) {
      err(
        s"""Authorization error in attribute '$entity.$attr':
           |'.only[${onlyRoles.mkString(", ")}]' would result in no roles having access.
           |None of the specified roles are in entity roles: ${if (entityRoles.isEmpty) "(public entity)" else entityRoles.mkString(", ")}
           |""".stripMargin
      )
    }

    // Check if using exclude[R] results in empty role set
    if (excludedRoles.nonEmpty && entityRoles.nonEmpty && entityRoles.forall(excludedRoles.contains)) {
      err(
        s"""Authorization error in attribute '$entity.$attr':
           |'.exclude[${excludedRoles.mkString(", ")}]' would result in no roles having access.
           |All entity roles are being excluded.
           |""".stripMargin
      )
    }
  }

  /** Validate attribute update grants (Layer 4) */
  private def validateAttributeUpdateGrants(entity: String, attr: String, entityRoles: List[String],
                                            attrUpdatingGrants: List[String], onlyRoles: List[String],
                                            excludedRoles: List[String]): Unit = {
    attrUpdatingGrants.foreach { roleName =>
      // Check if role is in entity roles
      if (!entityRoles.contains(roleName)) {
        err(
          s"""Authorization error in attribute '$entity.$attr':
             |Role '$roleName' in '.updating[$roleName]' is not in the entity roles.
             |Entity roles: ${if (entityRoles.isEmpty) "(public entity)" else entityRoles.mkString(", ")}
             |
             |To fix: Add '$roleName' to entity definition:
             |  trait $entity extends ${(entityRoles :+ roleName).distinct.sorted.mkString(" with ")}
             |""".stripMargin
        )
      }

      // Determine effective roles after restrictions
      val effectiveRoles = if (onlyRoles.nonEmpty) {
        onlyRoles
      } else if (excludedRoles.nonEmpty) {
        entityRoles.filterNot(excludedRoles.contains)
      } else {
        entityRoles
      }

      // Warn if grant is given to a role that doesn't pass restrictions
      if (!effectiveRoles.contains(roleName)) {
        val restrictionMsg = if (onlyRoles.nonEmpty) {
          s"'.only[${onlyRoles.mkString(", ")}]'"
        } else {
          s"'.exclude[${excludedRoles.mkString(", ")}]'"
        }
        err(
          s"""Authorization error in attribute '$entity.$attr':
             |'.updating[$roleName]' grant is ineffective because role '$roleName' is excluded by $restrictionMsg
             |Effective roles after restrictions: ${effectiveRoles.mkString(", ")}
             |
             |The update grant will have no effect because '$roleName' cannot access this attribute.
             |""".stripMargin
        )
      }
    }
  }

  /** Validate action grants have access to all attributes
   * If a role can delete or update an entity, they must be able to access all attributes
   * since these operations affect the entire database row.
   */
  private def validateEntityGrantsAttributeAccess(entity: String, entityRoles: List[String],
                                                  entityUpdatingGrants: List[String],
                                                  entityDeletingGrants: List[String],
                                                  metaAttrs: List[MetaAttribute]): Unit = {
    // Check both updating and deleting grants
    val grantsToCheck = List(
      ("updating", entityUpdatingGrants),
      ("deleting", entityDeletingGrants)
    )

    grantsToCheck.foreach { case (grantType, grantedRoles) =>
      grantedRoles.foreach { roleName =>
        // Check each attribute to see if this role can access it
        metaAttrs.foreach { attr =>
          // Determine if role can access this attribute
          val canAccess = {
            if (attr.onlyRoles.nonEmpty) {
              // Attribute has .only[R] - role must be in the only list
              attr.onlyRoles.contains(roleName)
            } else if (attr.excludedRoles.nonEmpty) {
              // Attribute has .exclude[R] - role must NOT be in the exclude list
              !attr.excludedRoles.contains(roleName)
            } else {
              // No restrictions - role can access
              true
            }
          }

          if (!canAccess) {
            val restrictionMsg = if (attr.onlyRoles.nonEmpty) {
              s".only[${attr.onlyRoles.mkString(", ")}]"
            } else {
              s".exclude[${attr.excludedRoles.mkString(", ")}]"
            }

            val fixOptions = if (attr.onlyRoles.nonEmpty) {
              s"""  - Allow '$roleName' to access '${attr.attribute}': .only[${(attr.onlyRoles :+ roleName).distinct.sorted.mkString(", ")}]
                 |  - Remove '$roleName' from the $grantType grant: with $grantType[${grantedRoles.filterNot(_ == roleName).mkString(", ")}]
                 |  - Remove the attribute restriction: ${attr.attribute} (without .only)""".stripMargin
            } else {
              s"""  - Allow '$roleName' to access '${attr.attribute}': .exclude[${attr.excludedRoles.filterNot(_ == roleName).mkString(", ")}]
                 |  - Remove '$roleName' from the $grantType grant: with $grantType[${grantedRoles.filterNot(_ == roleName).mkString(", ")}]
                 |  - Remove the attribute restriction: ${attr.attribute} (without .exclude)""".stripMargin
            }

            err(
              s"""Authorization error in entity '$entity':
                 |Entity grants $grantType to role '$roleName' but attribute '${attr.attribute}' is restricted with $restrictionMsg
                 |
                 |A role cannot ${if (grantType == "deleting") "delete" else "update"} an entity if it cannot access all attributes,
                 |since ${if (grantType == "deleting") "delete-by-ID removes" else "update operations affect"} the entire database row including all attribute values.
                 |
                 |To fix, choose one option:
                 |$fixOptions
                 |""".stripMargin
            )
          }
        }
      }
    }
  }
}

