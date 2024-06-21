package sbtmolecule.render

import molecule.base.ast.*
import molecule.base.util.{BaseHelpers, RegexMatching}
import sbtmolecule.render.sql.*
import scala.collection.mutable.ListBuffer


abstract class Schema_SqlBase(schema: MetaSchema) extends RegexMatching with BaseHelpers {

  protected val nss: Seq[MetaNs] = schema.parts.flatMap(_.nss)

  protected var hasReserved      = false
  protected var reservedNss      = Array.empty[Boolean]
  protected var reservedAttrs    = Array.empty[Boolean]
  protected var reservedNssAttrs = Array.empty[String]
  protected val refs             = ListBuffer.empty[(String, String, String)] // ns, refAttr, refNs

  private def createTable(metaNs: MetaNs, dialect: Dialect): Seq[String] = {
    val ns = metaNs.ns
    def reserved(a: MetaAttr): Boolean = dialect.reservedKeyWords.contains(a.attr.toLowerCase)
    val max = metaNs.attrs.map {
      case a if a.card == CardSet && a.refNs.nonEmpty => 0
      case a if reserved(a)                           => a.attr.length + 1
      case a                                          => a.attr.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(ns.toLowerCase)) "_" else ""

    val columns = metaNs.attrs.flatMap {
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
        // Add foreign key references
        a.refNs.foreach(refNs => refs += ((ns, a.attr, refNs)))

        Some(column + padS(max, column) + " " + dialect.tpe(a))
    }.mkString(s",\n|      |  ")

    val table =
      s"""CREATE TABLE IF NOT EXISTS $ns$tableSuffix (
         |      |  $columns
         |      |);
         |      |"""

    val joinTables = metaNs.attrs.collect {
      case MetaAttr(refAttr, CardSet, _, Some(refNs), _, _, _, _, _, _) =>
        val joinTable  = s"${ns}_${refAttr}_$refNs"
        val (id1, id2) = if (ns == refNs) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)   = (ns.length, refNs.length)
        val (p1, p2)   = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        val ref1       = s"${ns}_$id1$p1"
        val ref2       = s"${refNs}_$id2$p2"
        refs ++= List(
          (joinTable, s"${ns}_$id1", ns),
          (joinTable, s"${refNs}_$id2", refNs),
        )
        s"""CREATE TABLE IF NOT EXISTS $joinTable (
           |      |  $ref1 BIGINT,
           |      |  $ref2 BIGINT
           |      |);
           |      |"""
    }

    table +: joinTables
  }

  protected def clean(dialect: Dialect, s: String) = {
    s + (if (dialect.reservedKeyWords.contains(s.toLowerCase)) "_" else "")
  }

  protected def tables(dialect: Dialect): String = {
    hasReserved = false
    reservedNss = Array.empty[Boolean]
    reservedAttrs = Array.empty[Boolean]
    reservedNssAttrs = Array.empty[String]

    val tableDefinitions = nss.flatMap { ns =>
      reservedNss = reservedNss :+ dialect.reservedKeyWords.contains(ns.ns.toLowerCase)
      val result = createTable(ns, dialect)
      reservedNssAttrs = reservedNssAttrs :+ reservedAttrs.mkString(", ")
      reservedAttrs = Array.empty[Boolean]
      result
    }

    val quote       = dialect match {
      case _: Postgres.type => ""
      case _                => "`"
    }
    val foreignKeys = if (refs.isEmpty) Nil else {
      var m1 = 0
      var m2 = 0
      var m3 = 0
      var m4 = 0
      refs.foreach {
        case (ns, refAttr, refNs) =>
          m1 = clean(dialect, ns).length.max(m1)
          m2 = clean(dialect, refAttr).length.max(m2)
          m3 = clean(dialect, refNs).length.max(m3)
          m4 = ((clean(dialect, refAttr) + quote).length + 2).max(m4)
      }

      val constraints = ListBuffer.empty[String]
      List(refs.map {
        case (ns, refAttr, refNs) =>
          constraints += refAttr
          val ns1        = clean(dialect, ns)
          val table      = ns1 + padS(m1, ns1)
          val refAttr1   = clean(dialect, refAttr)
          val key        = refAttr1 + padS(m2, refAttr1)
          val nsKey      = ns + "_" + refAttr
          val refNs1     = clean(dialect, refNs)
          val ref        = refNs1 + padS(m3, refNs1)
          val count      = constraints.count(_ == refAttr)
          val refAttr2   = refAttr1 + (if (count == 1) quote else "_" + count + quote)
          val constraint = s"${quote}_$refAttr2" + padS(m4, refAttr2)
          s"ALTER TABLE $table ADD CONSTRAINT $constraint FOREIGN KEY ($key) REFERENCES $ref (id);"
      }.mkString("", "\n|      |", "\n|      |"))
    }

    (tableDefinitions ++ foreignKeys).mkString("\n|      |")
  }

  protected def getReserved = if (hasReserved) {
    s"""Some(Reserved(
       |    Array(${reservedNss.mkString(", ")}),
       |    Array(
       |      ${reservedNssAttrs.mkString(",\n      ")}
       |    )
       |  ))""".stripMargin
  } else "None"
}
