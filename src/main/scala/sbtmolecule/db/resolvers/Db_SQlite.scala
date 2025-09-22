package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.db.sqlDialect.{Dialect, SQlite}
import scala.collection.mutable.ListBuffer


case class Db_SQlite(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  private val refs2 = ListBuffer.empty[(String, String, Boolean)] // refAttr, ref, isOwner

  protected def createTable(metaEntity: MetaEntity, dialect: Dialect): String = {
    val ent = metaEntity.entity
    def reserved(a: MetaAttribute): Byte =
      if (dialect.reservedKeyWords.contains(a.attribute.toLowerCase)) b1 else b0
    val max = metaEntity.attributes.map {
      case a if a.value == SetValue && a.ref.nonEmpty => 0
      case a if reserved(a) == b1                     => a.attribute.length + 1
      case a                                          => a.attribute.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(ent.toLowerCase)) "_" else ""

    val fields = metaEntity.attributes.flatMap {
      case a if a.attribute == "id" =>
        reservedAttrs = reservedAttrs :+ b0
        Some("id" + padS(max, "id") + " " + dialect.tpe(a))

      case a if a.value == SetValue && a.ref.nonEmpty =>
        reservedAttrs = reservedAttrs :+ reserved(a)
        None

      case a =>
        val column = if (reserved(a) == b1) {
          hasReserved = true
          reservedAttrs = reservedAttrs :+ b1
          a.attribute + "_"
        } else {
          reservedAttrs = reservedAttrs :+ b0
          a.attribute
        }
        // Add foreign key reference (with owner flag)
        a.ref.foreach(refEntity => refs2 += ((a.attribute, refEntity, a.options.contains("owner"))))

        // Add index if requested
        if (a.options.contains("index"))
          indexes += s"CREATE INDEX IF NOT EXISTS _${ent}_$column ON $ent ($column);"

        // Add index if requested
        if (a.ref.nonEmpty)
          indexes += s"CREATE INDEX IF NOT EXISTS _${ent}_$column ON $ent ($column);"

        Some(column + padS(max, column) + " " + dialect.tpe(a))
    }

    val foreignKeys = if (refs2.isEmpty) Nil else {
      val maxRefAttr   = refs2.map(r => clean(dialect, r._1).length).max
      val maxRefEntity = refs2.map(r => clean(dialect, r._2).length).max
      refs2.map { case (refAttr, ref, isOwner) =>
        val refAttr1        = clean(dialect, refAttr)
        val key             = refAttr1 + padS(maxRefAttr, refAttr1)
        val refEnt1         = clean(dialect, ref)
        val ref1            = refEnt1 + padS(maxRefEntity, refEnt1)
        val onDeleteCascade = if (isOwner) " ON DELETE CASCADE" else ""
        s"CONSTRAINT _$key FOREIGN KEY ($key) REFERENCES $ref1 (id)$onDeleteCascade"
      }
    }

    val optForeignKeys = if (foreignKeys.isEmpty) "" else
      foreignKeys.mkString(",\n  ", s",\n  ", "")

    val columns = fields.mkString(s",\n  ") + optForeignKeys

    s"""CREATE TABLE IF NOT EXISTS $ent$tableSuffix (
       |  $columns
       |);
       |""".stripMargin
  }

  override protected def getTables(dialect: Dialect): String = {
    hasReserved = false
    reservedEntities = Array.empty[Byte]
    reservedAttrs = Array.empty[Byte]
    reservedEntityAttrs = Array.empty[String]
    var hasRefs            = false
    val entityMax          = entities.map(_.entity.length).max
    val pEntity            = (ent: String) => " " * (entityMax - ent.length)
    val tables             = entities.map { entity =>
      refs2.clear() // foreign key constraints per table
      val reservedEntity: Byte =
        if (dialect.reservedKeyWords.contains(entity.entity.toLowerCase)) b1 else b0

      reservedEntities = reservedEntities :+ reservedEntity

      val result = createTable(entity, dialect)
      hasRefs = hasRefs || refs2.nonEmpty

      reservedEntityAttrs = reservedEntityAttrs :+ reservedAttrs
        .mkString(s"/* ${entity.entity}${pEntity(entity.entity)} */   ", ", ", "")

      reservedAttrs = Array.empty[Byte]
      result
    }
    val enforceForeignKeys = if (hasRefs)
      """PRAGMA foreign_keys = 1;
        |
        |""".stripMargin
    else ""

    val customIndexes = if (indexes.isEmpty) "" else indexes.mkString("-- Column indexes\n", "\n", "\n")
    tables.mkString(enforceForeignKeys, "\n", "") + (if (customIndexes.isEmpty) "" else s"\n$customIndexes")
  }

  val tables = getTables(SQlite)

  def getSQL: String =
    s"""|$tables
        |""".stripMargin


  def getMeta: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.core.dataModel.*
        |import molecule.db.common.api.*
        |
        |
        |case class ${domain}_sqlite() extends ${domain}_ with MetaDb_sqlite {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("sqlite.sql")}"$getReserved
        |}""".stripMargin
}
