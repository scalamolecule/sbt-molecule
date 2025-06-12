package sbtmolecule.db.schema

import molecule.core.model.*
import sbtmolecule.db.schema.sqlDialect.Postgres


case class Schema_PostgreSQL(dbModel: DbModel) extends Schema_SqlBase(dbModel) {
  val domain = dbModel.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${dbModel.pkg}.schema
        |
        |import molecule.core.model.*
        |
        |
        |object ${domain}Schema_postgres extends ${domain}Schema with Schema_postgres {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(Postgres)}
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
        |  )$getReserved
        |}""".stripMargin
}
