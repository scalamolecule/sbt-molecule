package sbtmolecule

import molecule.base.ast.*


case class SchemaBase(metaDomain: MetaDomain) {
  val domain = metaDomain.domain

  def get: String =
    s"""|// AUTO-GENERATED Molecule Domain Schema boilerplate code for `$domain`
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.base.ast.*
        |import molecule.core.ast.Schema
        |
        |
        |trait ${domain}Schema extends Schema {
        |
        |  override val metaDomain: MetaDomain =
        |    ${metaDomain.render(2)}
        |
        |
        |  override val entityMap: Map[String, MetaEntity] = ${metaDomain.entityMap(1)}
        |
        |
        |  override val attrMap: Map[String, (Card, String, Seq[String])] = ${metaDomain.attrMap(1)}
        |
        |
        |  override val uniqueAttrs: List[String] = ${metaDomain.uniqueAttrs}
        |}""".stripMargin
}
