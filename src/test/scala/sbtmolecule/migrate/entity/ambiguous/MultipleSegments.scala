package sbtmolecule.migrate.entity.ambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import sbtmolecule.migration.MigrationDetector
import utest.*


trait MultipleSegments_1 extends DomainStructure {
  object sales {
    trait Customer {
      val firstName = oneString
      val lastName  = oneString
    }

    trait Order {
      val orderNumber = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }

    trait Warehouse {
      val location = oneString
    }
  }
}

trait MultipleSegments_2 extends DomainStructure {
  object sales {
    trait Order {
      val orderNumber = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object MultipleSegments extends BaseTest {
  override def tests: Tests = Tests {
    "Ambiguous entity changes in segments throw error" - {
      intercept[Exception](
        structures("entity/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following entities have been removed without extending `Remove` or `Rename("newName")`:
          |  sales.Customer
          |  inventory.Warehouse""".stripMargin
    }
  }
}
