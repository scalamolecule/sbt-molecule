//package sbtmolecule.parse
//
//import molecule.DomainStructure
//import molecule.base.metaModel.*
//import molecule.core.dataModel.*
//import sbtmolecule.ParseAndGenerate
//import utest.*
//
//
//object PublishedBooks extends DomainStructure {
//
//  trait Publisher {
//    val name = oneString
//
//    // Publishers currently published books
////    val books = many[Book]
//
//    // Publisher.name.Books.title
//    // Publisher.name.Books.*(Book.title)
//
//
//
//    // Creating Publisher and Books at the same time doesn't make sense
//    // But we could allow it - at least useful for tests...
//    // Publisher.name.Books.*(Book.title).insert(
//    //   ("Pubbie", List( "Book1", "Book2"))
//    // ).transact
//    // same for save
//    // Publisher.name("Pubbie").Books.title("Book1").save.transact
//  }
//
//  // Books remain if publishers go out of business
//  // Books are entities with their own identity
//  trait Book {
//    val title = oneString
//
//    val publisher = manyToOne[Publisher]
//
//    // .publisher // id of Publisher
//    // .Publisher.name
//
//
//    // Book.title("Screws").publisher(publisherId).save
//    //
//    // Book.title.invoice.insert(
//    //   ("Hammer", publisherId),
//    //   ("Spikes", publisherId),
//    // )
//
//
//    // Not allowed:
//    // Book.title("Screws").Publisher.name(8).save
//    //
//    // Book.title.Publisher.name.insert(
//    //   ("Hammer", 48, 8),
//    //   ("Spikes", 20, 8), // would become a redundant additional invoice!
//    // )
//  }
//}
//
//object oneToMany extends TestSuite {
//
//  override def tests: Tests = Tests {
//
//    "DSL" - {
//      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
//      val generator = ParseAndGenerate(path + "oneToMany.scala").generator
//      generator.metaDomain ==>
//        MetaDomain("sbtmolecule.parse", "PublishedBooks", List(
//          MetaSegment("", List(
//            MetaEntity("Publisher", List(
//              MetaAttribute("id"   , OneValue, "ID"    , Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("name" , OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("books", SetValue, "ID"    , Nil, Some("Book"), None, List("one-to-many"), None, Nil, Nil, Nil, Some("books"))
//            ), List(), List(), List(), false, None),
//
//            MetaEntity("Book", List(
//              MetaAttribute("id"       , OneValue, "ID"    , Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("title"    , OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("publisher", OneValue, "ID"    , Nil, Some("Publisher"), None, Nil, None, Nil, Nil, Nil, None)
//            ), List("Publisher"), List(), List(), false, None)
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
