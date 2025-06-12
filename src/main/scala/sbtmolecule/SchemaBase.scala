package sbtmolecule

import molecule.core.model.*


case class SchemaBase(dbModel: DbModel) {
  val domain = dbModel.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule database schema boilerplate code for `$domain`
        |package ${dbModel.pkg}.schema
        |
        |import molecule.core.model.*
        |
        |
        |trait ${domain}Schema extends Schema {
        |
        |  override val entityMap: Map[String, DbEntity] = ${dbModel.entityMap(1)}
        |
        |
        |  override val attrMap: Map[String, (Card, String, Seq[String])] = ${dbModel.attrMap(1)}
        |
        |
        |  override val uniqueAttrs: List[String] = ${dbModel.uniqueAttrs}
        |}""".stripMargin
}
