package sbtmolecule.migrate.attribute

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait NameSwap_1 extends DomainStructure {
  trait Person {
    val email = oneString.rename("phone")
    val phone = oneString.rename("email")
  }
}

trait NameSwap_2 extends DomainStructure {
  trait Person {
    val email = oneString
    val phone = oneString
  }
}

object NameSwapDetectionTest extends BaseTest {
  override def tests: Tests = Tests {
    "Detect dangerous name swap" - {
      val allStructures = rawStructures("attribute/")
      val before        = allStructures.head // NameSwap_1
      val after         = allStructures(1) // NameSwap_2

      intercept[Exception] {
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Dangerous attribute rename cycle detected.
          |
          |2-way swaps:
          |  - Person.email <-> Person.phone
          |This is not allowed because it can cause data corruption during migration.
          |You must break the cycle over multiple migrations using temporary names:
          |
          |Migration 1: Rename to temporary names
          |  val email = oneType.rename("email_temp")
          |  val phone = oneType.rename("phone_temp")
          |
          |Migration 2: Rename to final names
          |  val email_temp = oneType.rename("phone")
          |  val phone_temp = oneType.rename("email")""".stripMargin
    }
  }
}
