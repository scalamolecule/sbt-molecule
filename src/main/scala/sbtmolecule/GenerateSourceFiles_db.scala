package sbtmolecule

import java.io.File
import molecule.core.model.*
import sbt.*
import sbtmolecule.db.dsl.DbTable
import sbtmolecule.db.schema.*


case class GenerateSourceFiles_db(dbModel: DbModel) {

  def getCode(dbEntity: DbEntity): String = {
    DbTable(dbModel, dbEntity, 0, 0).get
  }

  def generate(srcManaged: File): Unit = {
    var entityIndex = 0
    var attrIndex   = 0
    val pkg         = dbModel.pkg
    val domain      = dbModel.domain
    val segments    = dbModel.segments
    val base        = pkg.split('.').toList.foldLeft(srcManaged)((dir, pkg) => dir / pkg)
    val domainDir   = base / "dsl" / domain

    for {
      dbSegment <- segments
      dbEntity <- dbSegment.ents
    } yield {
      //      val segmentPrefix = if (dbSegment.segment.isEmpty) "" else dbSegment.segment + "_"
      //      val entityCode    = DbTable(dbModel, segmentPrefix, dbEntity, entityIndex, attrIndex).get
      val entityCode = DbTable(dbModel, dbEntity, entityIndex, attrIndex).get
      entityIndex += 1
      attrIndex += dbEntity.attrs.length
      IO.write(domainDir / s"${dbEntity.ent}.scala", entityCode)
    }

    val schema = base / "schema"
    IO.write(schema / s"${domain}Schema.scala", SchemaBase(dbModel).get)
    IO.write(schema / s"${domain}Schema_datomic.scala", Schema_Datomic(dbModel).get)
    IO.write(schema / s"${domain}Schema_h2.scala", Schema_H2(dbModel).get)
    IO.write(schema / s"${domain}Schema_mariadb.scala", Schema_MariaDB(dbModel).get)
    IO.write(schema / s"${domain}Schema_mysql.scala", Schema_Mysql(dbModel).get)
    IO.write(schema / s"${domain}Schema_postgres.scala", Schema_PostgreSQL(dbModel).get)
    IO.write(schema / s"${domain}Schema_sqlite.scala", Schema_SQlite(dbModel).get)
  }
}
