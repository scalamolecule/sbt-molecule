package sbtmolecule.test

import sbtmolecule.parse.DataModel2MetaSchema
import sbtmolecule.render.*
import utest.*


object RenderDSL extends TestSuite {
  val projectRoot = System.getProperty("user.dir")
  //  lazy val scala2path       = projectRoot + "/base/src/test/scala-2/molecule/base/dataModel/"
  //  lazy val scala3path       = projectRoot + "/base/src/test/scala-3/molecule/base/dataModel/"
  //  lazy val schemaPartitions = DataModel2MetaSchema(scala2path + "Partitions.scala", "213")
  //  lazy val schema3          = DataModel2MetaSchema(scala3path + "Nss3.scala", "3")

  //  lazy val basePath      = projectRoot + "/base/jvm/src/test/scala-2/molecule/base/dataModel/"
  //  lazy val basePath      = projectRoot + "/sbtmolecule/dataModel/"
  lazy val basePath         = projectRoot + "/src/test/scala/sbtmolecule/dataModel/"
  lazy val CardinalitiesNss = DataModel2MetaSchema(basePath + "Cardinalities.scala", "213")
  lazy val StarwarsNss      = DataModel2MetaSchema(basePath + "Starwars.scala", "213")
  lazy val typesNss         = DataModel2MetaSchema(basePath + "Types.scala", "213")
  lazy val refsNss          = DataModel2MetaSchema(basePath + "Refs.scala", "213")
  lazy val uniqueNss        = DataModel2MetaSchema(basePath + "Uniques.scala", "213")
  lazy val validationNss    = DataModel2MetaSchema(basePath + "Validation.scala", "213")
  lazy val partitionsNss    = DataModel2MetaSchema(basePath + "Partitions.scala", "213")
  lazy val partitions2Nss   = DataModel2MetaSchema(basePath + "Partitions2.scala", "213")


  override def tests: Tests = Tests {

    "DSL" - {

      Dsl(typesNss, "", typesNss.parts.head.nss(0), scalaVersion = "213").get ==> "check"
      //      Dsl(typesNss, "", typesNss.parts.head.nss(1)).get ==> "check"
      //      Dsl(typesNss, "", typesNss.parts.head.nss(2)).get ==> "check"
      //            Dsl(validationNss, "", validationNss.parts.head.nss(11)).get ==> "check"
      //      Dsl(refsNss, "", refsNss.parts.head.nss(0)).get ==> "check"
      //      Dsl(partitionsNss, "accounting_", partitionsNss.parts(0).nss(0)).get ==> "check"
      //      Dsl(partitions2Nss, "", partitions2Nss.parts(0).nss(0)).get ==> "check"
      //      Dsl(partitionsNss, "y_", partitionsNss.parts(1).nss(0)).get ==> "check"
      //      Dsl(partitionsNss, "y_", partitionsNss.parts(1).nss(1)).get ==> "check"
      //      Dsl(validationNss, "", validationNss.parts(0).nss(0)).get ==> "check"
      //      Dsl(validationNss, "", validationNss.parts(0).nss(3)).get ==> "check" // Constants
      //      Dsl(validationNss, "", validationNss.parts(0).nss(4)).get ==> "check" // Variables
      //      Dsl(CardinalitiesNss, "", CardinalitiesNss.parts(0).nss(0)).get ==> "check"
      //      Dsl(StarwarsNss, "", StarwarsNss.parts(0).nss(2)).get ==> "check"
      //
      //      validationNss ==> "check"
      //      validationNss.parts.head.nss(3) ==> "check"
      //      validationNss.parts.head.nss(4) ==> "check"
      //      validationNss.parts.head.nss(12) ==> "check"
      //      validationNss.attrMap() ==> "check"
      //      validationNss.parts.head ==> "check"
      //
      //
            Dsl(typesNss, "", typesNss.parts.head.nss(0)).get ==> "check"
      //      Dsl(typesNss, "", typesNss.parts.head.nss(1), 1, 36).get ==> "check"
      //
      //      Dsl(uniqueNss, "", uniqueNss.parts.head.nss(0)).get ==> "check"
      //      Dsl(uniqueNss, "", uniqueNss.parts.head.nss(1)).get ==> "check"
      //
      //      Dsl(refsNss, "", refsNss.parts.head.nss(0)).get ==> "check"
      //      Dsl(refsNss, "", refsNss.parts.head.nss(1)).get ==> "check"
      //
      //      Schema(typesNss).get ==> "check"
      //      Schema(partitionsNss).get ==> "check"
      //
      //      Schema_Datomic(typesNss).get ==> "check"
      //      Schema_Datomic(refsNss).get ==> "check"
      //
      //      Schema_MariaDB(typesNss).get ==> "check"
      //      Schema_SQlite(typesNss).get ==> "check"
      //      Schema_PostgreSQL(typesNss).get ==> "check"
      //      Schema_H2(typesNss).get ==> "check"
      //      Schema_H2(refsNss).get ==> "check"
      //      Schema_Mysql(refsNss).get ==> "check"
      //      Schema_H2(uniqueNss).get ==> "check"
    }
  }
}
