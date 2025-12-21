package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait ImplicitRename_1 extends DomainStructure {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}

trait ImplicitRename_2 extends DomainStructure {
  trait Individual {
    val name = oneString
    val age  = oneInt
  }
}

object ImplicitRename extends BaseTest {
  override def tests: Tests = Tests {
    "Implicit entity rename throws error" - {
      intercept[Exception](
        structures("entity/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  Person""".stripMargin
    }
  }
}
