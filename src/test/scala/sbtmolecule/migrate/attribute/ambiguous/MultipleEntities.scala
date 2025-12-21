package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MultipleEntities_1 extends DomainStructure {
  trait Person {
    val firstName = oneString
    val lastName  = oneString
  }

  trait Company {
    val title     = oneString
    val shortName = oneString
  }
}

trait MultipleEntities_2 extends DomainStructure {
  trait Person {
    val fullName = oneString
  }

  trait Company {
    val name = oneString
  }
}

object MultipleEntities extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // Multiple entities with disappeared attributes without migration commands
      // Person: 'firstName' and 'lastName' disappeared
      // Company: 'title' and 'shortName' disappeared
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Person.firstName
          |  Person.lastName
          |  Company.title
          |  Company.shortName""".stripMargin
    }
  }
}
