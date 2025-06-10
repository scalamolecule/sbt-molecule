package sbtmolecule.test

import sbtmolecule.{SchemaBase, ParseDefinitionFile}
import utest.*


object RenderDomain extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  lazy val basePath      = projectRoot + "/src/test/scala/sbtmolecule/db/"
  lazy val cardinalities = ParseDefinitionFile(basePath + "Cardinalities.scala").optMetaDomain.get
  lazy val starwars      = ParseDefinitionFile(basePath + "Starwars.scala").optMetaDomain.get
  lazy val types         = ParseDefinitionFile(basePath + "Types.scala").optMetaDomain.get
  lazy val refs          = ParseDefinitionFile(basePath + "Refs.scala").optMetaDomain.get
  lazy val unique        = ParseDefinitionFile(basePath + "Uniques.scala").optMetaDomain.get
  lazy val validation    = ParseDefinitionFile(basePath + "Validation.scala").optMetaDomain.get
  lazy val scopes        = ParseDefinitionFile(basePath + "Scopes.scala").optMetaDomain.get
  lazy val segments      = ParseDefinitionFile(basePath + "Segments.scala").optMetaDomain.get


  override def tests: Tests = Tests {

    "DSL" - {
      //            Dsl(types, "", types.segments.head.ents(0)).get ==> "check" // Types
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
      SchemaBase(types).get ==> "check"

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
