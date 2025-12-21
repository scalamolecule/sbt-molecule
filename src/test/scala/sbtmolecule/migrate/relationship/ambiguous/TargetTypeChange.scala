package sbtmolecule.migrate.relationship.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait TargetTypeChange_1 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer].rename("user")
  }

  trait Customer {
    val name = oneString
  }

  trait User {
    val email = oneString
  }
}

trait TargetTypeChange_2 extends DomainStructure {
  trait Order {
    val number = oneInt
    val user   = manyToOne[User] // Changed target type from Customer to User
  }

  trait Customer {
    val name = oneString
  }

  trait User {
    val email = oneString
  }
}

object TargetTypeChange extends BaseTest {
  val List(before, after) = rawStructures("relationship/ambiguous/")

  override def tests: Tests = Tests {
    "Type change detection" - {
      intercept[Exception] {
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Customer.Orders""".stripMargin
    }
  }
}
