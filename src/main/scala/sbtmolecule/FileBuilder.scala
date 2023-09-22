package sbtmolecule

import java.io.File
import sbt.*
import sbtmolecule.parse.DataModel2MetaSchema
import sbtmolecule.render.*


object FileBuilder {

  def apply(sourceDir: File, managedDir: File, dataModelDirs: Seq[String], scalaVersion: String): Seq[File] = {

    // Loop domain directories
    dataModelDirs.flatMap { dataModelDir =>
      val dataModelDirs: Array[File] = sbt.IO.listFiles(sourceDir / dataModelDir)
      assert(
        dataModelDirs.exists(f => f.isDirectory && f.getName == "dataModel"),
        s"\nMissing `dataModel` package inside moleculeDataModelPaths:\n" + sourceDir / dataModelDir
      )

      // Loop data model files in each domain directory
      sbt.IO.listFiles(sourceDir / dataModelDir / "dataModel")
        .filter(f => f.isFile)
        .flatMap { dataModelFile =>
          val schema = DataModel2MetaSchema(dataModelFile.getPath, scalaVersion)

          val dslFiles: Seq[File] = {
            for {
              part <- schema.parts
              ns <- part.nss
            } yield {
              val nsFile     = schema.pkg.split('.').toList.foldLeft(managedDir)(
                (dir, pkg) => dir / pkg
              ) / "dsl" / schema.domain / s"${ns.ns}.scala"
              val partPrefix = if (part.part.isEmpty) "" else part.part + "_"
              val code       = Dsl(schema, partPrefix, ns).get
              IO.write(nsFile, code)
              nsFile
            }
          }

          val schemaFiles: Seq[File] = {
            val basePath = schema.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema"

            val schemaFile: File = basePath / s"${schema.domain}Schema.scala"
            IO.write(schemaFile, Schema(schema).get)

            val schemaFile_Datomic: File = basePath / s"${schema.domain}Schema_Datomic.scala"
            IO.write(schemaFile_Datomic, Schema_Datomic(schema).get)

            val schemaFile_Sql: File = basePath / s"${schema.domain}Schema_Sql.scala"
            IO.write(schemaFile_Sql, Schema_Sql2(schema).get)

            Seq(schemaFile, schemaFile_Datomic, schemaFile_Sql)
          }

          dslFiles ++ schemaFiles
        }
    }
  }
}
