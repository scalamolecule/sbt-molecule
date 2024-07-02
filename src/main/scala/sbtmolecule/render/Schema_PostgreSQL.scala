package sbtmolecule.render

import molecule.base.ast.*
import sbtmolecule.render.sql.*


case class Schema_PostgreSQL(schema: MetaSchema) extends Schema_SqlBase(schema) {

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
        |trait ${schema.domain}Schema_PostgreSQL extends Schema {
        |
        |  override val sqlSchema_postgres: String =
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
        |
        |
        |  // Index to lookup if name collides with db keyword
        |  override val sqlReserved_postgres: Option[Reserved] = $getReserved
        |}""".stripMargin
}
