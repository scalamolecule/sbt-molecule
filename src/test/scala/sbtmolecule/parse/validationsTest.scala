package sbtmolecule.parse

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.ParseAndGenerate
import sbtmolecule.db.Refs.{C, D}
import utest.*

object validations extends DomainStructure {

  trait MandatoryAttr {
    val name    = oneString.mandatory
    val hobbies = setString.mandatory
    val age     = oneInt
  }

  trait MandatoryRefAB {
    val i    = oneInt
    val refA = manyToOne[RefA].mandatory
  }
  trait MandatoryRefB {
    val i    = oneInt
    val refB = manyToOne[RefB].mandatory
  }

  trait RefA {
    val i    = oneInt
    val refB = manyToOne[RefB].mandatory
  }
  trait RefB {
    val i = oneInt
  }
}

object validationsTest extends TestSuite {

  override def tests: Tests = Tests {

    "DSL" - {
      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
      val generator = ParseAndGenerate(path + "validationsTest.scala").generator
      generator.metaDomain ==>
        MetaDomain("sbtmolecule.parse", "validations", List(
          MetaSegment("", List(
            MetaEntity("MandatoryAttr", List(
              MetaAttribute("id"     , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("name"   , OneValue, "String", Nil, None, None, None, None, List("mandatory"), None, Nil, Nil, Nil, None),
              MetaAttribute("hobbies", SetValue, "String", Nil, None, None, None, None, List("mandatory"), None, Nil, Nil, Nil, None),
              MetaAttribute("age"    , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None)
            ), List(), List("name", "hobbies"), List(), false, None),

            MetaEntity("MandatoryRefAB", List(
              MetaAttribute("id"  , OneValue, "ID" , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"   , OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("refA", OneValue, "ID" , Nil, Some("RefA"), Some("MandatoryRefABs"), Some("ManyToOne"), None, List("mandatory"), None, Nil, Nil, Nil, None)
            ), List(), List(), List("refA" -> "RefA"), false, None),

            MetaEntity("MandatoryRefB", List(
              MetaAttribute("id"  , OneValue, "ID" , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"   , OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("refB", OneValue, "ID" , Nil, Some("RefB"), Some("MandatoryRefBs"), Some("ManyToOne"), None, List("mandatory"), None, Nil, Nil, Nil, None)
            ), List(), List(), List("refB" -> "RefB"), false, None),

            MetaEntity("RefA", List(
              MetaAttribute("id"             , OneValue, "ID" , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"              , OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("refB"           , OneValue, "ID" , Nil, Some("RefB"), Some("RefAs"), Some("ManyToOne"), None, List("mandatory"), None, Nil, Nil, Nil, None),
              MetaAttribute("MandatoryRefABs", SetValue, "ID" , Nil, Some("MandatoryRefAB"), Some("refA"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("MandatoryRefAB"), List(), List("refB" -> "RefB"), false, None),

            MetaEntity("RefB", List(
              MetaAttribute("id"            , OneValue, "ID" , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"             , OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("MandatoryRefBs", SetValue, "ID" , Nil, Some("MandatoryRefB"), Some("refB"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("RefAs"         , SetValue, "ID" , Nil, Some("RefA"), Some("refB"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("MandatoryRefB", "RefA"), List(), List(), false, None)
          ))
        ))



      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
    }
  }
}
