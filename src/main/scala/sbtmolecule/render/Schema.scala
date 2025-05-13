package sbtmolecule.render

import molecule.db.base.ast.*


case class Schema(metaDomain: MetaDomain) {

  def get: String =
    s"""|/*
        |* AUTO-GENERATED schema boilerplate code
        |*
        |* To change:
        |* 1. edit domain definition file in `${metaDomain.pkg}/`
        |* 2. `sbt compile -Dmolecule=true`
        |*/
        |package ${metaDomain.pkg}.schema
        |
        |import molecule.db.base.ast.*
        |import molecule.db.core.api.Schema
        |
        |
        |trait ${metaDomain.domain}Schema extends Schema {
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
