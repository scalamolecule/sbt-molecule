package sbtmolecule.db.schema

import molecule.base.metaModel.*
import sbtmolecule.db.schema.sqlDialect.{Dialect, SQlite}
import scala.collection.mutable.ListBuffer


case class Schema_SQlite(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {

  private val refs2 = ListBuffer.empty[(String, String)] // refAttr, ref

  protected def createTable(metaEntity: MetaEntity, dialect: Dialect): Seq[String] = {
    val ent = metaEntity.entity
    def reserved(a: MetaAttribute): Byte =
      if (dialect.reservedKeyWords.contains(a.attribute.toLowerCase)) b1 else b0
    val max = metaEntity.attributes.map {
      case a if a.cardinality == CardSet && a.ref.nonEmpty => 0
      case a if reserved(a) == b1                          => a.attribute.length + 1
      case a                                               => a.attribute.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(ent.toLowerCase)) "_" else ""

    val fields = metaEntity.attributes.flatMap {
      case a if a.attribute == "id" =>
        reservedAttrs = reservedAttrs :+ b0
        Some("id" + padS(max, "id") + " " + dialect.tpe(a))

      case a if a.cardinality == CardSet && a.ref.nonEmpty =>
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
        // Add foreign key reference
        a.ref.foreach(refEntity => refs2 += ((a.attribute, refEntity)))

        Some(column + padS(max, column) + " " + dialect.tpe(a))
    }

    val foreignKeys = if (refs2.isEmpty) Nil else {
      val maxRefAttr   = refs2.map(r => clean(dialect, r._1).length).max
      val maxRefEntity = refs2.map(r => clean(dialect, r._2).length).max
      refs2.map { case (refAttr, ref) =>
        val refAttr1 = clean(dialect, refAttr)
        val key      = refAttr1 + padS(maxRefAttr, refAttr1)
        val refEnt1  = clean(dialect, ref)
        val ref1     = refEnt1 + padS(maxRefEntity, refEnt1)
        s"-- CONSTRAINT _$key FOREIGN KEY ($key) REFERENCES $ref1 (id)"
      }
    }

    val optForeignKeys = if (foreignKeys.isEmpty) "" else
      foreignKeys.mkString("\n|      |  ", s",\n|      |  ", "")

    val columns = fields.mkString(s",\n|      |  ") + optForeignKeys

    val table =
      s"""CREATE TABLE IF NOT EXISTS $ent$tableSuffix (
         |      |  $columns
         |      |);
         |      |"""

    val joinTables = metaEntity.attributes.collect {
      case MetaAttribute(refAttr, CardSet, _, _, Some(ref), _, _, _, _, _, _, _) =>
        val (id1, id2)     = if (ent == ref) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)       = (ent.length, ref.length)
        val (p1, p2)       = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        val key1           = s"${ent}_$id1$p1"
        val key2           = s"${ref}_$id2$p2"
        val cleanEntity    = clean(dialect, ent)
        val cleanRefEntity = clean(dialect, ref)

        s"""CREATE TABLE IF NOT EXISTS ${ent}_${refAttr}_$ref (
           |      |  $key1 BIGINT,
           |      |  $key2 BIGINT
           |      |  -- CONSTRAINT _$key1 FOREIGN KEY ($key1) REFERENCES $cleanEntity (id),
           |      |  -- CONSTRAINT _$key2 FOREIGN KEY ($key2) REFERENCES $cleanRefEntity (id)
           |      |);
           |      |"""
    }

    table +: joinTables
  }

  override protected def tables(dialect: Dialect): String = {
    hasReserved = false
    reservedEntities = Array.empty[Byte]
    reservedAttrs = Array.empty[Byte]
    reservedEntityAttrs = Array.empty[String]
    var hasRefs            = false
    val tables             = entities.flatMap { entity =>
      refs2.clear() // foreign key constraints per table
      val reservedEntity: Byte =
        if (dialect.reservedKeyWords.contains(entity.entity.toLowerCase)) b1 else b0

      reservedEntities = reservedEntities :+ reservedEntity

      val result = createTable(entity, dialect)
      hasRefs = hasRefs || refs2.nonEmpty

      reservedEntityAttrs = reservedEntityAttrs :+ reservedAttrs
        .mkString(s"\n      // ${entity.entity}\n      ", ", ", "")

      reservedAttrs = Array.empty[Byte]
      result
    }
    val enforceForeignKeys = if (hasRefs)
      """-- PRAGMA foreign_keys = 1;
        |      |
        |      |"""
    else ""
    tables.mkString(enforceForeignKeys, "\n|      |", "")
  }

  val domain = metaDomain.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.base.metaModel.*
        |import molecule.db.core.api.*
        |
        |
        |object ${domain}Schema_sqlite extends ${domain}Schema with Schema_sqlite {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(SQlite)}\"\"\".stripMargin
        |  )$getReserved
        |}""".stripMargin
}
