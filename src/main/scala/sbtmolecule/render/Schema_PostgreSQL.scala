package sbtmolecule.render

import molecule.base.ast.*
import sbtmolecule.db.sqlDialect.Postgres


case class Schema_PostgreSQL(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {

  def get: String =
    s"""|/*
        |* AUTO-GENERATED schema boilerplate code
        |*
        |* To change:
        |* 1. edit domain definition file in `${metaDomain.pkg}/`
        |* 2. `sbt compile -Dmolecule=true`
        |*/
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.base.api._
        |
        |
        |object ${metaDomain.domain}Schema_postgres extends ${metaDomain.domain}Schema with Schema_postgres {
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
