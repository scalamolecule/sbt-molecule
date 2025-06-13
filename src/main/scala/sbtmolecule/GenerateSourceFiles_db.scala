package sbtmolecule

import java.io.File
import molecule.base.metaModel.*
import sbt.*
import sbtmolecule.db.dsl.DbTable
import sbtmolecule.db.schema.*


case class GenerateSourceFiles_db(metaDomain: MetaDomain) {

  def getCode(metaEntity: MetaEntity): String = {
    DbTable(metaDomain, metaEntity, 0, 0).get
  }

  def generate(srcManaged: File): Unit = {
    var entityIndex = 0
    var attrIndex   = 0
    val pkg         = metaDomain.pkg
    val domain      = metaDomain.domain
    val segments    = metaDomain.segments
    val base        = pkg.split('.').toList.foldLeft(srcManaged)((dir, pkg) => dir / pkg)
    val domainDir   = base / "dsl" / domain

    for {
      metaSegment <- segments
      metaEntity <- metaSegment.ents
    } yield {
      //      val segmentPrefix = if (metaSegment.segment.isEmpty) "" else metaSegment.segment + "_"
      //      val entityCode    = DbTable(metaDomain, segmentPrefix, metaEntity, entityIndex, attrIndex).get
      val entityCode = DbTable(metaDomain, metaEntity, entityIndex, attrIndex).get
      entityIndex += 1
      attrIndex += metaEntity.attrs.length
      IO.write(domainDir / s"${metaEntity.ent}.scala", entityCode)
    }

    val schema = base / "schema"
    IO.write(schema / s"${domain}Schema.scala", SchemaBase(metaDomain).get)
    IO.write(schema / s"${domain}Schema_datomic.scala", Schema_Datomic(metaDomain).get)
    IO.write(schema / s"${domain}Schema_h2.scala", Schema_H2(metaDomain).get)
    IO.write(schema / s"${domain}Schema_mariadb.scala", Schema_MariaDB(metaDomain).get)
    IO.write(schema / s"${domain}Schema_mysql.scala", Schema_Mysql(metaDomain).get)
    IO.write(schema / s"${domain}Schema_postgres.scala", Schema_PostgreSQL(metaDomain).get)
    IO.write(schema / s"${domain}Schema_sqlite.scala", Schema_SQlite(metaDomain).get)
  }
}
