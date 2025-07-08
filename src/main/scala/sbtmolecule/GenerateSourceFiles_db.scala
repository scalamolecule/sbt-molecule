package sbtmolecule

import java.io.File
import molecule.base.metaModel.*
import sbt.*
import sbtmolecule.db.dsl.Entity
import sbtmolecule.db.dsl.ops.*
import sbtmolecule.db.resolvers.*


case class GenerateSourceFiles_db(metaDomain: MetaDomain) {

  def getEntityCode(metaEntity: MetaEntity): String = Entity(metaDomain, metaEntity).get
  def getEntityBuilderCode(metaEntity: MetaEntity): String = Entity_(metaDomain, metaEntity, 0, 0).get
  def getMetaDb: String = MetaDb_(metaDomain).getMeta

  def printEntity(metaEntity: MetaEntity): Unit = println(getEntityCode(metaEntity))
  def printEntityBuilder(metaEntity: MetaEntity): Unit = println(getEntityBuilderCode(metaEntity))
  def printMetaDb: Unit = {
//    println(domainDir)
    println(getMetaDb)
  }

  def generate(srcManaged: File, resourcesDir: File): Unit = {
    var entityIndex = 0
    var attrIndex   = 0
    val pkg         = metaDomain.pkg
    val domain      = metaDomain.domain
    val segments    = metaDomain.segments
    val base        = pkg.split('.').toList.foldLeft(srcManaged)((dir, pkg) => dir / pkg)
    val domainDir   = base / "dsl" / domain

    val segmentsWithoutEnums = segments.filter {
      case MetaSegment("_enums", entities) =>
        entities.foreach {
          case MetaEntity(enumTpe, attributes, _, _, _, _) =>
            val enumCode =
              s"""// AUTO-GENERATED Molecule boilerplate code
                 |package $pkg.dsl.$domain
                 |
                 |enum $enumTpe:
                 |  case ${attributes.map(_.attribute).mkString(", ")}
                 |""".stripMargin

            IO.write(domainDir / s"$enumTpe.scala", enumCode)
        }
        false

      case _ => true
    }

    for {
      metaSegment <- segmentsWithoutEnums
      metaEntity <- metaSegment.entities
    } yield {
      val ent     = metaEntity.entity
      val entity  = Entity(metaDomain, metaEntity).get
      val entity_ = Entity_(metaDomain, metaEntity, entityIndex, attrIndex).get

      // Increment of entityIndex has to come after calling DbTable.get
      entityIndex += 1
      attrIndex += metaEntity.attributes.length
      IO.write(domainDir / s"$ent.scala", entity)
      IO.write(domainDir / "ops" / s"${ent}_.scala", entity_)
    }

    val h2       = Db_H2(metaDomain)
    val mariadb  = Db_MariaDB(metaDomain)
    val mysql    = Db_Mysql(metaDomain)
    val postgres = Db_PostgreSQL(metaDomain)
    val sqlite   = Db_SQlite(metaDomain)

    val metadb = domainDir / "metadb"
    IO.write(metadb / s"${domain}_MetaDb.scala", MetaDb_(metaDomain).getMeta)
    IO.write(metadb / s"${domain}_MetaDb_h2.scala", h2.get)
    IO.write(metadb / s"${domain}_MetaDb_mariadb.scala", mariadb.get)
    IO.write(metadb / s"${domain}_MetaDb_mysql.scala", mysql.get)
    IO.write(metadb / s"${domain}_MetaDb_postgres.scala", postgres.get)
    IO.write(metadb / s"${domain}_MetaDb_sqlite.scala", sqlite.getMeta)

//    val schema = domainDir / "schema"
    val resources = resourcesDir / domain
    IO.write(resources / s"${domain}_Schema_h2.sql", h2.getSQL)
    IO.write(resources / s"${domain}_Schema_mariadb.sql", mariadb.getSQL)
    IO.write(resources / s"${domain}_Schema_mysql.sql", mysql.getSQL)
    IO.write(resources / s"${domain}_Schema_postgres.sql", postgres.getSQL)
    IO.write(resources / s"${domain}_Schema_sqlite.sql", sqlite.getSQL)
  }
}
