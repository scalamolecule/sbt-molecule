package sbtmolecule.render

import molecule.base.ast.*
import sbtmolecule.render.sql.*
import scala.collection.mutable.ListBuffer


case class Schema_SQlite(schema: MetaSchema) extends Schema_SqlBase(schema) {

  private val refs2 = ListBuffer.empty[(String, String)] // refAttr, refNs

  protected def createTable(metaNs: MetaNs, dialect: Dialect): Seq[String] = {
    val ns = metaNs.ns
    def reserved(a: MetaAttr): Boolean = dialect.reservedKeyWords.contains(a.attr.toLowerCase)
    val max = metaNs.attrs.map {
      case a if a.card == CardSet && a.refNs.nonEmpty => 0
      case a if reserved(a)                           => a.attr.length + 1
      case a                                          => a.attr.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(ns.toLowerCase)) "_" else ""

    val fields = metaNs.attrs.flatMap {
      case a if a.attr == "id" =>
        reservedAttrs = reservedAttrs :+ false
        Some("id" + padS(max, "id") + " " + dialect.tpe(a))

      case a if a.card == CardSet && a.refNs.nonEmpty =>
        reservedAttrs = reservedAttrs :+ reserved(a)
        None

      case a =>
        val column = if (reserved(a)) {
          hasReserved = true
          reservedAttrs = reservedAttrs :+ true
          a.attr + "_"
        } else {
          reservedAttrs = reservedAttrs :+ false
          a.attr
        }
        // Add foreign key reference
        a.refNs.foreach(refNs => refs2 += ((a.attr, refNs)))

        Some(column + padS(max, column) + " " + dialect.tpe(a))
    }

    val foreignKeys = if (refs2.isEmpty) Nil else {
      val maxRefAttr = refs2.map(r => clean(dialect, r._1).length).max
      val maxRefNs   = refs2.map(r => clean(dialect, r._2).length).max
      refs2.map { case (refAttr, refNs) =>
        val refAttr1 = clean(dialect, refAttr)
        val key      = refAttr1 + padS(maxRefAttr, refAttr1)
        val refNs1   = clean(dialect, refNs)
        val ref      = refNs1 + padS(maxRefNs, refNs1)
        s"-- CONSTRAINT _$key FOREIGN KEY ($key) REFERENCES $ref (id)"
      }
    }

    val optForeignKeys = if(foreignKeys.isEmpty) "" else
      foreignKeys.mkString("\n|      |  ", s",\n|      |  ", "")

    val columns = fields.mkString(s",\n|      |  ") + optForeignKeys

    val table =
      s"""CREATE TABLE IF NOT EXISTS $ns$tableSuffix (
         |      |  $columns
         |      |);
         |      |"""

    val joinTables = metaNs.attrs.collect {
      case MetaAttr(refAttr, CardSet, _, Some(refNs), _, _, _, _, _, _) =>
        val (id1, id2) = if (ns == refNs) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)   = (ns.length, refNs.length)
        val (p1, p2)   = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        val key1       = s"${ns}_$id1$p1"
        val key2       = s"${refNs}_$id2$p2"
        val cleanNs    = clean(dialect, ns)
        val cleanRefNs = clean(dialect, refNs)

        s"""CREATE TABLE IF NOT EXISTS ${ns}_${refAttr}_$refNs (
           |      |  $key1 BIGINT,
           |      |  $key2 BIGINT
           |      |  -- CONSTRAINT _$key1 FOREIGN KEY ($key1) REFERENCES $cleanNs (id),
           |      |  -- CONSTRAINT _$key2 FOREIGN KEY ($key2) REFERENCES $cleanRefNs (id)
           |      |);
           |      |"""
    }

    table +: joinTables
  }

  override protected def tables(dialect: Dialect): String = {
    hasReserved = false
    reservedNss = Array.empty[Boolean]
    reservedAttrs = Array.empty[Boolean]
    reservedNssAttrs = Array.empty[String]
    var hasRefs = false
    val tables  = nss.flatMap { ns =>
      refs2.clear() // foreign key constraints per table
      reservedNss = reservedNss :+ dialect.reservedKeyWords.contains(ns.ns.toLowerCase)
      val result = createTable(ns, dialect)
      hasRefs = hasRefs || refs2.nonEmpty
      reservedNssAttrs = reservedNssAttrs :+ reservedAttrs
        .mkString(s"\n      // ${ns.ns}\n      ", ", ", "")
      reservedAttrs = Array.empty[Boolean]
      result
    }
        val enforceForeignKeys = if (hasRefs)
          """-- PRAGMA foreign_keys = 1;
            |      |
            |      |"""
        else ""
        tables.mkString(enforceForeignKeys, "\n|      |", "")
  }

  def get: String =
    s"""|/*
        |* AUTO-GENERATED schema boilerplate code
        |*
        |* To change:
        |* 1. edit data model file in `${schema.pkg}/`
        |* 2. `sbt compile -Dmolecule=true`
        |*/
        |package ${schema.pkg}.schema
        |
        |import molecule.base.api.Schema
        |import molecule.base.ast._
        |
        |
        |trait ${schema.domain}Schema_SQlite extends Schema {
        |
        |  override val sqlSchema_sqlite: String =
        |    \"\"\"
        |      |${tables(SQlite)}\"\"\".stripMargin
        |
        |
        |  // Index to lookup if name collides with db keyword
        |  override val sqlReserved_sqlite: Option[Reserved] = $getReserved
        |}""".stripMargin
}
