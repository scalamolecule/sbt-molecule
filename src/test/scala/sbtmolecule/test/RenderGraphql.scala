package sbtmolecule.test

import molecule.base.ast.MetaDomain
import sbtmolecule.ParseDefinitionFile
import sbtmolecule.graphql.dsl.GraphqlEntity
import utest.*


object RenderGraphql extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  lazy val basePath: String     = projectRoot + "/src/test/scala/sbtmolecule/graphql/"
  lazy val starwars: MetaDomain = ParseDefinitionFile(basePath + "Starwars.scala").optMetaDomain.get




  override def tests: Tests = Tests {

    "DSL" - {

      GraphqlEntity(starwars, "", starwars.segments(0).ents(0)).get ==> "check"
    }
  }
}
