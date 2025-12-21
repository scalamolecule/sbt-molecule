package sbtmolecule.migrate.relationship.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait OwnerOptionRemove_1 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer].owner
  }

  trait Customer {
    val name = oneString
  }
}

trait OwnerOptionRemove_2 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer]
  }

  trait Customer {
    val name = oneString
  }
}

object OwnerOptionRemove extends BaseTest {
  val (before, after) = structures("relationship/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      val annotated    = stripPositions(before)
      val orderEntity  = annotated.segments.head.entities.find(_.entity == "Order").get
      val customerAttr = orderEntity.attributes.find(_.attribute == "customer").get

      assert(customerAttr.ref.contains("Customer"))
      assert(customerAttr.options.contains("owner"))
    }

    "After (clean)" - {
      val orderEntity  = after.segments.head.entities.find(_.entity == "Order").get
      val customerAttr = orderEntity.attributes.find(_.attribute == "customer").get

      assert(customerAttr.ref.contains("Customer"))
      assert(!customerAttr.options.contains("owner"))
    }

    "SQL" - {
      val sqlText = sql(before, after)

      // Should drop old FK constraint with CASCADE and add new one without CASCADE
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _customer"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _customer FOREIGN KEY (customer) REFERENCES Customer (id);"))
      assert(!sqlText.contains("ON DELETE CASCADE"))

      // Verify order: FK should be dropped before adding new one
      val fkDropPos = sqlText.indexOf("ALTER TABLE Order_ DROP CONSTRAINT _customer")
      val fkAddPos  = sqlText.indexOf("ALTER TABLE Order_ ADD CONSTRAINT _customer FOREIGN KEY (customer) REFERENCES Customer (id);")
      assert(fkDropPos < fkAddPos)
    }

    "All SQL" - {
      sql(before, after) ==>
        """ALTER TABLE Order_ DROP CONSTRAINT _customer;
          |ALTER TABLE Order_ ADD CONSTRAINT _customer FOREIGN KEY (customer) REFERENCES Customer (id);""".stripMargin
    }
  }
}
