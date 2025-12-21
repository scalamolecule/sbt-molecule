package sbtmolecule.migration

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.*
import scala.collection.mutable.ListBuffer

/**
 * Generates SQL migration scripts (ALTER TABLE statements) based on annotated MetaDomain changes.
 *
 * Works with the annotated "before" MetaDomain from MigrationDetector which contains:
 * - Attribute migrations: Add, Remove, Rename
 * - Entity migrations: Add, Remove, Rename
 * - Segment migrations: Remove, Rename
 */
object MigrationSqlGenerator {

  /**
   * Generate Flyway migration SQL for a specific database dialect.
   *
   * @param metaBefore Annotated MetaDomain with migration markers
   * @param metaAfter  New MetaDomain (clean, no migration markers)
   * @param dialect    SQL dialect (H2, PostgreSQL, MySQL, etc.)
   * @return SQL migration script with ALTER TABLE statements
   */
  def generateMigrationSql(
    metaBefore: MetaDomain,
    metaAfter: MetaDomain,
    dialect: Dialect
  ): String = {
    val statements = ListBuffer.empty[String]

    // Build maps for quick lookup
    val afterSegmentsMap = metaAfter.segments.map(s => s.segment -> s).toMap
    val afterEntitiesMap = metaAfter.segments.flatMap(_.entities).map(e => e.entity -> e).toMap

    // Get general custom column properties for this specific database
    val generalPropsForDb = metaAfter.generalDbColumnProps.getOrElse(dialect.dbId, Map.empty)

    // Process each segment
    for (beforeSegment <- metaBefore.segments) {
      val segmentName = beforeSegment.segment

      beforeSegment.migration match {
        case Some(SegmentMigration.Remove) =>
          // Drop all tables in this segment
          for (entity <- beforeSegment.entities) {
            val tableName = cleanName(dialect, entity.entity)
            statements += s"DROP TABLE IF EXISTS $tableName CASCADE;"
          }

        case Some(SegmentMigration.Rename(newSegmentName)) =>
          // Rename all tables: oldSegment_Entity -> newSegment_Entity
          afterSegmentsMap.get(newSegmentName).foreach { afterSegment =>
            for (beforeEntity <- beforeSegment.entities) {
              val oldTableName = cleanName(dialect, s"${segmentName}_${beforeEntity.entity}")
              val newTableName = cleanName(dialect, s"${newSegmentName}_${beforeEntity.entity}")
              statements += renameTable(dialect, oldTableName, newTableName)
            }
          }

        case None =>
          // Process entities within segment
          afterSegmentsMap.get(segmentName).foreach { afterSegment =>
            statements ++= processEntities(beforeSegment.entities, afterSegment.entities, dialect, generalPropsForDb, afterEntitiesMap)
          }
      }
    }

    // Add new segments/entities that don't exist in before
    for (afterSegment <- metaAfter.segments) {
      val beforeSegmentOpt = metaBefore.segments.find(_.segment == afterSegment.segment)
      beforeSegmentOpt match {
        case None =>
          // New segment - create all tables
          for (entity <- afterSegment.entities) {
            statements += createTable(entity, dialect, generalPropsForDb)
          }
        case Some(beforeSegment) =>
          // Check for new entities within existing segment
          for (afterEntity <- afterSegment.entities) {
            val existsInBefore = beforeSegment.entities.exists(_.entity == afterEntity.entity)
            if (!existsInBefore || afterEntity.migration.contains(EntityMigration.Add)) {
              statements += createTable(afterEntity, dialect, generalPropsForDb)
            }
          }
      }
    }

    if (statements.isEmpty) {
      "-- No migration changes"
    } else {
      statements.mkString("\n")
    }
  }

  private def processEntities(
    beforeEntities: List[MetaEntity],
    afterEntities: List[MetaEntity],
    dialect: Dialect,
    generalPropsForDb: Map[String, String],
    afterEntitiesMap: Map[String, MetaEntity]
  ): List[String] = {
    val statements = ListBuffer.empty[String]
    val afterEntitiesMapLocal = afterEntities.map(e => e.entity -> e).toMap

    for (beforeEntity <- beforeEntities) {
      val entityName = beforeEntity.entity

      beforeEntity.migration match {
        case Some(EntityMigration.Remove) =>
          // Drop table
          val tableName = cleanName(dialect, entityName)
          statements += s"DROP TABLE IF EXISTS $tableName CASCADE;"

        case Some(EntityMigration.Rename(oldName)) =>
          // Rename table
          val oldTableName = cleanName(dialect, oldName)
          val newTableName = cleanName(dialect, entityName)
          statements += renameTable(dialect, oldTableName, newTableName)

          // Process attribute changes within renamed entity
          afterEntitiesMapLocal.get(entityName).foreach { afterEntity =>
            statements ++= processAttributes(oldTableName, newTableName, beforeEntity, afterEntity, dialect, generalPropsForDb)
          }

        case Some(EntityMigration.Add) =>
          // New entity - will be handled in the outer loop

        case None =>
          // Entity exists - process attribute changes
          afterEntitiesMapLocal.get(entityName).foreach { afterEntity =>
            val tableName = cleanName(dialect, entityName)
            statements ++= processAttributes(tableName, tableName, beforeEntity, afterEntity, dialect, generalPropsForDb)
          }
      }
    }

    statements.toList
  }

  private def processAttributes(
    oldTableName: String,
    newTableName: String,
    beforeEntity: MetaEntity,
    afterEntity: MetaEntity,
    dialect: Dialect,
    generalPropsForDb: Map[String, String]
  ): List[String] = {
    val statements = ListBuffer.empty[String]
    val afterAttrsMap = afterEntity.attributes.map(a => a.attribute -> a).toMap
    val beforeAttrsMap = beforeEntity.attributes.map(a => a.attribute -> a).toMap

    // Use newTableName for all operations since table might have been renamed
    val tableName = newTableName

    // First, check for migration markers in afterEntity (current structure with .remove/.rename)
    for (afterAttr <- afterEntity.attributes) {
      val attrName = afterAttr.attribute

      afterAttr.migration match {
        case Some(AttrMigration.Remove) =>
          // Attribute marked with .remove in current structure
          val columnName = cleanName(dialect, attrName)

          // If this is a relationship, drop FK constraint and index first
          if (afterAttr.ref.nonEmpty) {
            statements += dropIndex(dialect, tableName, columnName)
            statements += dropForeignKey(dialect, tableName, columnName)
          } else if (afterAttr.options.contains("index")) {
            // Regular attribute with index
            statements += dropIndex(dialect, tableName, columnName)
          }

          statements += alterTableDropColumn(dialect, tableName, columnName)

        case Some(AttrMigration.Rename(newName)) =>
          // Attribute marked with .rename in current structure
          val oldColumnName = cleanName(dialect, attrName)
          val newColumnName = cleanName(dialect, newName)
          val columnType = dialect.tpe(afterAttr, generalPropsForDb)

          // If this is a relationship, need to drop/recreate FK and index
          if (afterAttr.ref.nonEmpty) {
            statements += dropIndex(dialect, tableName, oldColumnName)
            statements += dropForeignKey(dialect, tableName, oldColumnName)
            statements += renameColumn(dialect, tableName, oldColumnName, newColumnName, columnType)
            val isOwner = afterAttr.options.contains("owner")
            statements += addForeignKey(dialect, tableName, newColumnName, afterAttr.ref.get, isOwner)
            statements += createIndex(dialect, tableName, newColumnName)
          } else {
            // Regular attribute - just rename
            statements += renameColumn(dialect, tableName, oldColumnName, newColumnName, columnType)
          }

        case _ =>
          // No migration marker
      }
    }

    // Then process migration markers in beforeEntity (from migration file approach)
    for (beforeAttr <- beforeEntity.attributes) {
      val attrName = beforeAttr.attribute

      beforeAttr.migration match {
        case Some(AttrMigration.Remove) =>
          // Drop column
          val columnName = cleanName(dialect, attrName)

          // If this is a relationship, drop FK constraint and index first
          if (beforeAttr.ref.nonEmpty) {
            statements += dropIndex(dialect, tableName, columnName)
            statements += dropForeignKey(dialect, tableName, columnName)
          } else if (beforeAttr.options.contains("index")) {
            // Regular attribute with index
            statements += dropIndex(dialect, tableName, columnName)
          }

          statements += alterTableDropColumn(dialect, tableName, columnName)

        case Some(AttrMigration.Rename(newName)) =>
          // Rename column
          afterAttrsMap.get(newName).foreach { afterAttr =>
            val oldColumnName = cleanName(dialect, attrName)
            val newColumnName = cleanName(dialect, newName)
            val columnType = dialect.tpe(afterAttr, generalPropsForDb)

            // If this is a relationship, need to drop/recreate FK and index
            if (beforeAttr.ref.nonEmpty) {
              statements += dropIndex(dialect, tableName, oldColumnName)
              statements += dropForeignKey(dialect, tableName, oldColumnName)
              statements += renameColumn(dialect, tableName, oldColumnName, newColumnName, columnType)
              val isOwner = afterAttr.options.contains("owner")
              statements += addForeignKey(dialect, tableName, newColumnName, afterAttr.ref.get, isOwner)
              statements += createIndex(dialect, tableName, newColumnName)
            } else {
              // Regular attribute - just rename
              statements += renameColumn(dialect, tableName, oldColumnName, newColumnName, columnType)
            }
          }

        case Some(AttrMigration.Add) =>
          // New attribute - handled below

        case None =>
          // Attribute unchanged - no migration needed
      }
    }

    // Add new attributes
    // Collect all rename targets to avoid adding them as new columns
    val renameTargets = beforeEntity.attributes.flatMap {
      case attr if attr.migration.exists {
        case AttrMigration.Rename(newName) => true
        case _ => false
      } =>
        attr.migration.collect { case AttrMigration.Rename(newName) => newName }
      case _ => None
    }.toSet

    for (afterAttr <- afterEntity.attributes) {
      val attrName = afterAttr.attribute
      val existsInBefore = beforeEntity.attributes.exists(_.attribute == attrName)
      val isRenameTarget = renameTargets.contains(attrName)

      if ((!existsInBefore && !isRenameTarget) || afterAttr.migration.contains(AttrMigration.Add)) {
        val columnName = cleanName(dialect, attrName)
        val columnType = dialect.tpe(afterAttr, generalPropsForDb)
        statements += alterTableAddColumn(dialect, tableName, columnName, columnType)

        // If this is a relationship, add foreign key constraint and index
        if (afterAttr.ref.nonEmpty) {
          val refEntity = afterAttr.ref.get
          val isOwner = afterAttr.options.contains("owner")
          statements += addForeignKey(dialect, tableName, columnName, refEntity, isOwner)
          statements += createIndex(dialect, tableName, columnName)
        } else if (afterAttr.options.contains("index")) {
          // Regular attribute with .index option
          statements += createIndex(dialect, tableName, columnName)
        }
      }
    }

    // Handle option changes (.index and .owner)
    for (afterAttr <- afterEntity.attributes) {
      val attrName = afterAttr.attribute
      beforeAttrsMap.get(attrName).foreach { beforeAttr =>
        val columnName = cleanName(dialect, attrName)

        // Check for .index changes
        val beforeHasIndex = beforeAttr.options.contains("index")
        val afterHasIndex = afterAttr.options.contains("index")
        if (beforeHasIndex != afterHasIndex) {
          if (afterHasIndex) {
            // Index added
            statements += createIndex(dialect, tableName, columnName)
          } else {
            // Index removed
            statements += dropIndex(dialect, tableName, columnName)
          }
        }

        // Check for .owner changes on refs
        if (afterAttr.ref.nonEmpty) {
          val beforeHasOwner = beforeAttr.options.contains("owner")
          val afterHasOwner = afterAttr.options.contains("owner")
          if (beforeHasOwner != afterHasOwner) {
            val refEntity = afterAttr.ref.get
            // Need to recreate foreign key with/without CASCADE
            statements += dropForeignKey(dialect, tableName, columnName)
            statements += addForeignKey(dialect, tableName, columnName, refEntity, afterHasOwner)
          }
        }
      }
    }

    statements.toList
  }

  private def cleanName(dialect: Dialect, name: String): String = {
    if (dialect.reservedKeyWords.contains(name.toLowerCase)) name + "_" else name
  }

  private def createTable(entity: MetaEntity, dialect: Dialect, generalPropsForDb: Map[String, String]): String = {
    val entityName = entity.entity
    val tableName = cleanName(dialect, entityName)
    val tableSuffix = if (dialect.reservedKeyWords.contains(entityName.toLowerCase)) "_" else ""

    val columns = entity.attributes.flatMap {
      case a if a.value == molecule.core.dataModel.SetValue && a.ref.nonEmpty =>
        None // Join table columns handled separately
      case a =>
        val columnName = cleanName(dialect, a.attribute)
        val columnType = dialect.tpe(a, generalPropsForDb)
        Some(s"  $columnName $columnType")
    }.mkString(",\n")

    s"""CREATE TABLE IF NOT EXISTS $tableName$tableSuffix (
       |$columns
       |);""".stripMargin
  }

  private def renameTable(dialect: Dialect, oldName: String, newName: String): String = {
    dialect match {
      case _: H2.type         => s"ALTER TABLE $oldName RENAME TO $newName;"
      case _: PostgreSQL.type => s"ALTER TABLE $oldName RENAME TO $newName;"
      case _: MySQL.type      => s"RENAME TABLE $oldName TO $newName;"
      case _: MariaDB.type    => s"RENAME TABLE $oldName TO $newName;"
      case _: SQlite.type     => s"ALTER TABLE $oldName RENAME TO $newName;"
    }
  }

  private def alterTableAddColumn(dialect: Dialect, tableName: String, columnName: String, columnType: String): String = {
    s"ALTER TABLE $tableName ADD COLUMN $columnName $columnType;"
  }

  private def alterTableDropColumn(dialect: Dialect, tableName: String, columnName: String): String = {
    s"ALTER TABLE $tableName DROP COLUMN $columnName;"
  }

  private def renameColumn(dialect: Dialect, tableName: String, oldColumnName: String, newColumnName: String, columnType: String): String = {
    dialect match {
      case _: H2.type         => s"ALTER TABLE $tableName ALTER COLUMN $oldColumnName RENAME TO $newColumnName;"
      case _: PostgreSQL.type => s"ALTER TABLE $tableName RENAME COLUMN $oldColumnName TO $newColumnName;"
      case _: MySQL.type      => s"ALTER TABLE $tableName CHANGE $oldColumnName $newColumnName $columnType;"
      case _: MariaDB.type    => s"ALTER TABLE $tableName CHANGE $oldColumnName $newColumnName $columnType;"
      case _: SQlite.type     => s"ALTER TABLE $tableName RENAME COLUMN $oldColumnName TO $newColumnName;"
    }
  }

  private def createIndex(dialect: Dialect, tableName: String, columnName: String): String = {
    val ifNotExists = dialect match {
      case _: MySQL.type | _: MariaDB.type => ""
      case _                               => " IF NOT EXISTS"
    }
    s"CREATE INDEX$ifNotExists _${tableName}_$columnName ON $tableName ($columnName);"
  }

  private def dropIndex(dialect: Dialect, tableName: String, columnName: String): String = {
    dialect match {
      case _: MySQL.type | _: MariaDB.type => s"DROP INDEX _${tableName}_$columnName ON $tableName;"
      case _: H2.type                      => s"DROP INDEX IF EXISTS _${tableName}_$columnName;"
      case _: PostgreSQL.type              => s"DROP INDEX IF EXISTS _${tableName}_$columnName;"
      case _: SQlite.type                  => s"DROP INDEX IF EXISTS _${tableName}_$columnName;"
    }
  }

  private def dropForeignKey(dialect: Dialect, tableName: String, columnName: String): String = {
    dialect match {
      case _: MySQL.type | _: MariaDB.type => s"ALTER TABLE $tableName DROP FOREIGN KEY _$columnName;"
      case _: PostgreSQL.type              => s"ALTER TABLE $tableName DROP CONSTRAINT _$columnName;"
      case _: H2.type                      => s"ALTER TABLE $tableName DROP CONSTRAINT _$columnName;"
      case _: SQlite.type                  =>
        // SQLite doesn't support dropping foreign keys directly - would need table recreation
        s"-- WARNING: SQLite doesn't support dropping foreign keys. Manual migration required."
    }
  }

  private def addForeignKey(dialect: Dialect, tableName: String, columnName: String, refEntity: String, isOwner: Boolean): String = {
    val onDeleteCascade = if (isOwner) " ON DELETE CASCADE" else ""
    dialect match {
      case _: SQlite.type =>
        // SQLite doesn't support adding foreign keys - would need table recreation
        s"-- WARNING: SQLite doesn't support adding foreign keys. Manual migration required."
      case _ =>
        s"ALTER TABLE $tableName ADD CONSTRAINT _$columnName FOREIGN KEY ($columnName) REFERENCES $refEntity (id)$onDeleteCascade;"
    }
  }

  /**
   * Generate a descriptive filename for the Flyway migration.
   * Examples: "add_Person_email", "rename_Person_name_to_fullName", "remove_Person_age"
   */
  def generateMigrationDescription(metaBefore: MetaDomain, metaAfter: MetaDomain): String = {
    val changes = ListBuffer.empty[String]

    // Collect all changes
    for (beforeSegment <- metaBefore.segments) {
      beforeSegment.migration match {
        case Some(SegmentMigration.Remove) =>
          val segName = if (beforeSegment.segment.isEmpty) "root" else beforeSegment.segment
          changes += s"remove_segment_$segName"
        case Some(SegmentMigration.Rename(newName)) =>
          changes += s"rename_segment_${beforeSegment.segment}_to_$newName"
        case None =>
          // Process entity changes
          for (beforeEntity <- beforeSegment.entities) {
            beforeEntity.migration match {
              case Some(EntityMigration.Remove) =>
                changes += s"remove_${beforeEntity.entity}"
              case Some(EntityMigration.Rename(oldName)) =>
                changes += s"rename_${oldName}_to_${beforeEntity.entity}"
              case _ =>
                // Process attribute changes
                for (beforeAttr <- beforeEntity.attributes) {
                  beforeAttr.migration match {
                    case Some(AttrMigration.Remove) =>
                      changes += s"remove_${beforeEntity.entity}_${beforeAttr.attribute}"
                    case Some(AttrMigration.Rename(newName)) =>
                      changes += s"rename_${beforeEntity.entity}_${beforeAttr.attribute}_to_$newName"
                    case _ =>
                  }
                }
            }
          }
      }
    }

    // Check for new entities/attributes in metaAfter
    // Also check for migration markers in metaAfter (.remove, .rename)
    for (afterSegment <- metaAfter.segments) {
      val beforeSegmentOpt = metaBefore.segments.find(_.segment == afterSegment.segment)

      beforeSegmentOpt match {
        case None =>
          // New segment
          val segName = if (afterSegment.segment.isEmpty) "root" else afterSegment.segment
          changes += s"add_segment_$segName"

        case Some(beforeSegment) =>
          // Check for new entities
          for (afterEntity <- afterSegment.entities) {
            val beforeEntityOpt = beforeSegment.entities.find(_.entity == afterEntity.entity)

            beforeEntityOpt match {
              case None =>
                changes += s"add_${afterEntity.entity}"

              case Some(beforeEntity) =>
                // Collect all rename targets to avoid counting them as new attributes
                val renameTargets = beforeEntity.attributes.flatMap {
                  case attr if attr.migration.exists {
                    case AttrMigration.Rename(newName) => true
                    case _ => false
                  } =>
                    attr.migration.collect { case AttrMigration.Rename(newName) => newName }
                  case _ => None
                }.toSet

                // Check for new attributes
                for (afterAttr <- afterEntity.attributes) {
                  val existsInBefore = beforeEntity.attributes.exists(_.attribute == afterAttr.attribute)
                  val isRenameTarget = renameTargets.contains(afterAttr.attribute)

                  if (!existsInBefore && !isRenameTarget) {
                    changes += s"add_${afterEntity.entity}_${afterAttr.attribute}"
                  } else {
                    // Attribute exists in before - check for migration markers in current structure
                    afterAttr.migration match {
                      case Some(AttrMigration.Remove) =>
                        changes += s"remove_${afterEntity.entity}_${afterAttr.attribute}"
                      case Some(AttrMigration.Rename(newName)) =>
                        changes += s"rename_${afterEntity.entity}_${afterAttr.attribute}_to_$newName"
                      case _ =>
                    }
                  }
                }
            }
          }
      }
    }

    if (changes.isEmpty) {
      "molecule_no_changes"
    } else {
      // Simple generic description - just the count, prefixed with "molecule"
      // to distinguish auto-generated migrations from user's manual migrations
      val suffix = if (changes.length == 1) "change" else "changes"
      s"molecule_${changes.length}_$suffix"
    }
  }
}
