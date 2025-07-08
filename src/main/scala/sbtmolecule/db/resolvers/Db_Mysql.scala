package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import sbtmolecule.db.sqlDialect.Mysql


case class Db_Mysql(metaDomain: MetaDomain) extends SqlBase(metaDomain) {

  val tables = getTables(Mysql)

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
        |case class ${domain}_MetaDb_mysql() extends ${domain}_MetaDb with MetaDb_mysql {
        |
        |  override val schemaResourcePath: String = "${schemaResourcePath("mysql.sql")}"$getReserved
        |}""".stripMargin
}
