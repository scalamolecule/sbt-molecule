package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.H2


case class Db_H2(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  val tables = getTables(H2)

  def getSQL: String =
    s"""|$tables
        |CREATE ALIAS IF NOT EXISTS removeFromArray_ID             FOR "molecule.db.sql.h2.functions.removeFromArray_ID";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_String         FOR "molecule.db.sql.h2.functions.removeFromArray_String";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Int            FOR "molecule.db.sql.h2.functions.removeFromArray_Int";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Long           FOR "molecule.db.sql.h2.functions.removeFromArray_Long";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Float          FOR "molecule.db.sql.h2.functions.removeFromArray_Float";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Double         FOR "molecule.db.sql.h2.functions.removeFromArray_Double";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Boolean        FOR "molecule.db.sql.h2.functions.removeFromArray_Boolean";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_BigInt         FOR "molecule.db.sql.h2.functions.removeFromArray_BigInt";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_BigDecimal     FOR "molecule.db.sql.h2.functions.removeFromArray_BigDecimal";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Date           FOR "molecule.db.sql.h2.functions.removeFromArray_Date";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Duration       FOR "molecule.db.sql.h2.functions.removeFromArray_Duration";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Instant        FOR "molecule.db.sql.h2.functions.removeFromArray_Instant";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_LocalDate      FOR "molecule.db.sql.h2.functions.removeFromArray_LocalDate";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_LocalTime      FOR "molecule.db.sql.h2.functions.removeFromArray_LocalTime";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_LocalDateTime  FOR "molecule.db.sql.h2.functions.removeFromArray_LocalDateTime";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_OffsetTime     FOR "molecule.db.sql.h2.functions.removeFromArray_OffsetTime";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_OffsetDateTime FOR "molecule.db.sql.h2.functions.removeFromArray_OffsetDateTime";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_ZonedDateTime  FOR "molecule.db.sql.h2.functions.removeFromArray_ZonedDateTime";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_UUID           FOR "molecule.db.sql.h2.functions.removeFromArray_UUID";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_URI            FOR "molecule.db.sql.h2.functions.removeFromArray_URI";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Byte           FOR "molecule.db.sql.h2.functions.removeFromArray_Byte";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Short          FOR "molecule.db.sql.h2.functions.removeFromArray_Short";
        |CREATE ALIAS IF NOT EXISTS removeFromArray_Char           FOR "molecule.db.sql.h2.functions.removeFromArray_Char";
        |
        |CREATE ALIAS IF NOT EXISTS addPairs_ID             FOR "molecule.db.sql.h2.functions.addPairs_ID";
        |CREATE ALIAS IF NOT EXISTS addPairs_String         FOR "molecule.db.sql.h2.functions.addPairs_String";
        |CREATE ALIAS IF NOT EXISTS addPairs_Int            FOR "molecule.db.sql.h2.functions.addPairs_Int";
        |CREATE ALIAS IF NOT EXISTS addPairs_Long           FOR "molecule.db.sql.h2.functions.addPairs_Long";
        |CREATE ALIAS IF NOT EXISTS addPairs_Float          FOR "molecule.db.sql.h2.functions.addPairs_Float";
        |CREATE ALIAS IF NOT EXISTS addPairs_Double         FOR "molecule.db.sql.h2.functions.addPairs_Double";
        |CREATE ALIAS IF NOT EXISTS addPairs_Boolean        FOR "molecule.db.sql.h2.functions.addPairs_Boolean";
        |CREATE ALIAS IF NOT EXISTS addPairs_BigInt         FOR "molecule.db.sql.h2.functions.addPairs_BigInt";
        |CREATE ALIAS IF NOT EXISTS addPairs_BigDecimal     FOR "molecule.db.sql.h2.functions.addPairs_BigDecimal";
        |CREATE ALIAS IF NOT EXISTS addPairs_Date           FOR "molecule.db.sql.h2.functions.addPairs_Date";
        |CREATE ALIAS IF NOT EXISTS addPairs_Duration       FOR "molecule.db.sql.h2.functions.addPairs_Duration";
        |CREATE ALIAS IF NOT EXISTS addPairs_Instant        FOR "molecule.db.sql.h2.functions.addPairs_Instant";
        |CREATE ALIAS IF NOT EXISTS addPairs_LocalDate      FOR "molecule.db.sql.h2.functions.addPairs_LocalDate";
        |CREATE ALIAS IF NOT EXISTS addPairs_LocalTime      FOR "molecule.db.sql.h2.functions.addPairs_LocalTime";
        |CREATE ALIAS IF NOT EXISTS addPairs_LocalDateTime  FOR "molecule.db.sql.h2.functions.addPairs_LocalDateTime";
        |CREATE ALIAS IF NOT EXISTS addPairs_OffsetTime     FOR "molecule.db.sql.h2.functions.addPairs_OffsetTime";
        |CREATE ALIAS IF NOT EXISTS addPairs_OffsetDateTime FOR "molecule.db.sql.h2.functions.addPairs_OffsetDateTime";
        |CREATE ALIAS IF NOT EXISTS addPairs_ZonedDateTime  FOR "molecule.db.sql.h2.functions.addPairs_ZonedDateTime";
        |CREATE ALIAS IF NOT EXISTS addPairs_UUID           FOR "molecule.db.sql.h2.functions.addPairs_UUID";
        |CREATE ALIAS IF NOT EXISTS addPairs_URI            FOR "molecule.db.sql.h2.functions.addPairs_URI";
        |CREATE ALIAS IF NOT EXISTS addPairs_Byte           FOR "molecule.db.sql.h2.functions.addPairs_Byte";
        |CREATE ALIAS IF NOT EXISTS addPairs_Short          FOR "molecule.db.sql.h2.functions.addPairs_Short";
        |CREATE ALIAS IF NOT EXISTS addPairs_Char           FOR "molecule.db.sql.h2.functions.addPairs_Char";
        |
        |CREATE ALIAS IF NOT EXISTS removePairs_ID             FOR "molecule.db.sql.h2.functions.removePairs_ID";
        |CREATE ALIAS IF NOT EXISTS removePairs_String         FOR "molecule.db.sql.h2.functions.removePairs_String";
        |CREATE ALIAS IF NOT EXISTS removePairs_Int            FOR "molecule.db.sql.h2.functions.removePairs_Int";
        |CREATE ALIAS IF NOT EXISTS removePairs_Long           FOR "molecule.db.sql.h2.functions.removePairs_Long";
        |CREATE ALIAS IF NOT EXISTS removePairs_Float          FOR "molecule.db.sql.h2.functions.removePairs_Float";
        |CREATE ALIAS IF NOT EXISTS removePairs_Double         FOR "molecule.db.sql.h2.functions.removePairs_Double";
        |CREATE ALIAS IF NOT EXISTS removePairs_Boolean        FOR "molecule.db.sql.h2.functions.removePairs_Boolean";
        |CREATE ALIAS IF NOT EXISTS removePairs_BigInt         FOR "molecule.db.sql.h2.functions.removePairs_BigInt";
        |CREATE ALIAS IF NOT EXISTS removePairs_BigDecimal     FOR "molecule.db.sql.h2.functions.removePairs_BigDecimal";
        |CREATE ALIAS IF NOT EXISTS removePairs_Date           FOR "molecule.db.sql.h2.functions.removePairs_Date";
        |CREATE ALIAS IF NOT EXISTS removePairs_Duration       FOR "molecule.db.sql.h2.functions.removePairs_Duration";
        |CREATE ALIAS IF NOT EXISTS removePairs_Instant        FOR "molecule.db.sql.h2.functions.removePairs_Instant";
        |CREATE ALIAS IF NOT EXISTS removePairs_LocalDate      FOR "molecule.db.sql.h2.functions.removePairs_LocalDate";
        |CREATE ALIAS IF NOT EXISTS removePairs_LocalTime      FOR "molecule.db.sql.h2.functions.removePairs_LocalTime";
        |CREATE ALIAS IF NOT EXISTS removePairs_LocalDateTime  FOR "molecule.db.sql.h2.functions.removePairs_LocalDateTime";
        |CREATE ALIAS IF NOT EXISTS removePairs_OffsetTime     FOR "molecule.db.sql.h2.functions.removePairs_OffsetTime";
        |CREATE ALIAS IF NOT EXISTS removePairs_OffsetDateTime FOR "molecule.db.sql.h2.functions.removePairs_OffsetDateTime";
        |CREATE ALIAS IF NOT EXISTS removePairs_ZonedDateTime  FOR "molecule.db.sql.h2.functions.removePairs_ZonedDateTime";
        |CREATE ALIAS IF NOT EXISTS removePairs_UUID           FOR "molecule.db.sql.h2.functions.removePairs_UUID";
        |CREATE ALIAS IF NOT EXISTS removePairs_URI            FOR "molecule.db.sql.h2.functions.removePairs_URI";
        |CREATE ALIAS IF NOT EXISTS removePairs_Byte           FOR "molecule.db.sql.h2.functions.removePairs_Byte";
        |CREATE ALIAS IF NOT EXISTS removePairs_Short          FOR "molecule.db.sql.h2.functions.removePairs_Short";
        |CREATE ALIAS IF NOT EXISTS removePairs_Char           FOR "molecule.db.sql.h2.functions.removePairs_Char";
        |""".stripMargin



  def get: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.base.metaModel.*
        |import molecule.db.core.api.*
        |
        |
        |object ${domain}_MetaDb_h2 extends ${domain}_MetaDb with MetaDb_h2 {
        |
        |  /** Resource path to SQL schema file or Datomic EDN file to create the database */
        |  override val schemaResourcePath: String = "${schemaResourcePath("h2.sql")}"$getReserved
        |}""".stripMargin
}
