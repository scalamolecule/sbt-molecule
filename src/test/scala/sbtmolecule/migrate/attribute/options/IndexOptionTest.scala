package sbtmolecule.migrate.attribute.options

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationSqlGenerator
import sbtmolecule.db.sqlDialect.H2
import utest.*

/**
 * Tests for .index option add/remove migrations
 */

// Adding .index
trait AddIndex_1 extends DomainStructure {
  trait Person {
    val name  = oneString
    val email = oneString
  }
}

trait AddIndex_2 extends DomainStructure {
  trait Person {
    val name  = oneString.index
    val email = oneString.index
  }
}

// Removing .index
trait RemoveIndex_1 extends DomainStructure {
  trait Person {
    val name  = oneString.index
    val email = oneString
  }
}

trait RemoveIndex_2 extends DomainStructure {
  trait Person {
    val name  = oneString
    val email = oneString
  }
}

object IndexOptionTest extends BaseTest {

  override def tests: Tests = Tests {
    "Add .index to attributes" - {
      val allStructures = rawStructures("attribute/options/")
      val before        = allStructures(0) // AddIndex_1
      val after         = allStructures(1) // AddIndex_2

      MigrationSqlGenerator.generateMigrationSql(before, after, H2) ==>
        """CREATE INDEX IF NOT EXISTS _Person_name ON Person (name);
          |CREATE INDEX IF NOT EXISTS _Person_email ON Person (email);""".stripMargin
    }

    "Remove .index from attributes" - {
      val allStructures = rawStructures("attribute/options/")
      val before        = allStructures(2) // RemoveIndex_1
      val after         = allStructures(3) // RemoveIndex_2

      MigrationSqlGenerator.generateMigrationSql(before, after, H2) ==>
        "DROP INDEX IF EXISTS _Person_name;"
    }
  }
}
