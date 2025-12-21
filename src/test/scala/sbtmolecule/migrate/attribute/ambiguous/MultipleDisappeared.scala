package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MultipleDisappeared_1 extends DomainStructure {
  trait Person {
    val firstName = oneString
    val lastName  = oneString
  }
}

trait MultipleDisappeared_2 extends DomainStructure {
  trait Person {
    val name = oneString
  }
}

object MultipleDisappeared extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // Both 'firstName' and 'lastName' disappeared without migration commands
      // System cannot determine which was renamed or if both were removed
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Person.firstName
          |  Person.lastName""".stripMargin
    }
  }
}
