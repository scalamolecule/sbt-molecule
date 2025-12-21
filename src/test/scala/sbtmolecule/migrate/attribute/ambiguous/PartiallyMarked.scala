package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait PartiallyMarked_1 extends DomainStructure {
  trait Person {
    val firstName = oneString.rename("name")
    val lastName  = oneString // Missing migration command!
  }
}

trait PartiallyMarked_2 extends DomainStructure {
  trait Person {
    val name = oneString
  }
}

object PartiallyMarked extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // 'firstName' has explicit .rename("name")
      // 'lastName' disappeared without migration command
      // System detects incomplete migration commands
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Person.lastName""".stripMargin
    }
  }
}
