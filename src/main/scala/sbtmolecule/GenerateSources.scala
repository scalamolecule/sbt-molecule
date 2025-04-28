package sbtmolecule

import java.io.File
import molecule.base.ast.MetaDomain
import sbt.*
import sbtmolecule.render.*


object GenerateSources {

  def apply(managedSrcDir: File, metaDomains: Seq[MetaDomain]): Unit = {
    metaDomains.foreach { metaDomain =>

      // DSL files
      var entityIndex = 0
      var attrIndex   = 0
      for {
        metaSegment <- metaDomain.segments
        metaEntity <- metaSegment.ents
      } yield {
        val entityFile    = metaDomain.pkg.split('.').toList.foldLeft(managedSrcDir)(
          (dir, pkg) => dir / pkg
        ) / "dsl" / metaDomain.domain / s"${metaEntity.ent}.scala"
        val segmentPrefix = if (metaSegment.segment.isEmpty) "" else metaSegment.segment + "_"
        val code          = Dsl(metaDomain, segmentPrefix, metaEntity, entityIndex, attrIndex).get
        entityIndex += 1
        attrIndex += metaEntity.attrs.length
        IO.write(entityFile, code)
      }


      // Schema files
      val basePath = metaDomain.pkg.split('.').toList.foldLeft(managedSrcDir)((dir, pkg) => dir / pkg) / "schema"

      // Base Schema data that extends all db-specific schemas
      val schemaFile: File = basePath / s"${metaDomain.domain}Schema.scala"
      IO.write(schemaFile, Schema(metaDomain).get)

      // Db-specific schemas
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
    }
  }
}
