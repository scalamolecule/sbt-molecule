package sbtmolecule.db.schema

import molecule.core.model.*
import sbtmolecule.db.schema.sqlDialect.MariaDB


case class Schema_MariaDB(dbModel: DbModel) extends Schema_SqlBase(dbModel) {
  val domain = dbModel.domain
  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${dbModel.pkg}.schema
        |
        |import molecule.core.model.*
        |
        |
        |object ${domain}Schema_mariadb extends ${domain}Schema with Schema_mariadb {
        |
        |  override val schemaData: List[String] = List(
        |    \"\"\"
        |      |${tables(MariaDB)}\"\"\".stripMargin
        |  )$getReserved
        |}""".stripMargin
}
