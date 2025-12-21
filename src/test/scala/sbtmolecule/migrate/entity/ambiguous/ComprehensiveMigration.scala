package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait ComprehensiveMigration_1 extends DomainStructure {
  // Entity without migration marker (entity-level error)
  // Also has attribute changes (would be attribute-level error)
  trait Person {
    val firstName = oneString
    val lastName  = oneString
    val age       = oneInt
  }

  // Entity with explicit rename, but attributes without migration (attribute-level errors)
  trait Company extends RenameToDummyEntity {
    val oldName   = oneString // disappeared without .remove or .rename
    val employees = oneInt
  }

  // Entity without marker (entity-level error)
  trait Warehouse {
    val location = oneString
  }

  // Entity staying with attribute issues (attribute-level errors)
  trait Product {
    val title = oneString // disappeared without .remove or .rename
    val sku   = oneString
  }
}

trait ComprehensiveMigration_2 extends DomainStructure {
  trait Entity {
    val name      = oneString
    val employees = oneInt
  }

  trait Product {
    val name = oneString
    val sku  = oneString
  }
}

object ComprehensiveMigration extends BaseTest {
  override def tests: Tests = Tests {
    "Entity and attribute errors both reported" - {
      // Both entity-level errors (Person, Warehouse disappeared)
      // AND attribute-level errors (Company.oldName, Product.title disappeared)
      // are reported in the combined error message
      intercept[Exception](
        structures("entity/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  Person
          |  Warehouse
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  Company.oldName
          |  Product.title""".stripMargin
    }
  }
}
