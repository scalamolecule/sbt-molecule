package sbtmolecule.migrate.relationship.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait RenameRelationship_1 extends DomainStructure {
  trait Order {
    val number   = oneInt
    val customer = manyToOne[Customer].rename("buyer")
  }

  trait Customer {
    val name = oneString
  }
}

trait RenameRelationship_2 extends DomainStructure {
  trait Order {
    val number = oneInt
    val buyer  = manyToOne[Customer]
  }

  trait Customer {
    val name = oneString
  }
}

object RenameRelationship extends BaseTest {
  val (before, after) = structures("relationship/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      val annotated    = stripPositions(before)
      val orderEntity  = annotated.segments.head.entities.find(_.entity == "Order").get
      val customerAttr = orderEntity.attributes.find(_.attribute == "customer").get

      assert(customerAttr.ref.contains("Customer"))
      assert(customerAttr.migration.contains(AttrMigration.Rename("buyer")))
    }

    "After (clean)" - {
      val orderEntity = after.segments.head.entities.find(_.entity == "Order").get
      val buyerAttr   = orderEntity.attributes.find(_.attribute == "buyer").get

      assert(buyerAttr.ref.contains("Customer"))
      assert(buyerAttr.relationship.contains(ManyToOne))
    }

    "SQL" - {
      val sqlText = sql(before, after)

      // Should drop index, drop FK, rename column, add FK, add index
      assert(sqlText.contains("DROP INDEX IF EXISTS _Order__customer"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _customer"))
      assert(sqlText.contains("ALTER TABLE Order_ ALTER COLUMN customer RENAME TO buyer"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _buyer FOREIGN KEY (buyer) REFERENCES Customer"))
      assert(sqlText.contains("CREATE INDEX IF NOT EXISTS _Order__buyer ON Order_ (buyer)"))

      // Verify order
      val indexDropPos = sqlText.indexOf("DROP INDEX IF EXISTS _Order__customer")
      val fkDropPos    = sqlText.indexOf("ALTER TABLE Order_ DROP CONSTRAINT _customer")
      val renamePos    = sqlText.indexOf("ALTER TABLE Order_ ALTER COLUMN customer RENAME TO buyer")
      val fkAddPos     = sqlText.indexOf("ALTER TABLE Order_ ADD CONSTRAINT _buyer FOREIGN KEY (buyer) REFERENCES Customer")
      val indexAddPos  = sqlText.indexOf("CREATE INDEX IF NOT EXISTS _Order__buyer ON Order_ (buyer)")

      assert(indexDropPos < fkDropPos)
      assert(fkDropPos < renamePos)
      assert(renamePos < fkAddPos)
      assert(fkAddPos < indexAddPos)
    }

    "All SQL" - {
      sql(before, after) ==>
        """DROP INDEX IF EXISTS _Order__customer;
          |ALTER TABLE Order_ DROP CONSTRAINT _customer;
          |ALTER TABLE Order_ ALTER COLUMN customer RENAME TO buyer;
          |ALTER TABLE Order_ ADD CONSTRAINT _buyer FOREIGN KEY (buyer) REFERENCES Customer (id);
          |CREATE INDEX IF NOT EXISTS _Order__buyer ON Order_ (buyer);""".stripMargin
    }
  }
}
