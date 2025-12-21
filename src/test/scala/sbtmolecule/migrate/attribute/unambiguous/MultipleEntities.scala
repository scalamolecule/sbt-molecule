package sbtmolecule.migrate.attribute.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait MultipleEntities_1 extends DomainStructure {
  trait Person {
    val name     = oneString.rename("fullName")
    val age      = oneInt
    val nickname = oneString.remove
  }

  trait Company {
    val title   = oneString.rename("name")
    val revenue = oneInt
    val oldCode = oneString.remove
  }
}

trait MultipleEntities_2 extends DomainStructure {
  trait Person {
    val fullName = oneString
    val age      = oneInt
  }

  trait Company {
    val name    = oneString
    val revenue = oneInt
  }
}

object MultipleEntities extends BaseTest {
  val (before, after) = structures("attribute/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "MultipleEntities_1", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("fullName"))),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("nickname", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("Company", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("name"))),
              MetaAttribute("revenue", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("oldCode", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      stripPositions(after) ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "MultipleEntities_2", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("fullName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("Company", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("revenue", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List(
        "ALTER TABLE Person RENAME COLUMN name TO fullName;",
        "ALTER TABLE Person DROP COLUMN nickname;",
        "ALTER TABLE Company RENAME COLUMN title TO name;",
        "ALTER TABLE Company DROP COLUMN oldCode;"
      )
    }
  }
}
