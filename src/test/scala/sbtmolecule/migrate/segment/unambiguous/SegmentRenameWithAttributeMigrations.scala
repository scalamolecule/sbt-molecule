package sbtmolecule.migrate.segment.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait SegmentRenameWithAttributeMigrations_1 extends DomainStructure {
  object sales extends RenameToDummySegment {
    trait Customer {
      val firstName = oneString.rename("fullName")
      val age       = oneInt
    }

    trait Order {
      val orderNum = oneString.rename("orderNumber")
      val status   = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

trait SegmentRenameWithAttributeMigrations_2 extends DomainStructure {
  object seg {
    trait Customer {
      val fullName = oneString
      val age      = oneInt
    }

    trait Order {
      val orderNumber = oneString
      val status      = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object SegmentRenameWithAttributeMigrations extends BaseTest {
  val (before, after) = structures("segment/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRenameWithAttributeMigrations_1", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("firstName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("fullName"))),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNum", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("orderNumber"))),
              MetaAttribute("status", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
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
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRenameWithAttributeMigrations_2", List(
          MetaSegment("seg", List(
            MetaEntity("seg_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("fullName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("seg_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("status", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
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

    "SQL - segment rename followed by attribute migrations" - {
      // Segment rename happens first, then attribute migrations are applied to the renamed tables
      sql(before) ==> List(
        "ALTER TABLE sales_Customer RENAME TO seg_Customer;",
        "ALTER TABLE sales_Order RENAME TO seg_Order;",
        "ALTER TABLE seg_Customer RENAME COLUMN firstName TO fullName;",
        "ALTER TABLE seg_Order RENAME COLUMN orderNum TO orderNumber;"
      )
    }
  }
}
