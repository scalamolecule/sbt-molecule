package sbtmolecule.test

import sbtmolecule.ParseAndGenerate
import utest.*


object RenderGraphql extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  lazy val basePath = projectRoot + "/src/test/scala/sbtmolecule/graphql/"
  lazy val starWars = ParseAndGenerate(basePath + "Starwars.scala").graphql


  override def tests: Tests = Tests {

    "DSL" - {

//      starWars.getCode("Query") ==> "check"
      starWars.getCode("ReviewInput") ==> "check"
      //      GraphqlOutput(starwars, starwars.segments(0).ents(0)).get ==> "check"
    }
  }
}
