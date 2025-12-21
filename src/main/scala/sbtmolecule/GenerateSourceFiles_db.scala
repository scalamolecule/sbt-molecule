package sbtmolecule

import java.io.File
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbt.*
import sbtmolecule.db.dsl.Entity
import sbtmolecule.db.dsl.ops.*
import sbtmolecule.db.resolvers.*


case class GenerateSourceFiles_db(metaDomain: MetaDomain) {

  // For testing only
  def printEntity(metaEntity: MetaEntity): Unit = println(Entity(metaDomain, metaEntity).get)
  def printEntityBuilder(metaEntity: MetaEntity): Unit = println(Entity_(metaDomain, metaEntity, 0, 0).get)
  def printMetaDb: Unit = println(MetaDb_(metaDomain).getMeta)


  def generate(srcManaged: File, schemaDir: File): Unit = {
    var entityIndex  = 0
    var attrIndex    = 0
    val pkg          = metaDomain.pkg
    val domain       = metaDomain.domain
    val segments     = metaDomain.segments
    val pkgSegments  = pkg.split('.').toList
    val domainBase   = pkgSegments.foldLeft(srcManaged)((dir, pkg) => dir / pkg)
    val domainDir    = domainBase / "dsl" / domain
    val schemaBase   = pkgSegments.foldLeft(schemaDir)((dir, pkg) => dir / pkg)
    val domainSchemaDir = schemaBase / domain

    val segmentsWithoutEnums = segments.filter {
      case MetaSegment("_enums", entities, _) =>
        entities.foreach {
          case MetaEntity(enumTpe, attributes, _, _, _, _, _, _, _, _, _, _) =>
            val enumCode =
              s"""// AUTO-GENERATED Molecule boilerplate code
                 |package $pkg.dsl.$domain
                 |
                 |enum $enumTpe:
                 |  case ${attributes.map(_.attribute).mkString(", ")}
                 |""".stripMargin

            IO.write(domainDir / s"$enumTpe.scala", enumCode)
        }
        false

      case _ => true
    }

    for {
      metaSegment <- segmentsWithoutEnums
      metaEntity <- metaSegment.entities
    } yield {
      val ent     = metaEntity.entity
      val entity  = Entity(metaDomain, metaEntity).get
      val entity_ = Entity_(metaDomain, metaEntity, entityIndex, attrIndex).get

      // Increment of entityIndex has to come after calling DbTable.get
      entityIndex += 1
      attrIndex += metaEntity.attributes.length
      IO.write(domainDir / s"$ent.scala", entity)
      IO.write(domainDir / "ops" / s"${ent}_.scala", entity_)
    }

    val h2         = Db_H2(metaDomain)
    val mariadb    = Db_MariaDB(metaDomain)
    val mysql      = Db_MySQL(metaDomain)
    val postgresql = Db_PostgreSQL(metaDomain)
    val sqlite     = Db_SQlite(metaDomain)

    val metadb = domainDir / "metadb"
    IO.write(metadb / s"${domain}_.scala", MetaDb_(metaDomain).getMeta)
    IO.write(metadb / s"${domain}_h2.scala", h2.get)
    IO.write(metadb / s"${domain}_mariadb.scala", mariadb.get)
    IO.write(metadb / s"${domain}_mysql.scala", mysql.get)
    IO.write(metadb / s"${domain}_postgresql.scala", postgresql.get)
    IO.write(metadb / s"${domain}_sqlite.scala", sqlite.getMeta)

    IO.write(domainSchemaDir / s"${domain}_h2.sql", h2.getSQL)
    IO.write(domainSchemaDir / s"${domain}_mariadb.sql", mariadb.getSQL)
    IO.write(domainSchemaDir / s"${domain}_mysql.sql", mysql.getSQL)
    IO.write(domainSchemaDir / s"${domain}_postgresql.sql", postgresql.getSQL)
    IO.write(domainSchemaDir / s"${domain}_sqlite.sql", sqlite.getSQL)

    // Note: V1 migrations are now created explicitly via --init-migrations flag
    // Incremental migrations are auto-generated when migration directory exists
  }

}

object GenerateSourceFiles_db {
  /**
   * Save the current domain structure source file for comparison on next moleculeGen run.
   * Similar to Flyway V1 - always overwrites with the latest unambiguous structure.
   * Stored in resources/db/migration/{namespacePath}/{domain}/{domain}_previous.scala
   */
  def saveBeforeStructure(resources: File, namespacePath: String, domain: String, sourceFile: File): Unit = {
    val migrationDir = resources / "db" / "migration" / namespacePath / domain
    val beforeFile = migrationDir / s"${domain}_previous.scala"
    IO.createDirectory(migrationDir)

    // Copy the source file
    IO.copyFile(sourceFile, beforeFile)
  }

  /**
   * Get the next Flyway migration version number by scanning existing migrations.
   * Returns "V1" if no migrations exist, "V2" if V1 exists, etc.
   */
  def getNextMigrationVersion(resources: File, namespacePath: String, domain: String, dialect: String): String = {
    val flywayDir = resources / "db" / "migration" / namespacePath / domain / dialect
    if (!flywayDir.exists()) {
      "V1"
    } else {
      val versionPattern = """^V(\d+)__.*\.sql$""".r
      val versions = flywayDir.listFiles()
        .filter(_.isFile)
        .flatMap { file =>
          file.getName match {
            case versionPattern(num) => Some(num.toInt)
            case _ => None
          }
        }

      if (versions.isEmpty) {
        "V1"
      } else {
        val maxVersion = versions.max
        s"V${maxVersion + 1}"
      }
    }
  }

  /**
   * Generate Flyway migration files for all dialects.
   */
  def generateFlywayMigration(
    resources: File,
    namespacePath: String,
    domain: String,
    metaBefore: MetaDomain,
    metaAfter: MetaDomain
  ): Unit = {
    import sbtmolecule.migration.MigrationSqlGenerator
    import sbtmolecule.db.sqlDialect._

    val description = MigrationSqlGenerator.generateMigrationDescription(metaBefore, metaAfter)

    val dialects = List(
      ("h2", H2),
      ("postgresql", PostgreSQL),
      ("mysql", MySQL),
      ("mariadb", MariaDB),
      ("sqlite", SQlite)
    )

    for ((dialectName, dialect) <- dialects) {
      val version = getNextMigrationVersion(resources, namespacePath, domain, dialectName)
      val sql = MigrationSqlGenerator.generateMigrationSql(metaBefore, metaAfter, dialect)

      val flywayDir = resources / "db" / "migration" / namespacePath / domain / dialectName
      val migrationFile = flywayDir / s"${version}__$description.sql"

      IO.createDirectory(flywayDir)
      IO.write(migrationFile, sql)
    }
  }

  /**
   * Load previous domain structure if it exists by parsing the saved source file.
   * Returns None if this is the first run or file doesn't exist.
   */
  def loadBeforeStructure(resources: File, namespacePath: String, domain: String): Option[MetaDomain] = {
    val migrationDir = resources / "db" / "migration" / namespacePath / domain
    val beforeFile = migrationDir / s"${domain}_previous.scala"

    if (beforeFile.exists()) {
      try {
        // Parse the saved domain structure file
        val generators = ParseAndGenerate(beforeFile.getPath).generators
        generators.headOption.map(_.metaDomain)
      } catch {
        case e: Exception =>
          println(s"Warning: Could not load previous domain structure: ${e.getMessage}")
          None
      }
    } else {
      None
    }
  }
}
