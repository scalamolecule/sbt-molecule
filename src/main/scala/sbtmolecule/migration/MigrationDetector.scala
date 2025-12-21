package sbtmolecule.migration

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import scala.collection.mutable.ListBuffer

/**
 * Detects and annotates schema changes between two MetaDomain versions.
 *
 * Migration strategy operates on 3 levels:
 * 1. Segment level: Affects all entities/tables within the segment
 *    - segment extends Remove: DROP all tables with segment prefix
 *    - segment extends Rename("newName"): Rename all tables oldSegment_Entity -> newSegment_Entity
 * 2. Entity level: Affects individual tables
 *    - entity extends Remove: DROP TABLE
 *    - entity extends Rename("newName"): RENAME TABLE
 * 3. Attribute level: Affects individual columns
 *    - attribute.remove: DROP COLUMN
 *    - attribute.rename("newName"): RENAME COLUMN
 *
 * Additions are always implicit (new elements without explicit .remove somewhere).
 * Removals/renames require explicit markers to avoid ambiguity.
 */
object MigrationDetector {

  sealed trait AnnotationResult
  case class Success(annotatedMeta: MetaDomain) extends AnnotationResult
  case class AmbiguityDetected(errorMessage: String) extends AnnotationResult

  /**
   * Annotates the "before" MetaDomain with migration status by comparing with "after".
   *
   * @param metaBefore The original MetaDomain structure (with migration commands)
   * @param metaAfter  The target MetaDomain structure (clean)
   * @return Either an annotated MetaDomain or an error message if ambiguities detected
   */
  def annotateBeforeSafe(metaBefore: MetaDomain, metaAfter: MetaDomain): AnnotationResult = {
    annotateBeforeSafe(metaBefore, metaAfter, None)
  }

  /**
   * Annotates the "before" MetaDomain with migration status by comparing with "after".
   *
   * @param metaBefore The original MetaDomain structure (with migration commands)
   * @param metaAfter  The target MetaDomain structure (clean)
   * @param migrationFileName Optional migration file name for error message
   * @return Either an annotated MetaDomain or an error message if ambiguities detected
   */
  def annotateBeforeSafe(metaBefore: MetaDomain, metaAfter: MetaDomain, migrationFileName: Option[String], migrationFilePath: Option[String] = None): AnnotationResult = {
    annotateBeforeInternal(metaBefore, metaAfter, migrationFileName, migrationFilePath)
  }

  /**
   * Annotates the "before" MetaDomain with migration status by comparing with "after".
   *
   * Throws an exception if segments/entities/attributes disappeared without explicit migration commands.
   *
   * @param metaBefore The original MetaDomain structure (with migration commands)
   * @param metaAfter  The target MetaDomain structure (clean)
   * @return Annotated before-MetaDomain with migration status
   */
  def annotateBefore(metaBefore: MetaDomain, metaAfter: MetaDomain): MetaDomain = {
    annotateBefore(metaBefore, metaAfter, None)
  }

  /**
   * Annotates the "before" MetaDomain with migration status by comparing with "after".
   *
   * Throws an exception if segments/entities/attributes disappeared without explicit migration commands.
   *
   * @param metaBefore The original MetaDomain structure (with migration commands)
   * @param metaAfter  The target MetaDomain structure (clean)
   * @param migrationFileName Optional migration file name for error message
   * @return Annotated before-MetaDomain with migration status
   */
  def annotateBefore(metaBefore: MetaDomain, metaAfter: MetaDomain, migrationFileName: Option[String], migrationFilePath: Option[String] = None): MetaDomain = {
    annotateBeforeInternal(metaBefore, metaAfter, migrationFileName, migrationFilePath) match {
      case Success(annotatedMeta) => annotatedMeta
      case AmbiguityDetected(errorMessage) => throw new Exception(errorMessage)
    }
  }

  private def annotateBeforeInternal(metaBefore: MetaDomain, metaAfter: MetaDomain, migrationFileName: Option[String], migrationFilePath: Option[String]): AnnotationResult = {
    val afterSegmentsMap = metaAfter.segments.map(s => s.segment -> s).toMap

    val allDisappearedSegments = ListBuffer.empty[String]
    val allDisappearedEntities = ListBuffer.empty[String]
    val allDisappearedAttrs    = ListBuffer.empty[String]
    var hasExplicitMigrations  = false

    // Process each segment from before
    for (beforeSegment <- metaBefore.segments) {
      val segmentName = beforeSegment.segment

      // Skip the _enums segment - enums are not real entities/tables and don't need migration tracking
      if (segmentName == "_enums") {
        // Continue to next segment without processing
      } else {

      // Check for segment-level migration
      if (beforeSegment.migration.isDefined) hasExplicitMigrations = true

      beforeSegment.migration match {
        case Some(SegmentMigration.Remove) =>
          // Segment marked for removal - all entities will be dropped
          // This is valid - no error

        case Some(SegmentMigration.Rename(newSegmentName)) =>
          // Segment marked for rename - look for segment with new name
          val afterSegment = afterSegmentsMap.get(newSegmentName)
          afterSegment match {
            case Some(afterSeg) =>
              // Process entities within renamed segment
              val result = processEntitiesInSegment(beforeSegment, afterSeg, newSegmentName)
              allDisappearedEntities ++= result._1
              allDisappearedAttrs ++= result._2
              if (result._3) hasExplicitMigrations = true
            case None =>
              // Renamed segment not found - this is an error
              allDisappearedSegments += (if (segmentName.nonEmpty) segmentName else "(root)")
          }

        case None =>
          // No segment migration - check if segment still exists
          val afterSegment = afterSegmentsMap.get(segmentName)
          afterSegment match {
            case Some(afterSeg) =>
              // Segment exists - process entities within it
              val result = processEntitiesInSegment(beforeSegment, afterSeg, segmentName)
              allDisappearedEntities ++= result._1
              allDisappearedAttrs ++= result._2
              if (result._3) hasExplicitMigrations = true
            case None =>
              // Segment disappeared without migration marker
              // Check if all entities have migration markers
              val entitiesWithoutMigration = beforeSegment.entities.filter { entity =>
                entity.migration.isEmpty || entity.migration.exists {
                  case EntityMigration.Add => true
                  case _ => false
                }
              }

              if (entitiesWithoutMigration.nonEmpty) {
                // Segment disappeared without proper migrations - this is an error
                allDisappearedSegments += (if (segmentName.nonEmpty) segmentName else "(root)")
              }
          }
      }
      } // End of else block for skipping _enums
    }

    // Check for new segments/entities/attributes in metaAfter
    // Also check for explicit migration markers in metaAfter (.remove, .rename)
    var hasNewItems = false
    val beforeSegmentsMap = metaBefore.segments.map(s => s.segment -> s).toMap

    for (afterSegment <- metaAfter.segments) {
      // Skip the _enums segment - enums are not real entities/tables
      if (afterSegment.segment != "_enums") {
        beforeSegmentsMap.get(afterSegment.segment) match {
          case None =>
            // New segment
            hasNewItems = true
          case Some(beforeSegment) =>
          // Check for new entities
          val beforeEntitiesMap = beforeSegment.entities.map(e => e.entity -> e).toMap
          for (afterEntity <- afterSegment.entities) {
            beforeEntitiesMap.get(afterEntity.entity) match {
              case None =>
                // New entity
                hasNewItems = true
              case Some(beforeEntity) =>
                // Check for new attributes
                val beforeAttrsSet = beforeEntity.attributes.map(_.attribute).toSet
                val newAttrs = afterEntity.attributes.filter(a => !beforeAttrsSet.contains(a.attribute))
                if (newAttrs.nonEmpty) {
                  hasNewItems = true
                }
                // Check for attributes with explicit migration markers in current structure
                val attrsWithMigration = afterEntity.attributes.filter(_.migration.isDefined)
                if (attrsWithMigration.nonEmpty) {
                  hasExplicitMigrations = true
                }
            }
          }
        }
      } // End of if block for skipping _enums
    }

    // Build combined error message showing segment, entity, and attribute errors
    if (allDisappearedSegments.nonEmpty || allDisappearedEntities.nonEmpty || allDisappearedAttrs.nonEmpty) {
      val errorParts = List(
        if (allDisappearedSegments.nonEmpty) {
          val disappearedList = allDisappearedSegments.mkString("\n  ")
          Some(s"""The following segments have been removed without extending `Remove` or `Rename("newName")`:\n  $disappearedList""")
        } else None,
        if (allDisappearedEntities.nonEmpty) {
          val disappearedList = allDisappearedEntities.mkString("\n  ")
          Some(s"""The following entities have been removed without extending `Remove` or `Rename("newName")`:\n  $disappearedList""")
        } else None,
        if (allDisappearedAttrs.nonEmpty) {
          val disappearedList = allDisappearedAttrs.mkString("\n  ")
          Some(s"""The following attributes have been removed without calling `.remove` or `.rename("newName")`:\n  $disappearedList""")
        } else None
      ).flatten

      val migrationFileInstruction = migrationFilePath match {
        case Some(filePath) =>
          s"\n\nPlease resolve the ambiguous migration changes in:\n  $filePath\n\nThen run sbt moleculeGen again."
        case None =>
          migrationFileName match {
            case Some(fileName) =>
              s"\n\nPlease add the missing migration commands to ${fileName}_migration.scala and run sbt moleculeGen again."
            case None =>
              ""
          }
      }

      val errorMessage = """-- ERROR: Schema changes detected but explicit migration commands are missing.
        |
        |""".stripMargin + errorParts.mkString("\n\n") + migrationFileInstruction

      AmbiguityDetected(errorMessage)
    } else {
      // Determine migration status
      val status = if (hasExplicitMigrations || hasNewItems) MigrationStatus.HasChanges else MigrationStatus.Clean

      Success(metaBefore.copy(migrationStatus = status))
    }
  }

  /**
   * Process entities within a segment, comparing before and after states.
   * Returns (disappearedEntities, disappearedAttrs, hasExplicitMigrations)
   */
  private def processEntitiesInSegment(
    beforeSegment: MetaSegment,
    afterSegment: MetaSegment,
    targetSegmentName: String // May differ from beforeSegment.segment if segment was renamed
  ): (List[String], List[String], Boolean) = {
    val segmentName = beforeSegment.segment
    val afterEntitiesMap = afterSegment.entities.map(e => e.entity -> e).toMap

    val disappearedEntities = ListBuffer.empty[String]
    val disappearedAttrs = ListBuffer.empty[String]
    var hasExplicitMigrations = false

    // Process each entity in the before segment
    for (beforeEntity <- beforeSegment.entities) {
      val entityName = beforeEntity.entity

      // Check if entity has explicit migration
      if (beforeEntity.migration.isDefined) {
        hasExplicitMigrations = true
      }

      // Determine target entity name after potential segment rename
      val targetEntityBaseName = if (segmentName.nonEmpty) {
        entityName.stripPrefix(segmentName + "_")
      } else {
        entityName
      }

      val targetEntityFullName = if (targetSegmentName.nonEmpty) {
        s"${targetSegmentName}_$targetEntityBaseName"
      } else {
        targetEntityBaseName
      }

      // Look for entity in after structure
      val afterEntity = beforeEntity.migration match {
        case Some(EntityMigration.Rename(newName)) =>
          // Entity was renamed - look for new name with target segment prefix
          val fullNewName = if (targetSegmentName.nonEmpty) s"${targetSegmentName}_$newName" else newName
          afterEntitiesMap.get(fullNewName)
        case Some(EntityMigration.Remove) =>
          // Entity marked for removal
          None
        case _ =>
          // No entity migration - look for same name in target segment
          afterEntitiesMap.get(targetEntityFullName)
      }

      afterEntity match {
        case Some(afterEnt) =>
          // Entity exists - check attributes
          val attrResult = processAttributes(beforeEntity, afterEnt, segmentName)
          disappearedAttrs ++= attrResult._1
          if (attrResult._2) hasExplicitMigrations = true

        case None =>
          // Entity disappeared - check if it has explicit migration command
          beforeEntity.migration match {
            case Some(EntityMigration.Remove) =>
              // Entity is being removed - this is expected and valid
            case Some(EntityMigration.Rename(_)) =>
              // Entity is being renamed but target not found - error
              val entityNameOnly = if (segmentName.nonEmpty) entityName.stripPrefix(segmentName + "_") else entityName
              val prefix = if (segmentName.nonEmpty) s"$segmentName.$entityNameOnly" else entityNameOnly
              disappearedEntities += prefix
            case Some(EntityMigration.Add) =>
              // Entity is marked as Add but disappeared - error
              val entityNameOnly = if (segmentName.nonEmpty) entityName.stripPrefix(segmentName + "_") else entityName
              val prefix = if (segmentName.nonEmpty) s"$segmentName.$entityNameOnly" else entityNameOnly
              disappearedEntities += prefix
            case None =>
              // Entity disappeared without explicit migration - error
              val entityNameOnly = if (segmentName.nonEmpty) entityName.stripPrefix(segmentName + "_") else entityName
              val prefix = if (segmentName.nonEmpty) s"$segmentName.$entityNameOnly" else entityNameOnly
              disappearedEntities += prefix
          }
      }
    }

    (disappearedEntities.toList, disappearedAttrs.toList, hasExplicitMigrations)
  }

  /**
   * Process attributes within an entity, comparing before and after states.
   * Returns (disappearedAttrs, hasExplicitMigrations)
   */
  private def processAttributes(
    beforeEntity: MetaEntity,
    afterEntity: MetaEntity,
    originalSegmentName: String
  ): (List[String], Boolean) = {
    // Filter out auto-generated id attributes
    val beforeAttrs = beforeEntity.attributes.filterNot(_.attribute == "id")
    val afterAttrs  = afterEntity.attributes.filterNot(_.attribute == "id")

    val beforeNames = beforeAttrs.map(_.attribute).toSet
    val afterNames  = afterAttrs.map(_.attribute).toSet

    // Collect attributes with explicit migration commands
    val explicitlyMigrated = beforeAttrs.collect {
      case attr if attr.migration.isDefined => attr.attribute
    }.toSet

    val hasExplicitMigrations = explicitlyMigrated.nonEmpty

    // Detect dangerous name swaps
    detectNameSwaps(beforeAttrs, afterAttrs, beforeEntity, originalSegmentName)

    // Detect type changes (disallowed)
    detectTypeChanges(beforeAttrs, afterAttrs, beforeEntity, originalSegmentName)

    // Detect option changes (.index and .owner)
    detectOptionChanges(beforeAttrs, afterAttrs, beforeEntity, originalSegmentName)

    // Detect disappeared attributes
    val disappearedSet = beforeNames -- afterNames -- explicitlyMigrated
    val disappeared = beforeAttrs.filter(attr => disappearedSet.contains(attr.attribute))

    val disappearedAttrs = if (disappeared.nonEmpty) {
      // Build error message with proper segment.entity prefix
      val entityName = beforeEntity.entity
      val entityNameOnly = if (originalSegmentName.nonEmpty) {
        entityName.stripPrefix(originalSegmentName + "_")
      } else {
        entityName
      }
      val prefix = if (originalSegmentName.nonEmpty) {
        s"$originalSegmentName.$entityNameOnly"
      } else {
        entityNameOnly
      }
      disappeared.map(attr => s"$prefix.${attr.attribute}").toList
    } else {
      List.empty[String]
    }

    (disappearedAttrs, hasExplicitMigrations)
  }

  /**
   * Detects dangerous name swap scenarios where two attributes are being renamed to each other's names.
   * This is dangerous because it can cause data corruption if code runs between migration steps.
   *
   * Example of dangerous swap:
   *   val email = oneString.rename("phone")  // email -> phone
   *   val phone = oneString.rename("email")  // phone -> email
   *
   * Throws an exception if a name swap is detected.
   */
  private def detectNameSwaps(
    beforeAttrs: List[MetaAttribute],
    afterAttrs: List[MetaAttribute],
    beforeEntity: MetaEntity,
    originalSegmentName: String
  ): Unit = {
    // Collect all renames: oldName -> newName
    val renames = beforeAttrs.collect {
      case attr if attr.migration.exists {
        case AttrMigration.Rename(_) => true
        case _ => false
      } =>
        val newName = attr.migration.get.asInstanceOf[AttrMigration.Rename].newName
        (attr.attribute, newName)
    }.toMap

    // Detect cycles using depth-first search
    // A cycle exists if following the rename chain leads back to the starting point
    def findCycle(start: String, visited: Set[String] = Set.empty): Option[List[String]] = {
      def follow(current: String, path: List[String]): Option[List[String]] = {
        renames.get(current) match {
          case None => None // Chain ends, no cycle
          case Some(next) =>
            if (next == start && path.nonEmpty) {
              // Found a cycle back to start
              Some(path.reverse)
            } else if (path.contains(next)) {
              // Found a cycle, but not back to start
              None
            } else {
              // Continue following the chain
              follow(next, next :: path)
            }
        }
      }
      follow(start, List(start))
    }

    // Find all cycles - each attribute in a cycle will find the same cycle
    val allCycles = renames.keys.flatMap { start =>
      findCycle(start)
    }

    // Normalize and deduplicate cycles
    val cycles = allCycles.map { cycle =>
      // Normalize: start with alphabetically smallest name
      val minIdx = cycle.indexOf(cycle.min)
      cycle.drop(minIdx) ++ cycle.take(minIdx)
    }.toSet

    // For backwards compatibility, extract 2-way swaps from cycles
    val swaps = cycles.collect {
      case cycle if cycle.size == 2 => (cycle.head, cycle(1))
    }

    // Report any cycles found (2-way swaps or N-way cycles)
    if (cycles.nonEmpty) {
      val entityName = beforeEntity.entity
      val entityNameOnly = if (originalSegmentName.nonEmpty) {
        entityName.stripPrefix(originalSegmentName + "_")
      } else {
        entityName
      }
      val prefix = if (originalSegmentName.nonEmpty) {
        s"$originalSegmentName.$entityNameOnly"
      } else {
        entityNameOnly
      }

      // Separate 2-way swaps from longer cycles
      val twoWaySwaps = cycles.filter(_.size == 2)
      val longerCycles = cycles.filter(_.size > 2)

      val errorParts = scala.collection.mutable.ListBuffer.empty[String]
      errorParts += "-- ERROR: Dangerous attribute rename cycle detected."
      errorParts += ""

      if (twoWaySwaps.nonEmpty) {
        errorParts += "2-way swaps:"
        twoWaySwaps.foreach { cycle =>
          errorParts += s"  - $prefix.${cycle.head} <-> $prefix.${cycle(1)}"
        }
        if (longerCycles.nonEmpty) errorParts += ""
      }

      if (longerCycles.nonEmpty) {
        errorParts += "Cyclical renames:"
        longerCycles.foreach { cycle =>
          val cycleStr = cycle.map(name => s"$prefix.$name").mkString(" -> ")
          errorParts += s"  - $cycleStr -> $prefix.${cycle.head}"
        }
        errorParts += ""
      }

      errorParts += "This is not allowed because it can cause data corruption during migration."
      errorParts += "You must break the cycle over multiple migrations using temporary names:"
      errorParts += ""

      // Show example for first cycle (2-way or N-way)
      val exampleCycle = cycles.head
      if (exampleCycle.size == 2) {
        val name1 = exampleCycle.head
        val name2 = exampleCycle(1)
        errorParts += "Migration 1: Rename to temporary names"
        errorParts += s"""  val $name1 = oneType.rename("${name1}_temp")"""
        errorParts += s"""  val $name2 = oneType.rename("${name2}_temp")"""
        errorParts += ""
        errorParts += "Migration 2: Rename to final names"
        errorParts += s"""  val ${name1}_temp = oneType.rename("$name2")"""
        errorParts += s"""  val ${name2}_temp = oneType.rename("$name1")"""
      } else {
        val first = exampleCycle.head
        errorParts += "Migration 1: Rename first attribute to temporary name"
        errorParts += s"""  val $first = oneType.rename("${first}_temp")"""
        errorParts += ""
        errorParts += "Migration 2: Perform the remaining renames"
        exampleCycle.indices.foreach { i =>
          val from = exampleCycle(i)
          val to = exampleCycle((i + 1) % exampleCycle.size)
          if (i == 0) {
            // Skip first rename, already done with temp name
          } else if (i == exampleCycle.size - 1) {
            errorParts += s"""  val $from = oneType.rename("${first}_temp")"""
          } else {
            errorParts += s"""  val $from = oneType.rename("$to")"""
          }
        }
        errorParts += ""
        errorParts += "Migration 3: Rename temporary to final name"
        val second = exampleCycle(1)
        errorParts += s"""  val ${first}_temp = oneType.rename("$second")"""
      }

      throw new Exception(errorParts.mkString("\n"))
    }
  }

  /**
   * Detects type changes for attributes with the same name.
   * Type changes are disallowed because they fundamentally change the semantics of the attribute.
   *
   * Example of disallowed type change:
   *   // Before:
   *   val age = oneInt
   *
   *   // After:
   *   val age = oneString  // ERROR: Type changed from Int to String
   *
   * Throws an exception if a type change is detected.
   */
  private def detectTypeChanges(
    beforeAttrs: List[MetaAttribute],
    afterAttrs: List[MetaAttribute],
    beforeEntity: MetaEntity,
    originalSegmentName: String
  ): Unit = {
    val beforeAttrMap = beforeAttrs.map(a => a.attribute -> a).toMap
    val afterAttrMap = afterAttrs.map(a => a.attribute -> a).toMap

    // Find attributes that exist in both before and after
    val commonAttrNames = beforeAttrMap.keySet.intersect(afterAttrMap.keySet)

    // Check for type or cardinality changes
    val typeChanges = commonAttrNames.flatMap { attrName =>
      val beforeAttr = beforeAttrMap(attrName)
      val afterAttr = afterAttrMap(attrName)

      // Check if type changed
      val typeChanged = beforeAttr.baseTpe != afterAttr.baseTpe

      // Check if cardinality changed (comparing Value types)
      val cardinalityChanged = cardinalityName(beforeAttr.value) != cardinalityName(afterAttr.value)

      // Check if relationship target changed
      val refTargetChanged = (beforeAttr.ref, afterAttr.ref) match {
        case (Some(beforeRef), Some(afterRef)) => beforeRef != afterRef
        case (Some(_), None) | (None, Some(_)) => true // Changed from/to relationship
        case (None, None) => false
      }

      if (typeChanged || cardinalityChanged || refTargetChanged) {
        val beforeType = if (beforeAttr.ref.isDefined) {
          s"manyToOne[${beforeAttr.ref.get}]"
        } else {
          s"${cardinalityName(beforeAttr.value)}${beforeAttr.baseTpe}"
        }
        val afterType = if (afterAttr.ref.isDefined) {
          s"manyToOne[${afterAttr.ref.get}]"
        } else {
          s"${cardinalityName(afterAttr.value)}${afterAttr.baseTpe}"
        }
        Some((attrName, beforeType, afterType))
      } else {
        None
      }
    }

    if (typeChanges.nonEmpty) {
      val entityName = beforeEntity.entity
      val entityNameOnly = if (originalSegmentName.nonEmpty) {
        entityName.stripPrefix(originalSegmentName + "_")
      } else {
        entityName
      }
      val prefix = if (originalSegmentName.nonEmpty) {
        s"$originalSegmentName.$entityNameOnly"
      } else {
        entityNameOnly
      }

      val changeDescriptions = typeChanges.map { case (attrName, beforeType, afterType) =>
        s"  - $prefix.$attrName: $beforeType → $afterType"
      }.mkString("\n")

      val isRelationship = typeChanges.head._2.startsWith("manyToOne[") || typeChanges.head._3.startsWith("manyToOne[")

      val instructions = if (isRelationship) {
        s"""Type and cardinality changes fundamentally alter the semantics of your data model
           |and cannot be automatically migrated.
           |
           |For relationships, changing the target entity (e.g., Customer → User) requires:
           |
           |1. Create a new relationship with the desired target type:
           |   val ${typeChanges.head._1}New = ${typeChanges.head._3}
           |
           |2. Write custom code to migrate the foreign key references:
           |   // Example migration code in your application:
           |   MyEntity.${typeChanges.head._1}.${typeChanges.head._1}New.query.get.foreach { case (id, oldRef) =>
           |     val newRef = mapOldToNew(oldRef)  // Your mapping logic
           |     MyEntity(id).${typeChanges.head._1}New(newRef).update.transact
           |   }
           |
           |3. Verify that all foreign keys have been correctly migrated
           |
           |4. Remove the old relationship:
           |   val ${typeChanges.head._1} = ${typeChanges.head._2}.remove
           |
           |5. (Optional) Rename the new relationship to the old name:
           |   val ${typeChanges.head._1}New = ${typeChanges.head._3}.rename("${typeChanges.head._1}")
           |""".stripMargin
      } else {
        s"""Type and cardinality changes fundamentally alter the semantics of your data model
           |and cannot be automatically migrated.
           |
           |To migrate data between incompatible types, follow this process:
           |
           |1. Create a new attribute with the desired type/cardinality:
           |   val ${typeChanges.head._1}New = ${typeChanges.head._3}
           |
           |2. Write custom code to convert and save data from the old attribute to the new one:
           |   // Example migration code in your application:
           |   MyEntity.${typeChanges.head._1}.${typeChanges.head._1}New.query.get.foreach { case (id, oldValue) =>
           |     val newValue = convertOldToNew(oldValue)  // Your conversion logic
           |     MyEntity(id).${typeChanges.head._1}New(newValue).update.transact
           |   }
           |
           |3. Verify that all data has been correctly converted:
           |   // Query the new attribute and validate the results
           |   MyEntity.${typeChanges.head._1}New.query.get
           |
           |4. Remove the old attribute:
           |   val ${typeChanges.head._1} = ${typeChanges.head._2}.remove
           |
           |5. (Optional) Rename the new attribute to the old name:
           |   val ${typeChanges.head._1}New = ${typeChanges.head._3}.rename("${typeChanges.head._1}")
           |""".stripMargin
      }

      throw new Exception(
        s"""-- ERROR: Attribute type/cardinality changes are not allowed.
           |
           |The following attributes have changed their type or cardinality:
           |$changeDescriptions
           |
           |$instructions""".stripMargin
      )
    }
  }

  /**
   * Detects changes to .index and .owner options on attributes.
   * These changes require SQL migration (CREATE/DROP INDEX, ALTER FOREIGN KEY).
   *
   * Logs warnings but doesn't throw - these are non-breaking changes.
   */
  private def detectOptionChanges(
    beforeAttrs: List[MetaAttribute],
    afterAttrs: List[MetaAttribute],
    beforeEntity: MetaEntity,
    originalSegmentName: String
  ): Unit = {
    val beforeAttrMap = beforeAttrs.map(a => a.attribute -> a).toMap
    val afterAttrMap = afterAttrs.map(a => a.attribute -> a).toMap

    // Find attributes that exist in both before and after
    val commonAttrNames = beforeAttrMap.keySet.intersect(afterAttrMap.keySet)

    val optionChanges = commonAttrNames.flatMap { attrName =>
      val beforeAttr = beforeAttrMap(attrName)
      val afterAttr = afterAttrMap(attrName)

      val beforeHasIndex = beforeAttr.options.contains("index")
      val afterHasIndex = afterAttr.options.contains("index")
      val beforeHasOwner = beforeAttr.options.contains("owner")
      val afterHasOwner = afterAttr.options.contains("owner")

      val changes = List(
        if (beforeHasIndex != afterHasIndex) {
          Some(("index", beforeHasIndex, afterHasIndex))
        } else None,
        if (beforeHasOwner != afterHasOwner) {
          Some(("owner", beforeHasOwner, afterHasOwner))
        } else None
      ).flatten

      if (changes.nonEmpty) {
        Some((attrName, changes))
      } else {
        None
      }
    }

    if (optionChanges.nonEmpty) {
      val entityName = beforeEntity.entity
      val entityNameOnly = if (originalSegmentName.nonEmpty) {
        entityName.stripPrefix(originalSegmentName + "_")
      } else {
        entityName
      }
      val prefix = if (originalSegmentName.nonEmpty) {
        s"$originalSegmentName.$entityNameOnly"
      } else {
        entityNameOnly
      }

      val messages = optionChanges.flatMap { case (attrName, changes) =>
        changes.map { case (option, hadIt, hasIt) =>
          val action = if (hasIt) "added" else "removed"
          s"  - $prefix.$attrName: .$option $action"
        }
      }

      // Just log for now - we'll generate SQL for these changes
      println(s"""-- INFO: Detected option changes that will require schema migration:
         |${messages.mkString("\n")}
         |""".stripMargin)
    }
  }

  /**
   * Gets a human-readable cardinality name from a Value type.
   */
  private def cardinalityName(value: Value): String = {
    value match {
      case _: OneValue => "one"
      case _: SetValue => "set"
      case _: SeqValue => "seq"
      case _: MapValue => "map"
    }
  }

  /**
   * Generates SQL migration statements from annotated before-MetaDomain.
   *
   * Order of operations:
   * 1. Segment-level renames/removes (affects all tables in segment)
   * 2. Entity-level renames/removes (individual tables)
   * 3. Attribute-level changes (columns)
   */
  def generateSQL(beforeDomain: MetaDomain): List[String] = {
    val segmentMigrations = for {
      segment <- beforeDomain.segments if segment.migration.isDefined
    } yield {
      segment.migration match {
        case Some(SegmentMigration.Rename(newSegmentName)) =>
          // Rename all tables in this segment: oldSegment_Entity -> newSegment_Entity
          segment.entities.map { entity =>
            val oldTableName = entity.entity
            val entityBaseName = if (segment.segment.nonEmpty) {
              oldTableName.stripPrefix(segment.segment + "_")
            } else {
              oldTableName
            }
            val newTableName = if (newSegmentName.nonEmpty) {
              s"${newSegmentName}_$entityBaseName"
            } else {
              entityBaseName
            }
            s"ALTER TABLE $oldTableName RENAME TO $newTableName;"
          }

        case Some(SegmentMigration.Remove) =>
          // Drop all tables in this segment
          segment.entities.map { entity =>
            s"DROP TABLE ${entity.entity};"
          }

        case None => List.empty
      }
    }

    val entityMigrations = for {
      segment <- beforeDomain.segments
      entity <- segment.entities if entity.migration.isDefined
      // Skip entity migrations if segment is being removed
      if segment.migration != Some(SegmentMigration.Remove)
      // Determine table names: account for segment rename
      (oldTableName, targetSegmentName) = segment.migration match {
        case Some(SegmentMigration.Rename(newSegmentName)) =>
          // Segment was renamed, so entity table was already renamed by segment migration
          val entityBaseName = if (segment.segment.nonEmpty) {
            entity.entity.stripPrefix(segment.segment + "_")
          } else {
            entity.entity
          }
          val renamedTableName = if (newSegmentName.nonEmpty) {
            s"${newSegmentName}_$entityBaseName"
          } else {
            entityBaseName
          }
          (renamedTableName, newSegmentName)
        case _ =>
          // No segment rename, use original table name
          (entity.entity, segment.segment)
      }
    } yield {
      entity.migration match {
        case Some(EntityMigration.Rename(newName)) =>
          val newTableName = if (targetSegmentName.nonEmpty) {
            s"${targetSegmentName}_$newName"
          } else {
            newName
          }
          s"ALTER TABLE $oldTableName RENAME TO $newTableName;"
        case Some(EntityMigration.Remove) =>
          s"DROP TABLE $oldTableName;"
        case _ => ""
      }
    }

    val attrMigrations = for {
      segment <- beforeDomain.segments
      entity <- segment.entities
      // Determine table name: account for both segment and entity renames
      tableName = {
        val entityBaseName = if (segment.segment.nonEmpty) {
          entity.entity.stripPrefix(segment.segment + "_")
        } else {
          entity.entity
        }

        // Apply segment rename if present
        val segmentName = segment.migration match {
          case Some(SegmentMigration.Rename(newName)) => newName
          case Some(SegmentMigration.Remove) => segment.segment // Won't be used since table is dropped
          case None => segment.segment
        }

        // Apply entity rename if present (and segment not being removed)
        val finalEntityName = entity.migration match {
          case Some(EntityMigration.Rename(newName)) if segment.migration != Some(SegmentMigration.Remove) => newName
          case Some(EntityMigration.Remove) => entityBaseName // Won't be used since table is dropped
          case _ => entityBaseName
        }

        if (segmentName.nonEmpty) s"${segmentName}_$finalEntityName" else finalEntityName
      }
      // Skip if segment or entity is being removed
      if segment.migration != Some(SegmentMigration.Remove) && entity.migration != Some(EntityMigration.Remove)
      attr <- entity.attributes if attr.migration.isDefined
    } yield {
      attr.migration match {
        case Some(AttrMigration.Rename(newName)) => s"ALTER TABLE $tableName RENAME COLUMN ${attr.attribute} TO $newName;"
        case Some(AttrMigration.Add)             => s"ALTER TABLE $tableName ADD COLUMN ${attr.attribute} ${sqlType(attr)};"
        case Some(AttrMigration.Remove)          => s"ALTER TABLE $tableName DROP COLUMN ${attr.attribute};"
        case None                                => ""
      }
    }

    // Flatten segment migrations and combine with entity/attr migrations
    segmentMigrations.flatten ++ entityMigrations ++ attrMigrations
  }

  /**
   * Maps Molecule types to SQL column types (simplified for now)
   */
  private def sqlType(attr: MetaAttribute): String = {
    // Todo: use custom column properties (like refined type) if defined by user
    // Todo: needs to be database specific, e.g. Postgresql vs. H2 vs. Mysql etc. - see db.sqlDialect.*
    attr.baseTpe match {
      case "String"  => "VARCHAR(255)"
      case "Int"     => "INT"
      case "Long"    => "BIGINT"
      case "Double"  => "DOUBLE"
      case "Boolean" => "BOOLEAN"
      case _         => "VARCHAR(255)" // Default
    }
  }
}
