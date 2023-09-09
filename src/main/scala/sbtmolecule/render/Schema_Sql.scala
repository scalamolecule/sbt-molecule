package sbtmolecule.render

import molecule.base.ast.SchemaAST.*
import molecule.base.util.RegexMatching


case class Schema_Sql(schema: MetaSchema) extends RegexMatching {

  private val nss: Seq[MetaNs] = schema.parts.flatMap(_.nss)

  private def field(max: Int, a: MetaAttr): String = {
    val indent = padS(max, a.attr) + " "
    val array  = if (a.card == CardSet) " ARRAY" else ""
    val t      = a.baseTpe match {
      case "UUID" => "uuid"
      case "URI"  => "uri"
      case other  => "" + other.head.toLower + other.tail
    }
    val tpe    = "$" + (if (a.refNs.isEmpty) t else "ref")
    "       |  " + a.attr + indent + tpe + array
  }

  private def table(metaNs: MetaNs): Seq[String] = {
    val ns     = metaNs.ns
    val attrs  = metaNs.attrs.filterNot(a => a.card == CardSet && a.refNs.nonEmpty)
    val max    = attrs.map(a => a.attr.length).max.max(2)
    val fields = attrs.map(a => field(max, a)).tail.mkString(s",\n|") // without id
    val id     = "id" + padS(max, "id") + " $id,"
    val table  =
      s"""       |CREATE TABLE $ns (
         |       |  $id
         |$fields
         |       |);
         |       |"""

    val joinTables = metaNs.attrs.collect {
      case MetaAttr(refAttr, CardSet, _, Some(refNs), _, _, _, _, _, _) =>
        val (id1, id2) = if (ns == refNs) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)   = (ns.length, refNs.length)
        val (p1, p2)   = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        s"""       |CREATE TABLE ${ns}_${refAttr}_$refNs (
           |       |  ${ns}_$id1$p1 BIGINT,
           |       |  ${refNs}_$id2$p2 BIGINT
           |       |);
           |       |"""
    }

    table +: joinTables
  }

  private val tables: String = nss.flatMap(table).mkString(s"\n|")

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
        |import molecule.base.ast.SchemaAST._
        |
        |
        |trait ${schema.domain}Schema_Sql extends Schema {
        |
        |  private val dbs = List(
        |    "h2",
        |    "mysql",
        |    "mssql",
        |    "oracle",
        |    "derby",
        |    "db2",
        |    // etc..
        |  )
        |
        |  private val types = List(
        |    //                    h2                    mysql   + more dialects...
        |    "String"     -> List("LONGVARCHAR"        , "LONGVARCHAR"),
        |    "Int"        -> List("INT"                , "INT"),
        |    "Long"       -> List("BIGINT"             , "BIGINT"),
        |    "Float"      -> List("REAL"               , "REAL"),
        |    "Double"     -> List("DOUBLE PRECISION"   , "DOUBLE"),
        |    "Boolean"    -> List("BOOLEAN"            , "BOOLEAN"),
        |    "BigInt"     -> List("DECIMAL(100, 0)"    , "DECIMAL"),
        |    "BigDecimal" -> List("DECIMAL(65535, 25)" , "DECIMAL"),
        |    "Date"       -> List("DATE"               , "DATE"),
        |    "UUID"       -> List("UUID"               , "UUID"),
        |    "URI"        -> List("VARCHAR"            , "VARCHAR"),
        |    "Byte"       -> List("TINYINT"            , "TINYINT"),
        |    "Short"      -> List("SMALLINT"           , "SMALLINT"),
        |    "Char"       -> List("CHAR"               , "CHAR"),
        |  )
        |
        |  private val customVendorFunctions = Map(
        |    "h2" ->
        |      \"\"\"
        |        |CREATE ALIAS IF NOT EXISTS has_String     FOR "molecule.sql.jdbc.vendor.h2.functions.has_String";
        |        |CREATE ALIAS IF NOT EXISTS has_Int        FOR "molecule.sql.jdbc.vendor.h2.functions.has_Int";
        |        |CREATE ALIAS IF NOT EXISTS has_Long       FOR "molecule.sql.jdbc.vendor.h2.functions.has_Long";
        |        |CREATE ALIAS IF NOT EXISTS has_Float      FOR "molecule.sql.jdbc.vendor.h2.functions.has_Float";
        |        |CREATE ALIAS IF NOT EXISTS has_Double     FOR "molecule.sql.jdbc.vendor.h2.functions.has_Double";
        |        |CREATE ALIAS IF NOT EXISTS has_Boolean    FOR "molecule.sql.jdbc.vendor.h2.functions.has_Boolean";
        |        |CREATE ALIAS IF NOT EXISTS has_BigInt     FOR "molecule.sql.jdbc.vendor.h2.functions.has_BigInt";
        |        |CREATE ALIAS IF NOT EXISTS has_BigDecimal FOR "molecule.sql.jdbc.vendor.h2.functions.has_BigDecimal";
        |        |CREATE ALIAS IF NOT EXISTS has_Date       FOR "molecule.sql.jdbc.vendor.h2.functions.has_Date";
        |        |CREATE ALIAS IF NOT EXISTS has_UUID       FOR "molecule.sql.jdbc.vendor.h2.functions.has_UUID";
        |        |CREATE ALIAS IF NOT EXISTS has_URI        FOR "molecule.sql.jdbc.vendor.h2.functions.has_URI";
        |        |CREATE ALIAS IF NOT EXISTS has_Byte       FOR "molecule.sql.jdbc.vendor.h2.functions.has_Byte";
        |        |CREATE ALIAS IF NOT EXISTS has_Short      FOR "molecule.sql.jdbc.vendor.h2.functions.has_Short";
        |        |CREATE ALIAS IF NOT EXISTS has_Char       FOR "molecule.sql.jdbc.vendor.h2.functions.has_Char";
        |        |
        |        |CREATE ALIAS IF NOT EXISTS hasNo_String     FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_String";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Int        FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Int";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Long       FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Long";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Float      FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Float";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Double     FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Double";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Boolean    FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Boolean";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_BigInt     FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_BigInt";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_BigDecimal FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_BigDecimal";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Date       FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Date";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_UUID       FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_UUID";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_URI        FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_URI";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Byte       FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Byte";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Short      FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Short";
        |        |CREATE ALIAS IF NOT EXISTS hasNo_Char       FOR "molecule.sql.jdbc.vendor.h2.functions.hasNo_Char";\"\"\"
        |  )
        |
        |  override def sqlSchema(db: String): String = {
        |    val dbIndex = dbs.indexOf(db, 0) match {
        |      case -1 => throw new Exception(
        |        s"Database `$$db` not found among databases with implemented jdbc drivers:\\n  " + dbs.mkString("\\n  ")
        |      )
        |      case i  => i
        |    }
        |    
        |    val tpe = types.map { case (scalaType, sqlTpes) => scalaType -> sqlTpes(dbIndex) }.toMap
        |    val id  = "BIGINT AUTO_INCREMENT PRIMARY KEY"
        |
        |    lazy val string     = tpe("String")
        |    lazy val int        = tpe("Int")
        |    lazy val long       = tpe("Long")
        |    lazy val float      = tpe("Float")
        |    lazy val double     = tpe("Double")
        |    lazy val boolean    = tpe("Boolean")
        |    lazy val bigInt     = tpe("BigInt")
        |    lazy val bigDecimal = tpe("BigDecimal")
        |    lazy val date       = tpe("Date")
        |    lazy val uuid       = tpe("UUID")
        |    lazy val uri        = tpe("URI")
        |    lazy val byte       = tpe("Byte")
        |    lazy val short      = tpe("Short")
        |    lazy val char       = tpe("Char")
        |
        |    lazy val ref = long
        |    val customFunctions = customVendorFunctions.getOrElse(db, "")
        |
        |    s\"\"\"
        |$tables
        |       |$$customFunctions
        |       |\"\"\".stripMargin
        |  }
        |}""".stripMargin
}
