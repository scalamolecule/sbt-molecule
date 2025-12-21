package sbtmolecule.migrate.attribute

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

/**
 * Tests detection of cyclical renames (N-way cycles where N > 2).
 *
 * Examples:
 * - 3-way cycle: A->B, B->C, C->A
 * - 4-way cycle: A->B, B->C, C->D, D->A
 */

// 3-way cycle: email -> phone -> address -> email
trait ThreeWayCycle_1 extends DomainStructure {
  trait Person {
    val email   = oneString.rename("phone")
    val phone   = oneString.rename("address")
    val address = oneString.rename("email")
  }
}

trait ThreeWayCycle_2 extends DomainStructure {
  trait Person {
    val email   = oneString
    val phone   = oneString
    val address = oneString
  }
}

// 4-way cycle: a -> b -> c -> d -> a
trait FourWayCycle_1 extends DomainStructure {
  trait Data {
    val a = oneString.rename("b")
    val b = oneString.rename("c")
    val c = oneString.rename("d")
    val d = oneString.rename("a")
  }
}

trait FourWayCycle_2 extends DomainStructure {
  trait Data {
    val a = oneString
    val b = oneString
    val c = oneString
    val d = oneString
  }
}

// Valid chain (not a cycle): a -> b -> c
trait ValidChain_1 extends DomainStructure {
  trait Config {
    val oldName    = oneString.rename("mediumName")
    val mediumName = oneString.rename("newName")
    val unrelated  = oneInt
  }
}

trait ValidChain_2 extends DomainStructure {
  trait Config {
    val newName   = oneString
    val unrelated = oneInt
  }
}

object CyclicalRenameTest extends BaseTest {

  override def tests: Tests = Tests {
    "Detect 3-way cycle" - {
      val allStructures = rawStructures("attribute/")
      val before        = allStructures(0) // ThreeWayCycle_1
      val after         = allStructures(1) // ThreeWayCycle_2
      intercept[Exception] {
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Dangerous attribute rename cycle detected.
          |
          |Cyclical renames:
          |  - Person.address -> Person.email -> Person.phone -> Person.address
          |
          |This is not allowed because it can cause data corruption during migration.
          |You must break the cycle over multiple migrations using temporary names:
          |
          |Migration 1: Rename first attribute to temporary name
          |  val address = oneType.rename("address_temp")
          |
          |Migration 2: Perform the remaining renames
          |  val email = oneType.rename("phone")
          |  val phone = oneType.rename("address_temp")
          |
          |Migration 3: Rename temporary to final name
          |  val address_temp = oneType.rename("email")""".stripMargin
    }

    "Detect 4-way cycle" - {
      val allStructures = rawStructures("attribute/")
      val before        = allStructures(2) // FourWayCycle_1
      val after         = allStructures(3) // FourWayCycle_2
      intercept[Exception] {
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Dangerous attribute rename cycle detected.
          |
          |Cyclical renames:
          |  - Data.a -> Data.b -> Data.c -> Data.d -> Data.a
          |
          |This is not allowed because it can cause data corruption during migration.
          |You must break the cycle over multiple migrations using temporary names:
          |
          |Migration 1: Rename first attribute to temporary name
          |  val a = oneType.rename("a_temp")
          |
          |Migration 2: Perform the remaining renames
          |  val b = oneType.rename("c")
          |  val c = oneType.rename("d")
          |  val d = oneType.rename("a_temp")
          |
          |Migration 3: Rename temporary to final name
          |  val a_temp = oneType.rename("b")""".stripMargin
    }

    "Allow valid rename chain (not a cycle)" - {
      val allStructures = rawStructures("attribute/")
      val before        = allStructures(4) // ValidChain_1
      val after         = allStructures(5) // ValidChain_2

      // Should NOT throw - this is a valid chain: oldName -> mediumName -> newName
      import sbtmolecule.migration.MigrationDetector
      val annotated = MigrationDetector.annotateBefore(before, after)

      // Verify the renames were detected correctly
      val entity         = annotated.segments.head.entities.head
      val oldNameAttr    = entity.attributes.find(_.attribute == "oldName").get
      val mediumNameAttr = entity.attributes.find(_.attribute == "mediumName").get

      oldNameAttr.migration.toString ==> "Some(Rename(mediumName))"
      mediumNameAttr.migration.toString ==> "Some(Rename(newName))"
    }
  }
}
