package sbtmolecule.db.schema

import molecule.core.model.*
import sbtmolecule.db.schema.sqlDialect.Mysql


case class Schema_Mysql(dbModel: DbModel) extends Schema_SqlBase(dbModel) {
  val domain = dbModel.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${dbModel.pkg}.schema
        |
        |import molecule.core.model.*
        |
        |
        |object ${domain}Schema_mysql extends ${domain}Schema with Schema_mysql {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(Mysql)}\"\"\".stripMargin
        |  )$getReserved
        |}""".stripMargin
}
