package sbtmolecule.test

import sbtmolecule.ParseAndGenerate
import utest.*


object RenderDomain extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  lazy val basePath          = projectRoot + "/src/test/scala/sbtmolecule/db/"
  //  lazy val cardinalities = ParseAndGenerate(basePath + "Cardinalities.scala").generate.get
  //  lazy val starwars      = ParseAndGenerate(basePath + "Starwars.scala").generate.get
  lazy val types = ParseAndGenerate(basePath + "Types.scala").dbModel
  //  lazy val refs          = ParseAndGenerate(basePath + "Refs.scala").generate.get
  //  lazy val unique        = ParseAndGenerate(basePath + "Uniques.scala").generate.get
  //  lazy val validation    = ParseAndGenerate(basePath + "Validation.scala").generate.get
  //  lazy val scopes        = ParseAndGenerate(basePath + "Scopes.scala").generate.get
  //  lazy val segments      = ParseAndGenerate(basePath + "Segments.scala").generate.get


  override def tests: Tests = Tests {

    "DSL" - {
      types.getCode(types.dbModel.segments.head.ents(0)) ==> "check" // Types
      //            Dsl(types, "", types.segments.head.ents(1)).get ==> "check" // Refs
      //      Dsl(types, "", types.segments.head.ents(2)).get ==> "check" // Other
      //      Dsl(validation, "", validation.segments.head.ents(11)).get ==> "check"
      //      Dsl(refs, "", refs.segments.head.ents(0)).get ==> "check"
      //      Dsl(scopes, "", scopes.segments(0).ents(0)).get ==> "check"
      //      Dsl(segments, "", segments.segments(0).ents(0)).get ==> "check"
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
      //      SchemaBase(types).get ==> "check"

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
