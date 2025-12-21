package sbtmolecule.migrate.entity.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait ComprehensiveMigration_1 extends DomainStructure {
  // Entity being renamed with attribute migrations
  trait Person extends RenameToDummyEntity {
    val firstName = oneString.rename("name")
    val lastName  = oneString.remove
    val age       = oneInt
  }

  // Entity being removed (attributes don't need individual migrations)
  trait Company extends Remove {
    val orgName   = oneString
    val employees = oneInt
  }

  // Entity staying but with attribute migrations
  trait Product {
    val title    = oneString.rename("name")
    val sku      = oneString
    val oldPrice = oneInt.remove
  }
}

trait ComprehensiveMigration_2 extends DomainStructure {
  // Person renamed to Dummy
  trait Entity {
    val name = oneString
    val age  = oneInt
  }

  // Company removed (not present)

  // Product stays with changes
  trait Product {
    val name  = oneString
    val sku   = oneString
    val price = oneInt
  }
}

object ComprehensiveMigration extends BaseTest {
  val (before, after) = structures("entity/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.entity.unambiguous", "ComprehensiveMigration_1", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("firstName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("name"))),
              MetaAttribute("lastName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove)),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Rename("Entity"))),
            MetaEntity("Company", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orgName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("employees", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Remove)),
            MetaEntity("Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("name"))),
              MetaAttribute("sku", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("oldPrice", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Remove))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.entity.unambiguous", "ComprehensiveMigration_2", List(
          MetaSegment("", List(
            MetaEntity("Entity", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("age", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("sku", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("price", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List(
        "ALTER TABLE Person RENAME TO Entity;",
        "DROP TABLE Company;",
        "ALTER TABLE Entity RENAME COLUMN firstName TO name;",
        "ALTER TABLE Entity DROP COLUMN lastName;",
        "ALTER TABLE Product RENAME COLUMN title TO name;",
        "ALTER TABLE Product DROP COLUMN oldPrice;"
      )
    }
  }
}
