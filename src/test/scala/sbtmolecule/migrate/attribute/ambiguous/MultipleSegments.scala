package sbtmolecule.migrate.attribute.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MultipleSegments_1 extends DomainStructure {
  object sales {
    trait Customer {
      val firstName = oneString
      val lastName  = oneString
    }

    trait Order {
      val orderNumber = oneString
      val reference   = oneString
    }
  }

  object inventory {
    trait Product {
      val title       = oneString
      val description = oneString
    }

    trait Warehouse {
      val location = oneString
      val code     = oneString
    }
  }
}

trait MultipleSegments_2 extends DomainStructure {
  object sales {
    trait Customer {
      val fullName = oneString
    }

    trait Order {
      val orderId = oneString
    }
  }

  object inventory {
    trait Product {
      val name = oneString
    }

    trait Warehouse {
      val address = oneString
    }
  }
}

object MultipleSegments extends BaseTest {

  override def tests: Tests = Tests {
    "Error thrown during annotation" - {
      // Multiple segments, entities, and attributes without migration commands
      // sales.Customer: 'firstName' and 'lastName' disappeared
      // sales.Order: 'orderNumber' and 'reference' disappeared
      // inventory.Product: 'title' and 'description' disappeared
      // inventory.Warehouse: 'location' and 'code' disappeared
      intercept[Exception](
        structures("attribute/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following attributes have been removed without calling `.remove` or `.rename("newName")`:
          |  sales.Customer.firstName
          |  sales.Customer.lastName
          |  sales.Order.orderNumber
          |  sales.Order.reference
          |  inventory.Product.title
          |  inventory.Product.description
          |  inventory.Warehouse.location
          |  inventory.Warehouse.code""".stripMargin
    }
  }
}
