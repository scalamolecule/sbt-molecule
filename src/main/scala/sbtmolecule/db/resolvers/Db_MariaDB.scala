package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.MariaDB


case class Db_MariaDB(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  val tables = getTables(MariaDB)

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
        |case class ${domain}_mariadb() extends ${domain}_ with MetaDb_mariadb {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("mariadb.sql")}"$getReserved
        |}""".stripMargin
}
