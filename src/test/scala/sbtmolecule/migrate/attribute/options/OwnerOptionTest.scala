package sbtmolecule.migrate.attribute.options

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationSqlGenerator
import sbtmolecule.db.sqlDialect.H2
import utest.*

/**
 * Tests for .owner option add/remove migrations on references
 */

// Adding .owner
trait AddOwner_1 extends DomainStructure {
  trait Person {
    val name    = oneString
    val company = manyToOne[Company]
  }
  trait Company {
    val companyName = oneString
  }
}

trait AddOwner_2 extends DomainStructure {
  trait Person {
    val name    = oneString
    val company = manyToOne[Company].owner
  }
  trait Company {
    val companyName = oneString
  }
}

// Removing .owner
trait RemoveOwner_1 extends DomainStructure {
  trait Person {
    val name    = oneString
    val company = manyToOne[Company].owner
  }
  trait Company {
    val companyName = oneString
  }
}

trait RemoveOwner_2 extends DomainStructure {
  trait Person {
    val name    = oneString
    val company = manyToOne[Company]
  }
  trait Company {
    val companyName = oneString
  }
}

object OwnerOptionTest extends BaseTest {

  override def tests: Tests = Tests {
    "Add .owner to reference" - {
      val allStructures = rawStructures("attribute/options/")
      // Find AddOwner structures
      val before        = allStructures.find(_.domain.contains("AddOwner_1")).get
      val after         = allStructures.find(_.domain.contains("AddOwner_2")).get

      MigrationSqlGenerator.generateMigrationSql(before, after, H2) ==>
        """ALTER TABLE Person DROP CONSTRAINT _company;
          |ALTER TABLE Person ADD CONSTRAINT _company FOREIGN KEY (company) REFERENCES Company (id) ON DELETE CASCADE;""".stripMargin
    }

    "Remove .owner from reference" - {
      val allStructures = rawStructures("attribute/options/")
      // Find RemoveOwner structures
      val before        = allStructures.find(_.domain.contains("RemoveOwner_1")).get
      val after         = allStructures.find(_.domain.contains("RemoveOwner_2")).get

      MigrationSqlGenerator.generateMigrationSql(before, after, H2) ==>
        """ALTER TABLE Person DROP CONSTRAINT _company;
          |ALTER TABLE Person ADD CONSTRAINT _company FOREIGN KEY (company) REFERENCES Company (id);""".stripMargin
    }
  }
}
