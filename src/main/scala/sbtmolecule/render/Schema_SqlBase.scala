package sbtmolecule.render

import molecule.base.ast.*
import molecule.base.util.{BaseHelpers, RegexMatching}
import sbtmolecule.render.sql.*


abstract class Schema_SqlBase(schema: MetaSchema) extends RegexMatching with BaseHelpers {

  protected val nss: Seq[MetaNs] = schema.parts.flatMap(_.nss)

  private var hasReserved      = false
  private var reservedNss      = Array.empty[Boolean]
  private var reservedAttrs    = Array.empty[Boolean]
  private var reservedNssAttrs = Array.empty[String]

  protected def createTable(metaNs: MetaNs, dialect: Dialect): Seq[String] = {
    val mainTable = metaNs.ns
    def reserved(a: MetaAttr): Boolean = dialect.reservedKeyWords.contains(a.attr.toLowerCase)
    val max = metaNs.attrs.map {
      case a if a.card == CardSet && a.refNs.nonEmpty => 0
      case a if reserved(a)                           => a.attr.length + 1
      case a                                          => a.attr.length
    }.max.max(2)

    val fields      = metaNs.attrs.flatMap {
        case a if a.attr == "id" =>
          reservedAttrs = reservedAttrs :+ false
          Some("id" + padS(max, "id") + " " + dialect.tpe(a))

        case a if a.card == CardSet && a.refNs.nonEmpty =>
          reservedAttrs = reservedAttrs :+ reserved(a)
          None

        case a =>
          val suffix = if (reserved(a)) {
            hasReserved = true
            reservedAttrs = reservedAttrs :+ true
            "_"
          } else {
            reservedAttrs = reservedAttrs :+ false
            ""
          }
          Some(a.attr + suffix + padS(max, a.attr + suffix) + " " + dialect.tpe(a))
      }
      .mkString(s",\n|      |  ")
    val tableSuffix = if (dialect.reservedKeyWords.contains(mainTable.toLowerCase)) "_" else ""

    val tables =
      s"""CREATE TABLE IF NOT EXISTS $mainTable$tableSuffix (
         |      |  $fields
         |      |);
         |      |"""

    val joinTables = metaNs.attrs.collect {
      case MetaAttr(refAttr, CardSet, _, Some(refNs), _, _, _, _, _, _) =>
        val (id1, id2) = if (mainTable == refNs) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)   = (mainTable.length, refNs.length)
        val (p1, p2)   = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        s"""CREATE TABLE IF NOT EXISTS ${mainTable}_${refAttr}_$refNs (
           |      |  ${mainTable}_$id1$p1 BIGINT,
           |      |  ${refNs}_$id2$p2 BIGINT
           |      |);
           |      |"""
    }

    tables +: joinTables
  }

  protected def tables(dialect: Dialect): String = {
    hasReserved = false
    reservedNss = Array.empty[Boolean]
    reservedAttrs = Array.empty[Boolean]
    reservedNssAttrs = Array.empty[String]
    nss.flatMap { ns =>
      reservedNss = reservedNss :+ dialect.reservedKeyWords.contains(ns.ns.toLowerCase)
      val result = createTable(ns, dialect)
      reservedNssAttrs = reservedNssAttrs :+ reservedAttrs.mkString(", ")
      reservedAttrs = Array.empty[Boolean]
      result
    }.mkString("\n|      |")
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
