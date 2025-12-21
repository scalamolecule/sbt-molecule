package sbtmolecule.migrate.relationship.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait RemoveRelationship_1 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer].remove
  }

  trait Customer {
    val name = oneString
  }
}

trait RemoveRelationship_2 extends DomainStructure {
  trait Order {
    val number = oneInt
  }

  trait Customer {
    val name = oneString
  }
}

object RemoveRelationship extends BaseTest {
  val (before, after) = structures("relationship/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      val annotated    = stripPositions(before)
      // Verify Order entity has customer relationship with .remove marker
      val orderEntity  = annotated.segments.head.entities.find(_.entity == "Order").get
      val customerAttr = orderEntity.attributes.find(_.attribute == "customer").get

      assert(customerAttr.ref.contains("Customer"))
      assert(customerAttr.migration.contains(AttrMigration.Remove))
    }

    "After (clean)" - {
      // Verify Order entity no longer has customer relationship
      val orderEntity = after.segments.head.entities.find(_.entity == "Order").get
      assert(orderEntity.attributes.forall(_.attribute != "customer"))
    }

    "SQL" - {
      val sqlText = sql(before, after)

      // Should drop index, foreign key, then column (in that order)
      assert(sqlText.contains("DROP INDEX IF EXISTS _Order__customer"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _customer"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP COLUMN customer"))

      // Verify order: index drop -> FK drop -> column drop
      val indexPos = sqlText.indexOf("DROP INDEX IF EXISTS _Order__customer")
      val fkPos    = sqlText.indexOf("ALTER TABLE Order_ DROP CONSTRAINT _customer")
      val colPos   = sqlText.indexOf("ALTER TABLE Order_ DROP COLUMN customer")
      assert(indexPos < fkPos)
      assert(fkPos < colPos)
    }

    "All SQL" - {
      sql(before, after) ==>
        """DROP INDEX IF EXISTS _Order__customer;
          |ALTER TABLE Order_ DROP CONSTRAINT _customer;
          |ALTER TABLE Order_ DROP COLUMN customer;""".stripMargin
    }
  }
}
