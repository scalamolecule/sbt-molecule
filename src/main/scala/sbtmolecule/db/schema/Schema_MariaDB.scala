package sbtmolecule.db.schema

import molecule.base.ast.*
import sbtmolecule.db.schema.sqlDialect.MariaDB


case class Schema_MariaDB(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {
  val domain = metaDomain.domain
  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.core.ast._
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
