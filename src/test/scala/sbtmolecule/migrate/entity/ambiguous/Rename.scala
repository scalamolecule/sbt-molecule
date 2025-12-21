package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait Rename_1 extends DomainStructure {
  trait Person {
    val name = oneString
  }
}

trait Rename_2 extends DomainStructure {
  trait Individual {
    val name = oneString
  }
}

object Rename extends BaseTest {
  override def tests: Tests = Tests {
    "Ambiguous entity change throws error" - {
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
