package sbtmolecule

import java.io.File
import molecule.base.metaModel.*
import sbt.*
import sbtmolecule.db.dsl.DbEntity
import sbtmolecule.db.dsl.ops.*
import sbtmolecule.db.schema.*


case class GenerateSourceFiles_db(metaDomain: MetaDomain) {

  def getEntityCode(metaEntity: MetaEntity): String = DbEntity(metaDomain, metaEntity).get
  def getEntityBuilderCode(metaEntity: MetaEntity): String = Entity_Builders(metaDomain, metaEntity, 0, 0).get

  def printEntity(metaEntity: MetaEntity): Unit = println(getEntityCode(metaEntity))
  def printEntityBuilder(metaEntity: MetaEntity): Unit = println(getEntityBuilderCode(metaEntity))

  def generate(srcManaged: File): Unit = {
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
              s"""// AUTO-GENERATED Molecule DSL boilerplate code for enum `$enumTpe`
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
      val ent             = metaEntity.entity
      val ent_            = ent + "_"
      val entity          = DbEntity(metaDomain, metaEntity).get
      val entity_Attrs    = Entity_Attrs(metaDomain, metaEntity, entityIndex, attrIndex).get
      val entity_Builders = Entity_Builders(metaDomain, metaEntity, entityIndex, attrIndex).get
      val entity_Exprs    = Entity_Exprs(metaDomain, metaEntity, entityIndex, attrIndex).get
      val entity_Sort     = Entity_Sort(metaDomain, metaEntity, entityIndex, attrIndex).get

      // Increment of entityIndex has to come after calling DbTable.get
      entityIndex += 1
      attrIndex += metaEntity.attributes.length
      IO.write(domainDir / s"$ent.scala", entity)

//      IO.write(domainDir / ent_ / s"${ent}_Attrs.scala", entity_Attrs)
//      IO.write(domainDir / ent_ / s"${ent}_Builders.scala", entity_Builders)
//      IO.write(domainDir / ent_ / s"${ent}_Exprs.scala", entity_Exprs)
//      IO.write(domainDir / ent_ / s"${ent}_Sort.scala", entity_Sort)

      IO.write(domainDir / "ops" / s"${ent}_Attrs.scala", entity_Attrs)
      IO.write(domainDir / "ops" / s"${ent}_Builders.scala", entity_Builders)
      IO.write(domainDir / "ops" / s"${ent}_Exprs.scala", entity_Exprs)
      IO.write(domainDir / "ops" / s"${ent}_Sort.scala", entity_Sort)
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
