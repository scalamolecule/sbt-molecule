package sbtmolecule.parse

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.db.resolvers.Db_H2
import sbtmolecule.render.RenderDomain.types
import sbtmolecule.{GenerateSourceFiles_db, ParseAndGenerate}
import utest.*

object Accounting extends DomainStructure {

  trait Invoice {
    val no    = oneInt
    val date  = oneLocalDate
    val total = oneInt
  }

  trait InvoiceLine {
    val invoice   = manyToOne[Invoice].oneToMany("Lines").owner
    val qty       = oneInt
    val product   = oneString
    val unitPrice = oneInt
    val lineTotal = oneInt
  }
}


object AccountingTest extends TestSuite {

  override def tests: Tests = Tests {

    "MetaDomain" - {
      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
      val generator = ParseAndGenerate(path + getClass.getSimpleName.dropRight(1) + ".scala").generator
      generator.metaDomain ==>
        MetaDomain("sbtmolecule.parse", "Accounting", List(
          MetaSegment("", List(
            MetaEntity("Invoice", List(
              MetaAttribute("id"   , OneValue, "ID"       , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("no"   , OneValue, "Int"      , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("date" , OneValue, "LocalDate", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("total", OneValue, "Int"      , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Lines", SetValue, "ID"       , Nil, Some("InvoiceLine"), Some("invoice"), Some(OneToMany), None, List("owned"), None, Nil, Nil, Nil, None)
            ), List("InvoiceLine"), List(), List(), false, None),

            MetaEntity("InvoiceLine", List(
              MetaAttribute("id"       , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("invoice"  , OneValue, "ID"    , Nil, Some("Invoice"), Some("Lines"), Some(ManyToOne), None, List("owner"), None, Nil, Nil, Nil, None),
              MetaAttribute("qty"      , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("product"  , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("unitPrice", OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("lineTotal", OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None)
            ), List(), List(), List(), false, None)
          ))
        ))

      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
      //      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(2))

      println(Db_H2(generator.metaDomain).getSQL)
    }
  }
}
