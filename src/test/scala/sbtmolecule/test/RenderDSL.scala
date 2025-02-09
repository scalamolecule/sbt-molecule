package sbtmolecule.test

import sbtmolecule.ParseDomain
import sbtmolecule.render.*
import utest.*


object RenderDSL extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  lazy val basePath      = projectRoot + "/src/test/scala/sbtmolecule/domain/"
  lazy val cardinalities = ParseDomain(basePath + "Cardinalities.scala", "213")
  lazy val starwars      = ParseDomain(basePath + "Starwars.scala", "213")
  lazy val types         = ParseDomain(basePath + "Types.scala", "213")
  lazy val refs          = ParseDomain(basePath + "Refs.scala", "213")
  lazy val unique        = ParseDomain(basePath + "Uniques.scala", "213")
  lazy val validation    = ParseDomain(basePath + "Validation.scala", "213")
  lazy val scopes        = ParseDomain(basePath + "Scopes.scala", "213")
  lazy val segments      = ParseDomain(basePath + "Segments.scala", "213")


  override def tests: Tests = Tests {

    "DSL" - {
      //            Dsl(types, "", types.segments.head.ents(0), scalaVersion = "213").get ==> "check"
      //      Dsl(types, "", types.segments.head.ents(1)).get ==> "check"
      //      Dsl(types, "", types.segments.head.ents(2)).get ==> "check"
      //      Dsl(validation, "", validation.segments.head.ents(11)).get ==> "check"
      //      Dsl(refs, "", refs.segments.head.ents(0)).get ==> "check"
      //      Dsl(scopes, "", scopes.segments(0).ents(0)).get ==> "check"
      Dsl(segments, "", segments.segments(0).ents(0)).get ==> "check"
      //      Dsl(unique, "", unique.segments(0).ents(0)).get ==> "check"
      //      Dsl(validation, "", validation.segments(0).ents(0)).get ==> "check"
      //      Dsl(validation, "", validation.segments(0).ents(3)).get ==> "check" // Constants
      //      Dsl(validation, "", validation.segments(0).ents(4)).get ==> "check" // Variables
      //      Dsl(cardinalities, "", cardinalities.segments(0).ents(0)).get ==> "check"
      //      Dsl(starwars, "", starwars.segments(0).ents(2)).get ==> "check"
      //
      //      validation ==> "check"
      //      validation.segments.head.ents(3) ==> "check"
      //      validation.segments.head.ents(4) ==> "check"
      //      validation.segments.head.ents(12) ==> "check"
      //      validation.attrMap() ==> "check"
      //      validation.segments.head ==> "check"
      //
      //
      //      Dsl(types, "", types.segments.head.ents(0)).get ==> "check"
      //      Dsl(types, "", types.segments.head.ents(1), 1, 36).get ==> "check"
      //
      //      Dsl(unique, "", unique.segments.head.ents(0)).get ==> "check"
      //      Dsl(unique, "", unique.segments.head.ents(1)).get ==> "check"
      //
      //      Dsl(refs, "", refs.segments.head.ents(0)).get ==> "check"
      //      Dsl(refs, "", refs.segments.head.ents(1)).get ==> "check"
      //
      //      Schema(types).get ==> "check"

      //      Schema_Datomic(types).get ==> "check"
      //      Schema_Datomic(refs).get ==> "check"
      //            Schema_MariaDB(types).get ==> "check"
      //      Schema_MariaDB(starwars).get ==> "check"
      //      Schema_SQlite(types).get ==> "check"
      //      Schema_PostgreSQL(types).get ==> "check"
      //      Schema_H2(types).get ==> "check"
      //      Schema_H2(refs).get ==> "check"
      //      Schema_Mysql(refs).get ==> "check"
      //      Schema_H2(unique).get ==> "check"
    }
  }
}
