package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait MultipleDisappeared_1 extends DomainStructure {
  trait Person {
    val name = oneString
  }

  trait Company {
    val orgName = oneString
  }

  trait Product {
    val title = oneString
  }
}

trait MultipleDisappeared_2 extends DomainStructure {
  trait Product {
    val title = oneString
  }
}

object MultipleDisappeared extends BaseTest {
  override def tests: Tests = Tests {
    "Ambiguous multiple entity changes throw error" - {
      intercept[Exception](
        structures("entity/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  Person
          |  Company""".stripMargin
    }
  }
}
