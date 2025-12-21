package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import sbtmolecule.migration.MigrationFileGenerator.*
import utest.*

// Before-structure - saved by the plugin from last `sbt moleculeGen` run.
// This is the structure that we compare with the new one.
trait ImplicitRename_1 extends DomainStructure {
  trait Person {
    val name = oneString
  }
}

// User makes changes to domain structure - but has forgotten to add migration commands, so it's ambuiguous.
trait ImplicitRename_2 extends DomainStructure {
  trait Person {
    val fullName = oneString.description("keep new description")
  }
}

// Generated migration file (only showing remove option to avoid compilation error in test file)
// In reality, the generator creates BOTH options forcing user to choose
trait ImplicitRename_migration extends ImplicitRename_2 with DomainStructure {

  trait PersonMigrations extends Person {
    val name = oneString.remove
  }
}

// Resolved migration file (user has chosen the rename option)
trait ImplicitRename_migrationResolved extends ImplicitRename_2 with DomainStructure {

  trait PersonMigrations extends Person {
    val name = oneString.becomes(fullName)
  }
}

object ImplicitRename extends BaseTest {

  override def tests: Tests = Tests {

    "Error thrown running `sbt moleculeGen`" - {
      // User likely intended to rename 'name' -> 'fullName' but forgot .rename("fullName")
      // System detects 'name' disappeared without migration command and throws immediately
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Person.name""".stripMargin
    }

    "Migration meta domain" - {
      // Get all structures (before, after, migration1, migration2)
      val List(before, after, migration1, _) = rawStructures("attribute/ambiguous/")

      // Generated migration meta domain matches ImplicitRename_migration (ignoring source positions)
      migrationMetaDomain(before, after, "ImplicitRename_migration") ==> stripPositions(migration1)
    }


    "Migration trait" - {
      // Get all structures
      val List(before, after, _, _) = rawStructures("attribute/ambiguous/")

      // Build migration MetaDomain
      val generatedMigration = migrationMetaDomain(before, after, "ImplicitRename_migration")

      // Generate Scala source code from migration MetaDomain
      val generatedMigrationTrait = migrationSource(generatedMigration, "ImplicitRename_2", before, after)

      // Compare with expected format (both options uncommented - forces compilation error)
      generatedMigrationTrait ==>
        """trait ImplicitRename_migration extends ImplicitRename_2 with DomainStructure {
          |
          |  // Please choose intended migration commands:
          |  // (comment-out or delete unwanted option lines)
          |
          |  trait PersonMigrations extends Person {
          |    val name = oneString.remove // if removed
          |    val name = oneString.becomes() // if renamed: add new attribute like .becomes(otherAttr)
          |  }
          |}""".stripMargin
    }

    "Resolved migration file" - {
      // Get all structures
      val List(before, after, _, migration2) = rawStructures("attribute/ambiguous/")

      // Apply migration commands from migration2 to before structure
      val beforeWithMigrations = applyMigrationCommands(before, migration2)

      // Now annotation should succeed (no ambiguity)
      val annotatedBefore = MigrationDetector.annotateBefore(beforeWithMigrations, after)

      // Generate SQL and verify
      sql(annotatedBefore) ==> List("ALTER TABLE Person RENAME COLUMN name TO fullName;")
    }
  }
}
