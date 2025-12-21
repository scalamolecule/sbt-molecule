package sbtmolecule.migrate.segment.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait SegmentRenameWithEntityMigrations_1 extends DomainStructure {
  object sales extends RenameToDummySegment {
    trait Customer extends Remove {
      val name = oneString
    }

    trait Order extends RenameToDummyEntity {
      val orderNumber = oneString
    }

    trait Invoice {
      val invoiceDate = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

trait SegmentRenameWithEntityMigrations_2 extends DomainStructure {
  object seg {
    trait Entity {
      val orderNumber = oneString
    }

    trait Invoice {
      val invoiceDate = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object SegmentRenameWithEntityMigrations extends BaseTest {
  val (before, after) = structures("segment/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRenameWithEntityMigrations_1", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Remove)),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Rename("Entity"))),
            MetaEntity("sales_Invoice", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("invoiceDate", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), Some(SegmentMigration.Rename("seg"))),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRenameWithEntityMigrations_2", List(
          MetaSegment("seg", List(
            MetaEntity("seg_Entity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("seg_Invoice", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("invoiceDate", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL - segment rename happens, entity migrations are processed within the renamed segment context" - {
      sql(before) ==> List(
        "ALTER TABLE sales_Customer RENAME TO seg_Customer;",
        "ALTER TABLE sales_Order RENAME TO seg_Order;",
        "ALTER TABLE sales_Invoice RENAME TO seg_Invoice;",
        "DROP TABLE seg_Customer;",
        "ALTER TABLE seg_Order RENAME TO seg_Entity;"
      )
    }
  }
}
