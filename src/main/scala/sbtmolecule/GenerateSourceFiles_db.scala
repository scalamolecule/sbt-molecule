package sbtmolecule

import java.io.File
import molecule.base.metaModel.*
import sbt.*
import sbtmolecule.db.dsl.Entity
import sbtmolecule.db.dsl.ops.*
import sbtmolecule.db.resolvers.*


case class GenerateSourceFiles_db(metaDomain: MetaDomain) {

  // For testing only
  def printEntity(metaEntity: MetaEntity): Unit = println(Entity(metaDomain, metaEntity).get)
  def printEntityBuilder(metaEntity: MetaEntity): Unit = println(Entity_(metaDomain, metaEntity, 0, 0).get)
  def printMetaDb: Unit = println(MetaDb_(metaDomain).getMeta)


  def generate(srcManaged: File, resources: File): Unit = {
    var entityIndex  = 0
    var attrIndex    = 0
    val pkg          = metaDomain.pkg
    val domain       = metaDomain.domain
    val segments     = metaDomain.segments
    val pkgSegments  = pkg.split('.').toList
    val domainBase   = pkgSegments.foldLeft(srcManaged)((dir, pkg) => dir / pkg)
    val domainDir    = domainBase / "dsl" / domain
    val resourceBase = pkgSegments.foldLeft(resources)((dir, pkg) => dir / pkg)
    val resourceDir  = resourceBase / domain

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

    val h2         = Db_H2(metaDomain)
    val mariadb    = Db_MariaDB(metaDomain)
    val mysql      = Db_MySQL(metaDomain)
    val postgresql = Db_PostgreSQL(metaDomain)
    val sqlite     = Db_SQlite(metaDomain)

    val metadb = domainDir / "metadb"
    IO.write(metadb / s"${domain}_.scala", MetaDb_(metaDomain).getMeta)
    IO.write(metadb / s"${domain}_h2.scala", h2.get)
    IO.write(metadb / s"${domain}_mariadb.scala", mariadb.get)
    IO.write(metadb / s"${domain}_mysql.scala", mysql.get)
    IO.write(metadb / s"${domain}_postgresql.scala", postgresql.get)
    IO.write(metadb / s"${domain}_sqlite.scala", sqlite.getMeta)

    IO.write(resourceDir / s"${domain}_h2.sql", h2.getSQL)
    IO.write(resourceDir / s"${domain}_mariadb.sql", mariadb.getSQL)
    IO.write(resourceDir / s"${domain}_mysql.sql", mysql.getSQL)
    IO.write(resourceDir / s"${domain}_postgresql.sql", postgresql.getSQL)
    IO.write(resourceDir / s"${domain}_sqlite.sql", sqlite.getSQL)
  }
}
