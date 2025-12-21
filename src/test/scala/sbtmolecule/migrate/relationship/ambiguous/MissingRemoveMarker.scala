package sbtmolecule.migrate.relationship.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait MissingRemoveMarker_1 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer] // Missing .remove marker
  }

  trait Customer {
    val name = oneString
  }
}

trait MissingRemoveMarker_2 extends DomainStructure {
  trait Order {
    val number = oneInt
    // customer relationship removed but no .remove marker
  }

  trait Customer {
    val name = oneString
  }
}

object MissingRemoveMarker extends BaseTest {
  val List(before, after) = rawStructures("relationship/ambiguous/")

  override def tests: Tests = Tests {
    "Missing remove marker" - {
      intercept[Exception] {
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Order.customer
          |  Customer.Orders""".stripMargin
    }
  }
}
