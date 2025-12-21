package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MultipleDisappearedMultipleAdded_1 extends DomainStructure {
  trait Person {
    val firstName = oneString
    val lastName  = oneString
  }
}

trait MultipleDisappearedMultipleAdded_2 extends DomainStructure {
  trait Person {
    val fullName    = oneString
    val displayName = oneString
  }
}

object MultipleDisappearedMultipleAdded extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // Both 'firstName' and 'lastName' disappeared without migration commands
      // System cannot determine the correct mapping between old and new attributes
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
