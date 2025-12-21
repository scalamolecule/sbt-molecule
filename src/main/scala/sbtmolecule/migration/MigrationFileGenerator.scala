package sbtmolecule.migration

import molecule.base.metaModel.*
import molecule.core.dataModel._

/**
 * Generates migration helper MetaDomains that guide users through resolving ambiguous schema changes.
 *
 * When attributes/entities/segments disappear without explicit migration commands,
 * this builds a MetaDomain representing the migration structure with commented options
 * for the user to choose from.
 */
object MigrationFileGenerator {

  /**
   * Builds a migration MetaDomain from disappeared items.
   *
   * The migration MetaDomain contains:
   * - Entities with "Migrations" suffix (e.g., PersonMigrations)
   * - Attributes from disappeared items with .remove migration markers
   * - These can be changed to .rename("newName") by the user
   *
   * @param metaBefore The before structure
   * @param metaAfter The after structure
   * @param migrationDomainName Name for the migration domain (e.g., "MyDomain_migration_1")
   * @return MetaDomain representing the migration structure
   */
  def migrationMetaDomain(
    metaBefore: MetaDomain,
    metaAfter: MetaDomain,
    migrationDomainName: String
  ): MetaDomain = {

    val (disappearedSegments, disappearedEntities, disappearedAttrs) =
      extractDisappearedItems(metaBefore, metaAfter)

    // Group disappeared attributes by segment and entity
    val attrsBySegmentAndEntity = disappearedAttrs.groupBy(attr => (attr.segment, attr.entity))

    // Build migration segments
    val migrationSegments = attrsBySegmentAndEntity.map { case ((segmentName, entityName), attrs) =>
      val migrationEntityName = s"${entityName}Migrations"

      // Create MetaAttributes for each disappeared attribute with .remove marker
      val migrationAttrs = attrs.flatMap { disappearedAttr =>
        // Find the original attribute from metaBefore to get its full metadata
        findAttribute(metaBefore, segmentName, entityName, disappearedAttr.attribute).map { originalAttr =>
          // Mark with Remove migration - user can change this to Rename
          originalAttr.copy(migration = Some(AttrMigration.Remove))
        }
      }

      // Add implicit id attribute as first attribute
      val idAttr = MetaAttribute(
        attribute = "id",
        value = OneValue,
        baseTpe = "ID"
      )

      // Create migration entity with id + migration attributes
      val migrationEntity = MetaEntity(
        entity = migrationEntityName,
        attributes = idAttr :: migrationAttrs.toList
      )

      // Create segment containing the migration entity
      MetaSegment(
        segment = segmentName,
        entities = List(migrationEntity)
      )
    }.toList

    // Build the migration MetaDomain
    // Use the same package as the after structure
    MetaDomain(
      pkg = metaAfter.pkg,
      domain = migrationDomainName,
      segments = migrationSegments,
      migrationStatus = MigrationStatus.Clean
    )
  }

  /**
   * Generates Scala source code from a migration MetaDomain.
   *
   * @param migrationMeta The migration MetaDomain
   * @param baseDomainTraitName The name of the base domain trait to extend (e.g., "ImplicitRename_2")
   * @param metaBefore The before structure (to get possible renames)
   * @param metaAfter The after structure (to get possible renames)
   * @return Scala source code as a string
   */
  def migrationSource(
    migrationMeta: MetaDomain,
    baseDomainTraitName: String,
    metaBefore: MetaDomain,
    metaAfter: MetaDomain
  ): String = {
    val migrationTraitName = migrationMeta.domain

    // Get disappeared items to find possible renames
    val (_, _, disappearedAttrs) = extractDisappearedItems(metaBefore, metaAfter)
    val possibleRenamesMap = disappearedAttrs.map(item =>
      (item.entity, item.attribute, item.possibleRenames)
    ).groupBy(_._1).map { case (entityName, items) =>
      entityName -> items.map(t => t._2 -> t._3).toMap
    }

    // Generate trait bodies for each entity
    val entityTraits = migrationMeta.segments.flatMap { segment =>
      segment.entities.map { entity =>
        val entityName = entity.entity
        val baseEntityName = if (entityName.endsWith("Migrations")) {
          entityName.stripSuffix("Migrations")
        } else {
          entityName
        }

        // Generate attribute lines
        val attrLines = entity.attributes
          .filterNot(_.attribute == "id") // Skip id
          .flatMap { attr =>
            val attrName = attr.attribute
            val attrType = cardinalityAndType(attr)

            // Get possible renames for this attribute
            val possibleRenames = possibleRenamesMap
              .get(baseEntityName)
              .flatMap(_.get(attrName))
              .getOrElse(List.empty)

            // Generate lines based on migration marker
            attr.migration match {
              case Some(AttrMigration.Remove) =>
                // Generate both remove and becomes options UNCOMMENTED
                // This forces a compilation error until user chooses one and comments out the other
                val removeLine = s"    val $attrName = $attrType.remove // if removed"
                val becomesLine = s"    val $attrName = $attrType.becomes() // if renamed: add new attribute like .becomes(otherAttr)"
                List(removeLine, becomesLine)

              case Some(AttrMigration.Rename(newName)) =>
                // Already resolved - show the becomes
                List(s"    val $attrName = $attrType.becomes($newName)")

              case _ =>
                List.empty
            }
          }.mkString("\n")

        s"""
           |  trait $entityName extends $baseEntityName {
           |$attrLines
           |  }""".stripMargin
      }
    }.mkString("\n")

    s"""trait $migrationTraitName extends $baseDomainTraitName with DomainStructure {
       |
       |  // Please choose intended migration commands:
       |  // (comment-out or delete unwanted option lines)
       |$entityTraits
       |}""".stripMargin
  }

  /**
   * Converts MetaAttribute to its cardinality + type representation (e.g., "oneString", "manyInt")
   */
  private def cardinalityAndType(attr: MetaAttribute): String = {
    val cardPrefix = cardinalityPrefix(attr.value)
    val tpe = attr.baseTpe.head.toString.toUpperCase + attr.baseTpe.tail // Capitalize first letter
    s"$cardPrefix$tpe"
  }

  /**
   * Finds an attribute in the MetaDomain by segment, entity, and attribute name.
   */
  private def findAttribute(
    metaDomain: MetaDomain,
    segmentName: String,
    entityName: String,
    attrName: String
  ): Option[MetaAttribute] = {
    for {
      segment <- metaDomain.segments.find(_.segment == segmentName)
      entity <- segment.entities.find(_.entity == entityName)
      attr <- entity.attributes.find(_.attribute == attrName)
    } yield attr
  }

  case class DisappearedItem(
    segment: String,
    entity: String,
    attribute: String,
    possibleRenames: List[String] = List.empty // Potential new attribute names (for future use)
  )

  /**
   * Extracts disappeared items from MetaDomain comparison.
   * This analyzes the before/after to determine what needs migration helpers.
   */
  def extractDisappearedItems(
    metaBefore: MetaDomain,
    metaAfter: MetaDomain
  ): (List[String], List[String], List[DisappearedItem]) = {

    val afterSegmentsMap = metaAfter.segments.map(s => s.segment -> s).toMap

    var disappearedSegments = List.empty[String]
    var disappearedEntities = List.empty[String]
    var disappearedAttrs = List.empty[DisappearedItem]

    // Process each segment from before
    for (beforeSegment <- metaBefore.segments) {
      val segmentName = beforeSegment.segment

      // Check for segment-level changes
      beforeSegment.migration match {
        case Some(_) =>
          // Has explicit migration marker - no helper needed

        case None =>
          val afterSegment = afterSegmentsMap.get(segmentName)
          afterSegment match {
            case Some(afterSeg) =>
              // Segment exists - check entities
              val (entities, attrs) = processEntitiesForMigration(beforeSegment, afterSeg)
              disappearedEntities ++= entities
              disappearedAttrs ++= attrs

            case None =>
              // Segment disappeared without marker
              val entitiesWithoutMigration = beforeSegment.entities.filter(_.migration.isEmpty)
              if (entitiesWithoutMigration.nonEmpty) {
                disappearedSegments :+= (if (segmentName.nonEmpty) segmentName else "(root)")
              }
          }
      }
    }

    (disappearedSegments, disappearedEntities, disappearedAttrs)
  }

  private def processEntitiesForMigration(
    beforeSegment: MetaSegment,
    afterSegment: MetaSegment
  ): (List[String], List[DisappearedItem]) = {

    val segmentName = beforeSegment.segment
    val afterEntitiesMap = afterSegment.entities.map(e => e.entity -> e).toMap

    var disappearedEntities = List.empty[String]
    var disappearedAttrs = List.empty[DisappearedItem]

    for (beforeEntity <- beforeSegment.entities) {
      val entityName = beforeEntity.entity

      beforeEntity.migration match {
        case Some(_) =>
          // Has explicit migration - no helper needed

        case None =>
          val afterEntity = afterEntitiesMap.get(entityName)
          afterEntity match {
            case Some(afterEnt) =>
              // Entity exists - check attributes
              val attrs = extractDisappearedAttributes(beforeEntity, afterEnt, segmentName)
              disappearedAttrs ++= attrs

            case None =>
              // Entity disappeared without marker
              val entityNameOnly = if (segmentName.nonEmpty) entityName.stripPrefix(segmentName + "_") else entityName
              val prefix = if (segmentName.nonEmpty) s"$segmentName.$entityNameOnly" else entityNameOnly
              disappearedEntities :+= prefix
          }
      }
    }

    (disappearedEntities, disappearedAttrs)
  }

  private def extractDisappearedAttributes(
    beforeEntity: MetaEntity,
    afterEntity: MetaEntity,
    segmentName: String
  ): List[DisappearedItem] = {

    val beforeAttrs = beforeEntity.attributes.filterNot(_.attribute == "id")
    val afterAttrs = afterEntity.attributes.filterNot(_.attribute == "id")

    val beforeNames = beforeAttrs.map(_.attribute).toSet
    val afterNames = afterAttrs.map(_.attribute).toSet

    val explicitlyMigrated = beforeAttrs.collect {
      case attr if attr.migration.isDefined => attr.attribute
    }.toSet

    val disappearedSet = beforeNames -- afterNames -- explicitlyMigrated
    val disappeared = beforeAttrs.filter(attr => disappearedSet.contains(attr.attribute))

    // Detect newly added attributes as possible renames
    val addedNames = afterNames -- beforeNames
    val addedAttrs = afterAttrs.filter(attr => addedNames.contains(attr.attribute))

    val entityName = beforeEntity.entity
    val entityNameOnly = if (segmentName.nonEmpty) {
      entityName.stripPrefix(segmentName + "_")
    } else {
      entityName
    }

    disappeared.map { attr =>
      // Find attributes with matching type that were added (potential renames)
      val possibleRenames = addedAttrs
        .filter(a => sameType(attr, a))
        .map(_.attribute)
        .toList

      DisappearedItem(
        segment = segmentName,
        entity = entityNameOnly,
        attribute = attr.attribute,
        possibleRenames = possibleRenames
      )
    }.toList
  }

  /**
   * Checks if two attributes have the same type (cardinality + baseTpe).
   */
  private def sameType(attr1: MetaAttribute, attr2: MetaAttribute): Boolean = {
    cardinalityPrefix(attr1.value) == cardinalityPrefix(attr2.value) &&
    attr1.baseTpe == attr2.baseTpe
  }

  /**
   * Gets the cardinality prefix for an attribute value.
   */
  private def cardinalityPrefix(value: Value): String = {
    value match {
      case _: OneValue => "one"
      case _: SetValue => "many"
      case _: SeqValue => "many"
      case _: MapValue => "map"
    }
  }

  /**
   * Merges migration commands from a migration structure into the before structure.
   * The migration structure contains traits with migration markers (.remove, .rename, .becomes).
   * This extracts those markers and applies them to the corresponding attributes in before.
   *
   * Migration entities are named "EntityMigrations" and we need to map them back to "Entity".
   */
  def applyMigrationCommands(before: MetaDomain, migrationDomain: MetaDomain): MetaDomain = {
    // Build a map of entity -> attribute -> migration from the migration domain
    // Strip "Migrations" suffix from entity names to match original entity names
    val migrationMap: Map[String, Map[String, AttrMigration]] =
      migrationDomain.segments.flatMap { segment =>
        segment.entities.flatMap { entity =>
          // Remove "Migrations" suffix to get the original entity name
          val originalEntityName = if (entity.entity.endsWith("Migrations")) {
            entity.entity.stripSuffix("Migrations")
          } else {
            entity.entity
          }

          entity.attributes
            .filter(_.migration.isDefined)
            .map { attr =>
              (originalEntityName, attr.attribute, attr.migration.get)
            }
        }
      }.groupBy(_._1)
        .map { case (entityName, attrList) =>
          entityName -> attrList.map { case (_, attrName, migration) => attrName -> migration }.toMap
        }

    // Apply migrations to the before structure
    before.copy(
      segments = before.segments.map { segment =>
        segment.copy(
          entities = segment.entities.map { entity =>
            migrationMap.get(entity.entity) match {
              case Some(attrMigrations) =>
                entity.copy(
                  attributes = entity.attributes.map { attr =>
                    attrMigrations.get(attr.attribute) match {
                      case Some(migration) => attr.copy(migration = Some(migration))
                      case None => attr
                    }
                  }
                )
              case None => entity
            }
          }
        )
      }
    )
  }
}
