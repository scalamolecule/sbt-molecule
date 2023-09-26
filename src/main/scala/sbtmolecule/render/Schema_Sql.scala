package sbtmolecule.render

import molecule.base.ast.*
import molecule.base.util.{BaseHelpers, RegexMatching}
import sbtmolecule.render.sql.*


case class Schema_Sql(schema: MetaSchema) extends RegexMatching with BaseHelpers {

  private val nss: Seq[MetaNs] = schema.parts.flatMap(_.nss)

  private var hasReserved   = false
  private var reservedNss   = Array.empty[Boolean]
  private var reservedAttrs = Array.empty[Boolean]

  private def createTable(metaNs: MetaNs, dialect: Dialect): Seq[String] = {
    val mainTable   = metaNs.ns
    val attrs       = metaNs.attrs.filterNot(a => a.card == CardSet && a.refNs.nonEmpty)
    val max         = attrs.map(a => a.attr.length).max.max(2)
    val fields      = attrs.map {
        case a if a.attr == "id" =>
          reservedAttrs = reservedAttrs :+ false
          "id" + padS(max, "id") + " " + dialect.tpe(a)

        case a =>
          val suffix = if (dialect.reservedKeyWords.contains(a.attr.toLowerCase)) {
            hasReserved = true
            reservedAttrs = reservedAttrs :+ true
            "_"
          } else {
            reservedAttrs = reservedAttrs :+ false
            ""
          }
          a.attr + suffix + padS(max, a.attr + suffix) + " " + dialect.tpe(a)
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

  private def tables(dialect: Dialect): String = {
    hasReserved = false
    reservedNss = Array.empty[Boolean]
    reservedAttrs = Array.empty[Boolean]
    nss.flatMap { ns =>
      reservedNss = reservedNss :+ dialect.reservedKeyWords.contains(ns.ns.toLowerCase)
      createTable(ns, dialect)
    }.mkString("\n|      |")
  }

  private def getReserved = if (hasReserved) {
    s"""Some(Reserved(
       |    Array(${reservedNss.mkString(", ")}),
       |    Array(${reservedAttrs.mkString(", ")})
       |  ))""".stripMargin
  } else "None"

  private val sqlSchema_h2   = tables(H2)
  private val sqlReserved_h2 = getReserved

  private val sqlSchema_mysql   = tables(Mysql)
  private val sqlReserved_mysql = getReserved

  private val sqlSchema_postgres   = tables(Postgres)
  private val sqlReserved_postgres = getReserved

  def get: String =
    s"""|/*
        |* AUTO-GENERATED schema boilerplate code
        |*
        |* To change:
        |* 1. edit data model file in `${schema.pkg}.dataModel/`
        |* 2. `sbt compile -Dmolecule=true`
        |*/
        |package ${schema.pkg}.schema
        |
        |import molecule.base.api.Schema
        |import molecule.base.ast._
        |
        |
        |trait ${schema.domain}Schema_Sql extends Schema {
        |
        |  override val sqlSchema_h2: String =
        |    \"\"\"
        |      |$sqlSchema_h2
        |      |CREATE ALIAS IF NOT EXISTS has_String     FOR "molecule.sql.h2.functions.has_String";
        |      |CREATE ALIAS IF NOT EXISTS has_Int        FOR "molecule.sql.h2.functions.has_Int";
        |      |CREATE ALIAS IF NOT EXISTS has_Long       FOR "molecule.sql.h2.functions.has_Long";
        |      |CREATE ALIAS IF NOT EXISTS has_Float      FOR "molecule.sql.h2.functions.has_Float";
        |      |CREATE ALIAS IF NOT EXISTS has_Double     FOR "molecule.sql.h2.functions.has_Double";
        |      |CREATE ALIAS IF NOT EXISTS has_Boolean    FOR "molecule.sql.h2.functions.has_Boolean";
        |      |CREATE ALIAS IF NOT EXISTS has_BigInt     FOR "molecule.sql.h2.functions.has_BigInt";
        |      |CREATE ALIAS IF NOT EXISTS has_BigDecimal FOR "molecule.sql.h2.functions.has_BigDecimal";
        |      |CREATE ALIAS IF NOT EXISTS has_Date       FOR "molecule.sql.h2.functions.has_Date";
        |      |CREATE ALIAS IF NOT EXISTS has_UUID       FOR "molecule.sql.h2.functions.has_UUID";
        |      |CREATE ALIAS IF NOT EXISTS has_URI        FOR "molecule.sql.h2.functions.has_URI";
        |      |CREATE ALIAS IF NOT EXISTS has_Byte       FOR "molecule.sql.h2.functions.has_Byte";
        |      |CREATE ALIAS IF NOT EXISTS has_Short      FOR "molecule.sql.h2.functions.has_Short";
        |      |CREATE ALIAS IF NOT EXISTS has_Char       FOR "molecule.sql.h2.functions.has_Char";
        |      |
        |      |CREATE ALIAS IF NOT EXISTS hasNo_String     FOR "molecule.sql.h2.functions.hasNo_String";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Int        FOR "molecule.sql.h2.functions.hasNo_Int";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Long       FOR "molecule.sql.h2.functions.hasNo_Long";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Float      FOR "molecule.sql.h2.functions.hasNo_Float";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Double     FOR "molecule.sql.h2.functions.hasNo_Double";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Boolean    FOR "molecule.sql.h2.functions.hasNo_Boolean";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_BigInt     FOR "molecule.sql.h2.functions.hasNo_BigInt";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_BigDecimal FOR "molecule.sql.h2.functions.hasNo_BigDecimal";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Date       FOR "molecule.sql.h2.functions.hasNo_Date";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_UUID       FOR "molecule.sql.h2.functions.hasNo_UUID";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_URI        FOR "molecule.sql.h2.functions.hasNo_URI";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Byte       FOR "molecule.sql.h2.functions.hasNo_Byte";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Short      FOR "molecule.sql.h2.functions.hasNo_Short";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Char       FOR "molecule.sql.h2.functions.hasNo_Char";
        |      |\"\"\".stripMargin
        |
        |
        |  override val sqlSchema_mysql: String =
        |    \"\"\"
        |      |$sqlSchema_mysql\"\"\".stripMargin
        |
        |
        |  override val sqlSchema_postgres: String =
        |    \"\"\"
        |      |$sqlSchema_postgres
        |      |CREATE OR REPLACE FUNCTION _final_median(numeric[])
        |      |   RETURNS numeric AS
        |      |$$$$
        |      |   SELECT AVG(val)
        |      |   FROM (
        |      |     SELECT DISTINCT val
        |      |     FROM unnest($$1) val
        |      |     ORDER BY 1
        |      |     LIMIT  2 - MOD(array_upper($$1, 1), 2)
        |      |     OFFSET CEIL(array_upper($$1, 1) / 2.0) - 1
        |      |   ) sub;
        |      |$$$$
        |      |LANGUAGE 'sql' IMMUTABLE;
        |      |
        |      |CREATE AGGREGATE median(numeric) (
        |      |  SFUNC=array_append,
        |      |  STYPE=numeric[],
        |      |  FINALFUNC=_final_median,
        |      |  INITCOND='{}'
        |      |);
        |      |\"\"\".stripMargin
        |
        |
        |  override val sqlReserved_h2: Option[Reserved] = $sqlReserved_h2
        |
        |  override val sqlReserved_mysql: Option[Reserved] = $sqlReserved_mysql
        |
        |  override val sqlReserved_postgres: Option[Reserved] = $sqlReserved_postgres
        |}""".stripMargin
}
