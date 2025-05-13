package sbtmolecule.render

import molecule.db.base.ast.*
import sbtmolecule.sqlDialect.MariaDB


case class Schema_MariaDB(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {

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
        |import molecule.db.base.api._
        |
        |
        |object ${metaDomain.domain}Schema_mariadb extends ${metaDomain.domain}Schema with Schema_mariadb {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(MariaDB)}\"\"\".stripMargin
        |  )$getReserved
        |}""".stripMargin
}
