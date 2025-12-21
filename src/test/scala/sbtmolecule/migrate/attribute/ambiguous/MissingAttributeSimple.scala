package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MissingAttributeSimple_1 extends DomainStructure {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}

trait MissingAttributeSimple_2 extends DomainStructure {
  trait Person {
    val age = oneInt
  }
}

object MissingAttributeSimple extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // 'name' disappeared without .remove
      // System cannot determine if this was intentional or a mistake
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Person.name""".stripMargin
    }
  }
}
