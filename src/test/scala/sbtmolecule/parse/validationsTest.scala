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
        42


      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
    }
  }
}
