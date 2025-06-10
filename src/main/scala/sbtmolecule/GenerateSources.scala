package sbtmolecule

import java.io.File
import molecule.base.ast.*
import sbt.*
import sbtmolecule.db.dsl.DbEntity
import sbtmolecule.db.schema.*
import sbtmolecule.graphql.dsl.GraphqlEntity


object GenerateSources {

  def apply(managedSrcDir: File, metaDomains: Seq[MetaDomain]): Unit = {
    metaDomains.foreach { metaDomain =>
      val endpoint = metaDomain.endpoint
      val pkg      = metaDomain.pkg
      val domain   = metaDomain.domain
      val segments = metaDomain.segments
      val base     = pkg.split('.').toList.foldLeft(managedSrcDir)((dir, pkg) => dir / pkg)
      val dsl      = base / "dsl" / domain
      val schema   = base / "schema" // no need to namespace since we have a small fixed set of schemas per domain

      IO.write(schema / s"${domain}Schema.scala", SchemaBase(metaDomain).get)

      endpoint match {
        case Endpoint.Db =>
          var entityIndex = 0
          var attrIndex   = 0
          for {
            metaSegment <- segments
            metaEntity <- metaSegment.ents
          } yield {
            val entityFile    = pkg.split('.').toList.foldLeft(managedSrcDir)(
              (dir, pkg) => dir / pkg
            ) / "dsl" / domain / s"${metaEntity.ent}.scala"
            val segmentPrefix = if (metaSegment.segment.isEmpty) "" else metaSegment.segment + "_"
            val code          = DbEntity(metaDomain, segmentPrefix, metaEntity, entityIndex, attrIndex).get
            entityIndex += 1
            attrIndex += metaEntity.attrs.length
            IO.write(entityFile, code)
          }

          // Db-specific schemas
          val schemaFile_Datomic: File = schema / s"${domain}Schema_datomic.scala"
          IO.write(schemaFile_Datomic, Schema_Datomic(metaDomain).get)

          val schemaFile_h2: File = schema / s"${domain}Schema_h2.scala"
          IO.write(schemaFile_h2, Schema_H2(metaDomain).get)

          val schemaFile_mariadb: File = schema / s"${domain}Schema_mariadb.scala"
          IO.write(schemaFile_mariadb, Schema_MariaDB(metaDomain).get)

          val schemaFile_mysql: File = schema / s"${domain}Schema_mysql.scala"
          IO.write(schemaFile_mysql, Schema_Mysql(metaDomain).get)

          val schemaFile_postgres: File = schema / s"${domain}Schema_postgres.scala"
          IO.write(schemaFile_postgres, Schema_PostgreSQL(metaDomain).get)

          val schemaFile_sqlite: File = schema / s"${domain}Schema_sqlite.scala"
          IO.write(schemaFile_sqlite, Schema_SQlite(metaDomain).get)


        case Endpoint.GraphQL =>
          var entityIndex   = 0
          var attrIndex     = 0
          val segment       = segments.head
          val segmentPrefix = if (segment.segment.isEmpty) "" else segment.segment + "_"
          segment.ents.foreach {
            case metaEntity@MetaEntity(entity, attrs, _, _, _, _) =>
              val entityFile = dsl / "output" / s"$entity.scala"
              val code       = GraphqlEntity(metaDomain, segmentPrefix, metaEntity, entityIndex, attrIndex).get
              entityIndex += 1
              attrIndex += attrs.length
              IO.write(entityFile, code)
          }

        case Endpoint.REST => // not implemented yet
      }
    }
  }
}
