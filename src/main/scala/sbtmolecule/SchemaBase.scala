package sbtmolecule

import molecule.base.metaModel.*


case class SchemaBase(metaDomain: MetaDomain) {
  val domain = metaDomain.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule database schema boilerplate code for `$domain`
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.base.metaModel.*
        |import molecule.db.core.api.Schema
        |
        |trait ${domain}Schema extends Schema {
        |
        |  override val entityMap: Map[String, MetaEntity] = ${metaDomain.entityMap(1)}
        |
        |
        |  override val attrMap: Map[String, (Cardinality, String, Seq[String])] = ${metaDomain.attrMap(1)}
        |
        |
        |  override val uniqueAttrs: List[String] = ${metaDomain.uniqueAttrs}
        |}""".stripMargin
}
