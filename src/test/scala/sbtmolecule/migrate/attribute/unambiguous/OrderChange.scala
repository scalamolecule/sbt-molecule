package sbtmolecule.migrate.attribute.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.OneValue
import sbtmolecule.migrate.BaseTest
import utest.*


trait OrderChange_1 extends DomainStructure {
  trait Person {
    val name     = oneString
    val nickname = oneString
  }
}

trait OrderChange_2 extends DomainStructure {
  trait Person {
    val nickname = oneString
    val name     = oneString
  }
}

object OrderChange extends BaseTest {
  val (before, after) = structures("attribute/unambiguous/")


  override def tests: Tests = Tests {
    "Before" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "OrderChange_1", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("nickname", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "After" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "OrderChange_2", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("nickname", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List() // No SQL needed since only order of attributes changed
    }
  }
}
