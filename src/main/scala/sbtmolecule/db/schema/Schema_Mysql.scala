package sbtmolecule.db.schema

import molecule.base.ast.*
import sbtmolecule.db.schema.sqlDialect.Mysql


case class Schema_Mysql(metaDomain: MetaDomain) extends Schema_SqlBase(metaDomain) {
  val domain = metaDomain.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.core.ast._
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
