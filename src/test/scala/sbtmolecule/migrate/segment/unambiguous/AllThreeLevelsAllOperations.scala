package sbtmolecule.migrate.segment.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait AllThreeLevelsAllOperations_1 extends DomainStructure {
  // Segment-level Remove: drops all tables in segment
  object oldSegmentToRemove extends Remove {
    trait EntityA {
      val attr1 = oneString
    }

    trait EntityB {
      val attr2 = oneString
    }
  }

  // Segment-level Rename: renames all tables oldSegment_Entity -> newSegment_Entity
  object oldSegmentToRename extends RenameToDummySegment {
    // Entity-level Remove within renamed segment
    trait EntityToRemove extends Remove {
      val attr3 = oneString
    }

    // Entity-level Rename within renamed segment
    trait OldEntityName extends RenameToDummyEntity {
      // Attribute-level Remove within renamed entity
      val attrToRemove  = oneString.remove
      // Attribute-level Rename within renamed entity
      val oldAttrName   = oneString.rename("newAttrName")
      val unchangedAttr = oneInt
    }

    trait UnchangedEntity {
      val attr4 = oneString
    }
  }

  object stableSegment {
    trait StableEntity {
      val attr5 = oneString
    }
  }
}

trait AllThreeLevelsAllOperations_2 extends DomainStructure {
  // oldSegmentToRemove is completely gone (all tables dropped)

  // oldSegmentToRename is now seg
  object seg {
    // EntityToRemove is gone (table dropped)

    // OldEntityName is now Entity
    trait Entity {
      // attrToRemove is gone (column dropped)
      // oldAttrName is now newAttrName (column renamed)
      val newAttrName   = oneString
      val unchangedAttr = oneInt
    }

    trait UnchangedEntity {
      val attr4 = oneString
    }
  }

  object stableSegment {
    trait StableEntity {
      val attr5 = oneString
    }
  }
}

object AllThreeLevelsAllOperations extends BaseTest {
  val (before, after) = structures("segment/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated) - all 6 operations" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "AllThreeLevelsAllOperations_1", List(
          // Segment with Remove
          MetaSegment("oldSegmentToRemove", List(
            MetaEntity("oldSegmentToRemove_EntityA", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr1", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("oldSegmentToRemove_EntityB", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr2", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), Some(SegmentMigration.Remove)),

          // Segment with Rename
          MetaSegment("oldSegmentToRename", List(
            // Entity with Remove
            MetaEntity("oldSegmentToRename_EntityToRemove", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr3", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Remove)),

            // Entity with Rename and attribute migrations
            MetaEntity("oldSegmentToRename_OldEntityName", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attrToRemove", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove)),
              MetaAttribute("oldAttrName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("newAttrName"))),
              MetaAttribute("unchangedAttr", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Rename("Entity"))),

            MetaEntity("oldSegmentToRename_UnchangedEntity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr4", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), Some(SegmentMigration.Rename("seg"))),

          // Stable segment
          MetaSegment("stableSegment", List(
            MetaEntity("stableSegment_StableEntity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr5", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "AllThreeLevelsAllOperations_2", List(
          // Renamed segment
          MetaSegment("seg", List(
            // Renamed entity
            MetaEntity("seg_Entity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("newAttrName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("unchangedAttr", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),

            MetaEntity("seg_UnchangedEntity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr4", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None),

          // Stable segment
          MetaSegment("stableSegment", List(
            MetaEntity("stableSegment_StableEntity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("attr5", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL - demonstrates all 6 migration operations in correct order" - {
      sql(before) ==> List(
        // 1. Segment-level Remove: DROP all tables in oldSegmentToRemove
        "DROP TABLE oldSegmentToRemove_EntityA;",
        "DROP TABLE oldSegmentToRemove_EntityB;",

        // 2. Segment-level Rename: RENAME all tables oldSegmentToRename_* -> seg_*
        "ALTER TABLE oldSegmentToRename_EntityToRemove RENAME TO seg_EntityToRemove;",
        "ALTER TABLE oldSegmentToRename_OldEntityName RENAME TO seg_OldEntityName;",
        "ALTER TABLE oldSegmentToRename_UnchangedEntity RENAME TO seg_UnchangedEntity;",

        // 3. Entity-level Remove: DROP table (already renamed by segment migration)
        "DROP TABLE seg_EntityToRemove;",

        // 4. Entity-level Rename: RENAME table (within already-renamed segment)
        "ALTER TABLE seg_OldEntityName RENAME TO seg_Entity;",

        // 5. Attribute-level Remove: DROP column (from renamed entity in renamed segment)
        "ALTER TABLE seg_Entity DROP COLUMN attrToRemove;",

        // 6. Attribute-level Rename: RENAME column (from renamed entity in renamed segment)
        "ALTER TABLE seg_Entity RENAME COLUMN oldAttrName TO newAttrName;"
      )
    }
  }
}
