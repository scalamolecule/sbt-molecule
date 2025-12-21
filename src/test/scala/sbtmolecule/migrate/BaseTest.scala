package sbtmolecule.migrate

import molecule.base.metaModel.MetaDomain
import sbtmolecule.ParseAndGenerate
import sbtmolecule.migration.MigrationDetector
import utest.TestSuite

trait BaseTest extends TestSuite {
  val basePath = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/migrate/"

  // Use name of calling test class to find source and target structures within the test file
  def path(dir: String): String = basePath + dir + getClass.getSimpleName.dropRight(1) + ".scala"

  /**
   * Parse all domain structures from test file.
   * Returns: List(before, after, [migration1, migration2, ...])
   * Migration structures are optional and only present in tests that include them.
   */
  def rawStructures(dir: String): List[MetaDomain] = ParseAndGenerate(path(dir)).generators.map(_.metaDomain)

  /**
   * Get before and after structures with migration detection.
   * Only compares the first two structures (before and after).
   * Throws exception if ambiguous changes detected.
   */
  def structures(dir: String): (MetaDomain, MetaDomain) = {
    val allStructures       = rawStructures(dir)
    // Take only first two for comparison (before and after)
    val List(before, after) = allStructures.take(2)
    val annotatedBefore     = MigrationDetector.annotateBefore(before, after)
    (annotatedBefore, after)
  }

  def sql(before: MetaDomain, after: MetaDomain): String =
    sbtmolecule.migration.MigrationSqlGenerator.generateMigrationSql(before, after, sbtmolecule.db.sqlDialect.H2)

  // For compatibility - generate SQL from before domain only
  def sql(before: MetaDomain): List[String] = {
    // Extract after domain from test structures
    // This is a bit hacky but maintains backward compatibility
    MigrationDetector.generateSQL(before)
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
    val migrationMap: Map[String, Map[String, molecule.base.metaModel.AttrMigration]] =
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
                      case None            => attr
                    }
                  }
                )
              case None                 => entity
            }
          }
        )
      }
    )
  }

  /**
   * Extracts the source code of a specific trait from the test file.
   * Used to compare generated migration source with actual trait definition.
   */
  def extractTraitSource(dir: String, traitName: String): String = {
    import scala.meta._

    val filePath    = path(dir)
    val fileContent = scala.io.Source.fromFile(filePath).mkString

    // Parse with scalameta
    val tree = fileContent.parse[scala.meta.Source].get

    // Find the trait with the given name
    tree.collect {
      case tr@q"trait $name extends ..$parents { ..$stats }" if name.value == traitName =>
        tr.syntax
    }.headOption.getOrElse(
      throw new Exception(s"Trait $traitName not found in $filePath")
    )
  }

  /**
   * Normalizes whitespace for comparison (removes extra spaces, blank lines, etc.)
   */
  def normalizeWhitespace(str: String): String = {
    str
      .split("\n")
      .map(_.trim)
      .filter(_.nonEmpty)
      .mkString("\n")
  }

  /**
   * Strips source positions from MetaDomain for comparison.
   * Generated MetaDomains don't have source positions, but parsed ones do.
   */
  def stripPositions(meta: MetaDomain): MetaDomain = {
    meta.copy(
      segments = meta.segments.map { segment =>
        segment.copy(
          entities = segment.entities.map { entity =>
            entity.copy(
              attributes = entity.attributes.map { attr =>
                attr.copy(sourcePosition = None)
              }
            )
          }
        )
      }
    )
  }
}
