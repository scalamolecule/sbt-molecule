//package sbtmolecule.render
//
//import sbtmolecule.ParseAndGenerate
//import utest.*
//
//
//object RenderGraphql extends TestSuite {
//  val projectRoot = System.getProperty("user.dir")
//  lazy val basePath = projectRoot + "/src/test/scala/sbtmolecule/graphql/"
//  lazy val starWars = ParseAndGenerate(basePath + "Starwars.scala").graphql
//
//
//  override def tests: Tests = Tests {
//
//    "DSL" - {
//      //      starWars.printCode("Episode")
//      starWars.printCode("Character")
//      //      starWars.printCode("Droid")
//      //      starWars.printCode("Query")
//      //      starWars.printCode("ReviewInput")
//      //      starWars.printCode("Date")
//      //      starWars.printCode("Mutation")
//      //      starWars.printCode("Subscription")
//    }
//  }
//}
