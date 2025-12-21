package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait EntityAndAttributeErrors_1 extends DomainStructure {
  // Entity without migration marker (entity-level error)
  trait Person {
    val firstName = oneString
    val lastName  = oneString // Also an attribute-level error
  }

  // Entity with explicit marker, but attribute without migration (attribute-level error only)
  trait Company extends RenameToDummyEntity {
    val orgName = oneString // This attribute disappeared (attribute-level error)
  }
}

trait EntityAndAttributeErrors_2 extends DomainStructure {
  trait Entity {
    val name = oneString
  }
}

object EntityAndAttributeErrors extends BaseTest {
  override def tests: Tests = Tests {
    "Entity errors take precedence over attribute errors" - {
      // Even though there are both entity-level errors (Person disappeared)
      // and attribute-level errors (Company.orgName disappeared),
      // both errors should be reported in the combined error message
      intercept[Exception](
        structures("entity/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  Person
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Company.orgName""".stripMargin
    }
  }
}
