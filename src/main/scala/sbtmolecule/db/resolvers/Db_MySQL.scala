package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.MySQL


case class Db_MySQL(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  val tables = getTables(MySQL)

  def getSQL: String =
    s"""|$tables
        |""".stripMargin

  def get: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.base.metaModel.*
        |import molecule.db.common.api.*
        |
        |
        |case class ${domain}_mysql() extends ${domain}_ with MetaDb_mysql {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("mysql.sql")}"$getReserved
        |}""".stripMargin
}
