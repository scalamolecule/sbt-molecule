package sbtmolecule.migrate.attribute.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait MultipleSegments_1 extends DomainStructure {
  object sales {
    trait Customer {
      val name  = oneString.rename("fullName")
      val email = oneString.remove
    }

    trait Order {
      val orderNumber = oneString.rename("orderId")
      val total       = oneInt
    }
  }

  object inventory {
    trait Product {
      val title       = oneString.rename("name")
      val description = oneString
      val oldSku      = oneString.remove
    }

    trait Warehouse {
      val location = oneString.rename("address")
      val capacity = oneInt
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
      val total   = oneInt
    }
  }

  object inventory {
    trait Product {
      val name        = oneString
      val description = oneString
    }

    trait Warehouse {
      val address  = oneString
      val capacity = oneInt
    }
  }
}

object MultipleSegments extends BaseTest {
  val (before, after) = structures("attribute/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "MultipleSegments_1", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("fullName"))),
              MetaAttribute("email", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("orderId"))),
              MetaAttribute("total", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          )),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("name"))),
              MetaAttribute("description", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("oldSku", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("inventory_Warehouse", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("location", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("address"))),
              MetaAttribute("capacity", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "MultipleSegments_2", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("fullName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderId", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("total", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          )),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("description", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("inventory_Warehouse", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("address", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("capacity", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List(
        "ALTER TABLE sales_Customer RENAME COLUMN name TO fullName;",
        "ALTER TABLE sales_Customer DROP COLUMN email;",
        "ALTER TABLE sales_Order RENAME COLUMN orderNumber TO orderId;",
        "ALTER TABLE inventory_Product RENAME COLUMN title TO name;",
        "ALTER TABLE inventory_Product DROP COLUMN oldSku;",
        "ALTER TABLE inventory_Warehouse RENAME COLUMN location TO address;"
      )
    }
  }
}
