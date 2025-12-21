package sbtmolecule.migrate.relationship.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait ComprehensiveMigration_1 extends DomainStructure {
  trait Order {
    val number      = oneInt
    val oldCustomer = manyToOne[Customer].remove // Remove this
    val vendor      = manyToOne[Vendor].rename("supplier") // Rename this
    val shipper     = manyToOne[Shipper] // Add .owner to this
  }

  trait Customer {
    val name = oneString
  }

  trait Vendor {
    val company = oneString
  }

  trait Shipper {
    val code = oneString
  }
}

trait ComprehensiveMigration_2 extends DomainStructure {
  trait Order {
    val number    = oneInt
    // oldCustomer removed
    val supplier  = manyToOne[Vendor] // vendor renamed to supplier
    val shipper   = manyToOne[Shipper].owner // .owner added
    val warehouse = manyToOne[Warehouse] // New relationship
  }

  trait Customer {
    val name = oneString
  }

  trait Vendor {
    val company = oneString
  }

  trait Shipper {
    val code = oneString
  }

  trait Warehouse {
    val location = oneString
  }
}

object ComprehensiveMigration extends BaseTest {
  val (before, after) = structures("relationship/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      val annotated   = stripPositions(before)
      val orderEntity = annotated.segments.head.entities.find(_.entity == "Order").get

      // oldCustomer has .remove
      val oldCustomerAttr = orderEntity.attributes.find(_.attribute == "oldCustomer").get
      assert(oldCustomerAttr.migration.contains(AttrMigration.Remove))

      // vendor has .rename
      val vendorAttr = orderEntity.attributes.find(_.attribute == "vendor").get
      assert(vendorAttr.migration.contains(AttrMigration.Rename("supplier")))
    }

    "After (clean)" - {
      val orderEntity = after.segments.head.entities.find(_.entity == "Order").get

      // oldCustomer is gone
      assert(orderEntity.attributes.forall(_.attribute != "oldCustomer"))

      // supplier exists (was vendor)
      val supplierAttr = orderEntity.attributes.find(_.attribute == "supplier").get
      assert(supplierAttr.ref.contains("Vendor"))

      // shipper has .owner
      val shipperAttr = orderEntity.attributes.find(_.attribute == "shipper").get
      assert(shipperAttr.options.contains("owner"))

      // warehouse is new
      val warehouseAttr = orderEntity.attributes.find(_.attribute == "warehouse").get
      assert(warehouseAttr.ref.contains("Warehouse"))
    }

    "SQL" - {
      val sqlText = sql(before, after)

      // Remove oldCustomer: drop index, drop FK, drop column
      assert(sqlText.contains("DROP INDEX IF EXISTS _Order__oldCustomer"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _oldCustomer"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP COLUMN oldCustomer"))

      // Rename vendor to supplier: drop index, drop FK, rename, add FK, add index
      assert(sqlText.contains("DROP INDEX IF EXISTS _Order__vendor"))
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _vendor"))
      assert(sqlText.contains("ALTER TABLE Order_ ALTER COLUMN vendor RENAME TO supplier"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _supplier FOREIGN KEY (supplier) REFERENCES Vendor"))
      assert(sqlText.contains("CREATE INDEX IF NOT EXISTS _Order__supplier ON Order_ (supplier)"))

      // Add .owner to shipper: drop FK, add FK with CASCADE
      assert(sqlText.contains("ALTER TABLE Order_ DROP CONSTRAINT _shipper"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _shipper FOREIGN KEY (shipper) REFERENCES Shipper (id) ON DELETE CASCADE"))

      // Add warehouse: add column, add FK, add index
      assert(sqlText.contains("ALTER TABLE Order_ ADD COLUMN warehouse BIGINT"))
      assert(sqlText.contains("ALTER TABLE Order_ ADD CONSTRAINT _warehouse FOREIGN KEY (warehouse) REFERENCES Warehouse"))
      assert(sqlText.contains("CREATE INDEX IF NOT EXISTS _Order__warehouse ON Order_ (warehouse)"))
    }

    "All SQL" - {
      sql(before, after) ==>
        """DROP INDEX IF EXISTS _Order__oldCustomer;
          |ALTER TABLE Order_ DROP CONSTRAINT _oldCustomer;
          |ALTER TABLE Order_ DROP COLUMN oldCustomer;
          |DROP INDEX IF EXISTS _Order__vendor;
          |ALTER TABLE Order_ DROP CONSTRAINT _vendor;
          |ALTER TABLE Order_ ALTER COLUMN vendor RENAME TO supplier;
          |ALTER TABLE Order_ ADD CONSTRAINT _supplier FOREIGN KEY (supplier) REFERENCES Vendor (id);
          |CREATE INDEX IF NOT EXISTS _Order__supplier ON Order_ (supplier);
          |ALTER TABLE Order_ DROP CONSTRAINT _shipper;
          |ALTER TABLE Order_ ADD CONSTRAINT _shipper FOREIGN KEY (shipper) REFERENCES Shipper (id) ON DELETE CASCADE;
          |ALTER TABLE Order_ ADD COLUMN warehouse BIGINT;
          |ALTER TABLE Order_ ADD CONSTRAINT _warehouse FOREIGN KEY (warehouse) REFERENCES Warehouse (id);
          |CREATE INDEX IF NOT EXISTS _Order__warehouse ON Order_ (warehouse);
          |ALTER TABLE Customer ADD COLUMN Orders BIGINT ARRAY;
          |ALTER TABLE Customer ADD CONSTRAINT _Orders FOREIGN KEY (Orders) REFERENCES Order (id);
          |CREATE INDEX IF NOT EXISTS _Customer_Orders ON Customer (Orders);
          |ALTER TABLE Vendor ADD COLUMN Orders BIGINT ARRAY;
          |ALTER TABLE Vendor ADD CONSTRAINT _Orders FOREIGN KEY (Orders) REFERENCES Order (id);
          |CREATE INDEX IF NOT EXISTS _Vendor_Orders ON Vendor (Orders);
          |ALTER TABLE Shipper ADD COLUMN Orders BIGINT ARRAY;
          |ALTER TABLE Shipper ADD CONSTRAINT _Orders FOREIGN KEY (Orders) REFERENCES Order (id) ON DELETE CASCADE;
          |CREATE INDEX IF NOT EXISTS _Shipper_Orders ON Shipper (Orders);
          |ALTER TABLE Warehouse ADD COLUMN Orders BIGINT ARRAY;
          |ALTER TABLE Warehouse ADD CONSTRAINT _Orders FOREIGN KEY (Orders) REFERENCES Order (id);
          |CREATE INDEX IF NOT EXISTS _Warehouse_Orders ON Warehouse (Orders);""".stripMargin
    }
  }
}
