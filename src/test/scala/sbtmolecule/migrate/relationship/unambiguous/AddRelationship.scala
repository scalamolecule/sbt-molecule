package sbtmolecule.migrate.relationship.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait AddRelationship_1 extends DomainStructure {
  trait Order {
    val number = oneInt
  }

  trait Customer {
    val name = oneString
  }
}

trait AddRelationship_2 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer]
  }

  trait Customer {
    val name = oneString
  }
}

object AddRelationship extends BaseTest {
  val (before, after) = structures("relationship/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      val annotated   = stripPositions(before)
      // Verify Order entity has no customer relationship yet
      val orderEntity = annotated.segments.head.entities.find(_.entity == "Order").get
      assert(orderEntity.attributes.forall(_.attribute != "customer"))
    }

    "After (clean)" - {
      // Verify Order entity now has customer relationship
      val orderEntity  = after.segments.head.entities.find(_.entity == "Order").get
      val customerAttr = orderEntity.attributes.find(_.attribute == "customer").get

      assert(customerAttr.ref.contains("Customer"))
      assert(customerAttr.relationship.contains(ManyToOne))
    }

    "SQL" - {
      val sqlText = sql(before, after)

      // Should add column, foreign key, and index (Order is reserved keyword so becomes Order_)
      assert(sqlText.contains("ALTER TABLE Order_ ADD COLUMN customer BIGINT"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _customer FOREIGN KEY (customer) REFERENCES Customer"))
      assert(sqlText.contains("CREATE INDEX IF NOT EXISTS _Order__customer ON Order_ (customer)"))
    }

    "All SQL" - {
      sql(before, after) ==>
        """ALTER TABLE Order_ ADD COLUMN customer BIGINT;
          |ALTER TABLE Order_ ADD CONSTRAINT _customer FOREIGN KEY (customer) REFERENCES Customer (id);
          |CREATE INDEX IF NOT EXISTS _Order__customer ON Order_ (customer);
          |ALTER TABLE Customer ADD COLUMN Orders BIGINT ARRAY;
          |ALTER TABLE Customer ADD CONSTRAINT _Orders FOREIGN KEY (Orders) REFERENCES Order (id);
          |CREATE INDEX IF NOT EXISTS _Customer_Orders ON Customer (Orders);""".stripMargin
    }
  }
}
