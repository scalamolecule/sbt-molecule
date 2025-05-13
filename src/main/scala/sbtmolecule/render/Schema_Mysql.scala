package sbtmolecule.render

import molecule.db.base.ast.*
import sbtmolecule.sqlDialect.Mysql


case class Schema_Mysql(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {

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
        |import molecule.db.core.api._
        |
        |
        |object ${metaDomain.domain}Schema_mysql extends ${metaDomain.domain}Schema with Schema_mysql {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(Mysql)}\"\"\".stripMargin
        |  )$getReserved
        |}""".stripMargin
}
