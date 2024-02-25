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
            var nsIndex = 0
            var attrIndex = 0
            for {
              part <- schema.parts
              ns <- part.nss
            } yield {
              val nsFile     = schema.pkg.split('.').toList.foldLeft(managedDir)(
                (dir, pkg) => dir / pkg
              ) / "dsl" / schema.domain / s"${ns.ns}.scala"
              val partPrefix = if (part.part.isEmpty) "" else part.part + "_"
              val code       = Dsl(schema, partPrefix, ns, nsIndex, attrIndex, scalaVersion).get
              nsIndex += 1
              attrIndex += ns.attrs.length
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

            val schemaFile_h2: File = basePath / s"${schema.domain}Schema_H2.scala"
            IO.write(schemaFile_h2, Schema_H2(schema).get)

            val schemaFile_mariadb: File = basePath / s"${schema.domain}Schema_MariaDB.scala"
            IO.write(schemaFile_mariadb, Schema_MariaDB(schema).get)

            val schemaFile_mysql: File = basePath / s"${schema.domain}Schema_Mysql.scala"
            IO.write(schemaFile_mysql, Schema_Mysql(schema).get)

            val schemaFile_postgres: File = basePath / s"${schema.domain}Schema_PostgreSQL.scala"
            IO.write(schemaFile_postgres, Schema_PostgreSQL(schema).get)

            Seq(
              schemaFile,
              schemaFile_Datomic,
              schemaFile_h2,
              schemaFile_mariadb,
              schemaFile_mysql,
              schemaFile_postgres,
            )
          }

          dslFiles ++ schemaFiles
        }
    }
  }
}
