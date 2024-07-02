package sbtmolecule.render

import molecule.base.ast.*
import sbtmolecule.render.sql.*


case class Schema_Mysql(schema: MetaSchema) extends Schema_SqlBase(schema) {

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
        |trait ${schema.domain}Schema_Mysql extends Schema {
        |
        |  override val sqlSchema_mysql: String =
        |    \"\"\"
        |      |${tables(Mysql)}\"\"\".stripMargin
        |
        |
        |  // Index to lookup if name collides with db keyword
        |  override val sqlReserved_mysql: Option[Reserved] = $getReserved
        |}""".stripMargin
}
