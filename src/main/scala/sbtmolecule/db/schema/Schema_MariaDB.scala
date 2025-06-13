package sbtmolecule.db.schema

import molecule.base.metaModel.*
import sbtmolecule.db.schema.sqlDialect.MariaDB


case class Schema_MariaDB(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {
  val domain = metaDomain.domain
  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.base.metaModel.*
        |import molecule.db.core.api.*
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
