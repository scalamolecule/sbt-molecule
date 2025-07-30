package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.PostgreSQL


case class Db_PostgreSQL(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  val tables = getTables(PostgreSQL)

  def getSQL: String =
    s"""|$tables
        |CREATE OR REPLACE FUNCTION _final_median(numeric[])
        |   RETURNS numeric AS
        |$$$$
        |   SELECT AVG(val)
        |   FROM (
        |     SELECT DISTINCT val
        |     FROM unnest($$1) val
        |     ORDER BY 1
        |     LIMIT  2 - MOD(array_upper($$1, 1), 2)
        |     OFFSET CEIL(array_upper($$1, 1) / 2.0) - 1
        |   ) sub;
        |$$$$
        |LANGUAGE 'sql' IMMUTABLE;
        |
        |CREATE AGGREGATE median(numeric) (
        |  SFUNC=array_append,
        |  STYPE=numeric[],
        |  FINALFUNC=_final_median,
        |  INITCOND='{}'
        |);
        |""".stripMargin


  def get: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.base.metaModel.*
        |import molecule.db.common.api.*
        |
        |
        |case class ${domain}_postgresql() extends ${domain}_ with MetaDb_postgresql {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("postgresql.sql")}"$getReserved
        |}""".stripMargin
}
