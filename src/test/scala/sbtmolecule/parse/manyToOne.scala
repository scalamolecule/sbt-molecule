//package sbtmolecule.parse
//
//import molecule.DomainStructure
//import molecule.base.metaModel.*
//import molecule.core.dataModel.*
//import sbtmolecule.render.RenderDomain.types
//import sbtmolecule.{GenerateSourceFiles_db, ParseAndGenerate}
//import utest.*
//
//object Artists extends DomainStructure {
//
//  trait Artist {
//    val name = oneString
//
//    // .Works.price
//    // .Works.*(Work.price)
//
//
//    // Artist would most likely exist before works are added
//    // Strange to define both from this side...
//    // Artist.name.Works.*(Work.title.price).insert(
//    //   ("Bob", List(
//    //     ("Mona", 14),
//    //     ("Hope", 20))
//    //   )
//    // ).transact
//
//    // Doesn't make sense
//    // Artist.name("Bob").Paintings.title("Mona").save
//  }
//
//
//  trait Work {
//    val title  = oneString
//    val price  = oneInt
//    val artist = manyToOne[Artist] // transparently adds reverse ref "Works"
//
//    // .painter
//    // .Painter.name
//
//
//    // Work.title("Mona").price(14).artist(artistId).save.transact
//    //
//    // Work.title.price.artist.insert(
//    //   ("Mona", 14, artistId),
//    //   ("Hope", 20, artistId),
//    // ).transact
//  }
//}
//
//
//object manyToOneTest extends TestSuite {
//
//  override def tests: Tests = Tests {
//
//    "DSL" - {
//      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
//      val generator = ParseAndGenerate(path + "manyToOne.scala").generator
//      generator.metaDomain ==>
//        MetaDomain("sbtmolecule.parse", "Artists", List(
//          MetaSegment("", List(
//            MetaEntity("Artist", List(
//              MetaAttribute("id"   , OneValue, "ID"    , Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("name" , OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("Works", SetValue, "ID"    , Nil, Some("Work"), None, List("one-to-many"), None, Nil, Nil, Nil, Some("artist"))
//            ), List("Work"), List(), List(), false, None),
//
//            MetaEntity("Work", List(
//              MetaAttribute("id"    , OneValue, "ID"    , Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("title" , OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("price" , OneValue, "Int"   , Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("artist", OneValue, "ID"    , Nil, Some("Artist"), None, Nil, None, Nil, Nil, Nil, Some("Works"))
//            ), List(), List(), List(), false, None)
//          ))
//        ))
//
//
//      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
//      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
//      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
//    }
//  }
//}
