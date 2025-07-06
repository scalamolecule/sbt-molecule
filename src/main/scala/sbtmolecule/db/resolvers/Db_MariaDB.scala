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
        |import molecule.db.core.api.*
        |
        |
        |object ${domain}_MetaDb_mariadb extends ${domain}_MetaDb with MetaDb_mariadb {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("mariadb.sql")}"$getReserved
        |}""".stripMargin
}
