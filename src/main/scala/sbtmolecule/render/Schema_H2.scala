package sbtmolecule.render

import molecule.base.ast.*
import sbtmolecule.render.sql.*


case class Schema_H2(schema: MetaSchema) extends Schema_SqlBase(schema) {

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
        |trait ${schema.domain}Schema_H2 extends Schema {
        |
        |  override val sqlSchema_h2: String =
        |    \"\"\"
        |      |${tables(H2)}
        |      |CREATE ALIAS IF NOT EXISTS has_String         FOR "molecule.sql.h2.functions.has_String";
        |      |CREATE ALIAS IF NOT EXISTS has_Int            FOR "molecule.sql.h2.functions.has_Int";
        |      |CREATE ALIAS IF NOT EXISTS has_Long           FOR "molecule.sql.h2.functions.has_Long";
        |      |CREATE ALIAS IF NOT EXISTS has_Float          FOR "molecule.sql.h2.functions.has_Float";
        |      |CREATE ALIAS IF NOT EXISTS has_Double         FOR "molecule.sql.h2.functions.has_Double";
        |      |CREATE ALIAS IF NOT EXISTS has_Boolean        FOR "molecule.sql.h2.functions.has_Boolean";
        |      |CREATE ALIAS IF NOT EXISTS has_BigInt         FOR "molecule.sql.h2.functions.has_BigInt";
        |      |CREATE ALIAS IF NOT EXISTS has_BigDecimal     FOR "molecule.sql.h2.functions.has_BigDecimal";
        |      |CREATE ALIAS IF NOT EXISTS has_Date           FOR "molecule.sql.h2.functions.has_Date";
        |      |CREATE ALIAS IF NOT EXISTS has_Duration       FOR "molecule.sql.h2.functions.has_Duration";
        |      |CREATE ALIAS IF NOT EXISTS has_Instant        FOR "molecule.sql.h2.functions.has_Instant";
        |      |CREATE ALIAS IF NOT EXISTS has_LocalDate      FOR "molecule.sql.h2.functions.has_LocalDate";
        |      |CREATE ALIAS IF NOT EXISTS has_LocalTime      FOR "molecule.sql.h2.functions.has_LocalTime";
        |      |CREATE ALIAS IF NOT EXISTS has_LocalDateTime  FOR "molecule.sql.h2.functions.has_LocalDateTime";
        |      |CREATE ALIAS IF NOT EXISTS has_OffsetTime     FOR "molecule.sql.h2.functions.has_OffsetTime";
        |      |CREATE ALIAS IF NOT EXISTS has_OffsetDateTime FOR "molecule.sql.h2.functions.has_OffsetDateTime";
        |      |CREATE ALIAS IF NOT EXISTS has_ZonedDateTime  FOR "molecule.sql.h2.functions.has_ZonedDateTime";
        |      |CREATE ALIAS IF NOT EXISTS has_UUID           FOR "molecule.sql.h2.functions.has_UUID";
        |      |CREATE ALIAS IF NOT EXISTS has_URI            FOR "molecule.sql.h2.functions.has_URI";
        |      |CREATE ALIAS IF NOT EXISTS has_Byte           FOR "molecule.sql.h2.functions.has_Byte";
        |      |CREATE ALIAS IF NOT EXISTS has_Short          FOR "molecule.sql.h2.functions.has_Short";
        |      |CREATE ALIAS IF NOT EXISTS has_Char           FOR "molecule.sql.h2.functions.has_Char";
        |      |
        |      |CREATE ALIAS IF NOT EXISTS hasNo_String         FOR "molecule.sql.h2.functions.hasNo_String";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Int            FOR "molecule.sql.h2.functions.hasNo_Int";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Long           FOR "molecule.sql.h2.functions.hasNo_Long";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Float          FOR "molecule.sql.h2.functions.hasNo_Float";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Double         FOR "molecule.sql.h2.functions.hasNo_Double";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Boolean        FOR "molecule.sql.h2.functions.hasNo_Boolean";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_BigInt         FOR "molecule.sql.h2.functions.hasNo_BigInt";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_BigDecimal     FOR "molecule.sql.h2.functions.hasNo_BigDecimal";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Date           FOR "molecule.sql.h2.functions.hasNo_Date";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Duration       FOR "molecule.sql.h2.functions.hasNo_Duration";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Instant        FOR "molecule.sql.h2.functions.hasNo_Instant";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_LocalDate      FOR "molecule.sql.h2.functions.hasNo_LocalDate";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_LocalTime      FOR "molecule.sql.h2.functions.hasNo_LocalTime";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_LocalDateTime  FOR "molecule.sql.h2.functions.hasNo_LocalDateTime";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_OffsetTime     FOR "molecule.sql.h2.functions.hasNo_OffsetTime";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_OffsetDateTime FOR "molecule.sql.h2.functions.hasNo_OffsetDateTime";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_ZonedDateTime  FOR "molecule.sql.h2.functions.hasNo_ZonedDateTime";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_UUID           FOR "molecule.sql.h2.functions.hasNo_UUID";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_URI            FOR "molecule.sql.h2.functions.hasNo_URI";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Byte           FOR "molecule.sql.h2.functions.hasNo_Byte";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Short          FOR "molecule.sql.h2.functions.hasNo_Short";
        |      |CREATE ALIAS IF NOT EXISTS hasNo_Char           FOR "molecule.sql.h2.functions.hasNo_Char";
        |      |\"\"\".stripMargin
        |
        |
        |  override val sqlReserved_h2: Option[Reserved] = $getReserved
        |}""".stripMargin
}
