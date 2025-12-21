package sbtmolecule.migrate.segment.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait ComprehensiveMigrationErrors_1 extends DomainStructure {
  // Segment disappears without migration
  object sales {
    trait Customer {
      val firstName = oneString
      val lastName  = oneString
    }
  }

  // Segment stays but has entity/attribute errors
  object inventory {
    trait Product {
      val oldTitle = oneString // attribute disappeared
      val sku      = oneString
    }

    trait Warehouse {
      val location = oneString // entity disappeared
    }
  }

  // Another segment disappears
  object shipping {
    trait Carrier {
      val name = oneString
    }
  }
}

trait ComprehensiveMigrationErrors_2 extends DomainStructure {
  object inventory {
    trait Product {
      val newTitle = oneString
      val sku      = oneString
    }
  }
}

object ComprehensiveMigrationErrors extends BaseTest {
  override def tests: Tests = Tests {
    "All three levels of errors reported together" - {
      // Should report segment, entity, and attribute errors
      intercept[Exception](
        structures("segment/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following segments have been removed without extending `Remove` or `Rename("newName")`:
          |  sales
          |  shipping
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  inventory.Warehouse
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  inventory.Product.oldTitle""".stripMargin
    }
  }
}
