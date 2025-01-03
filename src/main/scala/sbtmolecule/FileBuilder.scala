package sbtmolecule

import java.io.File
import sbt.*
import sbtmolecule.render.*


object FileBuilder {

  def apply(sourceDir: File, managedDir: File, domainDirs: Seq[String], scalaVersion: String): Seq[File] = {

    // Loop domain directories
    domainDirs.flatMap { domainDir =>
      val definitionDir = sourceDir / domainDir
      assert(
        definitionDir.isDirectory,
        s"\nPath in moleculeDomainPaths is not a directory:\n" + definitionDir
      )

      // Loop Data Structure files in each domain directory
      sbt.IO.listFiles(definitionDir)
        .filter(f => f.isFile)
        .flatMap { domainFile =>
          val metaDomain = ParseDomain(domainFile.getPath, scalaVersion)

          val dslFiles: Seq[File] = {
            var entityIndex = 0
            var attrIndex   = 0
            for {
              metaSegment <- metaDomain.segments
              metaEntity <- metaSegment.ents
            } yield {
              val entityFile    = metaDomain.pkg.split('.').toList.foldLeft(managedDir)(
                (dir, pkg) => dir / pkg
              ) / "dsl" / metaDomain.domain / s"${metaEntity.ent}.scala"
              val segmentPrefix = if (metaSegment.segment.isEmpty) "" else metaSegment.segment + "_"
              val code          = Dsl(metaDomain, segmentPrefix, metaEntity, entityIndex, attrIndex, scalaVersion).get
              entityIndex += 1
              attrIndex += metaEntity.attrs.length
              IO.write(entityFile, code)
              entityFile
            }
          }

          val schemaFiles: Seq[File] = {
            val basePath = metaDomain.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema"

            val schemaFile: File = basePath / s"${metaDomain.domain}Schema.scala"
            IO.write(schemaFile, Schema(metaDomain).get)

            val schemaFile_Datomic: File = basePath / s"${metaDomain.domain}Schema_datomic.scala"
            IO.write(schemaFile_Datomic, Schema_Datomic(metaDomain).get)

            val schemaFile_h2: File = basePath / s"${metaDomain.domain}Schema_h2.scala"
            IO.write(schemaFile_h2, Schema_H2(metaDomain).get)

            val schemaFile_mariadb: File = basePath / s"${metaDomain.domain}Schema_mariadb.scala"
            IO.write(schemaFile_mariadb, Schema_MariaDB(metaDomain).get)

            val schemaFile_mysql: File = basePath / s"${metaDomain.domain}Schema_mysql.scala"
            IO.write(schemaFile_mysql, Schema_Mysql(metaDomain).get)

            val schemaFile_postgres: File = basePath / s"${metaDomain.domain}Schema_postgres.scala"
            IO.write(schemaFile_postgres, Schema_PostgreSQL(metaDomain).get)

            val schemaFile_sqlite: File = basePath / s"${metaDomain.domain}Schema_sqlite.scala"
            IO.write(schemaFile_sqlite, Schema_SQlite(metaDomain).get)

            Seq(
              schemaFile,
              schemaFile_Datomic,
              schemaFile_h2,
              schemaFile_mariadb,
              schemaFile_mysql,
              schemaFile_postgres,
              schemaFile_sqlite,
            )
          }

          dslFiles ++ schemaFiles
        }
    }
  }
}
