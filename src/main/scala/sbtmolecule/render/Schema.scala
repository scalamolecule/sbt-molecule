package sbtmolecule.render

import molecule.base.ast.*


case class Schema(schema: MetaSchema) {

  def get: String =
    s"""|/*
        |* AUTO-GENERATED schema boilerplate code
        |*
        |* To change:
        |* 1. edit data model file in `${schema.pkg}/`
        |* 2. `sbt compile -Dmolecule=true`
        |*/
        |package ${schema.pkg}.schema
        |
        |import molecule.base.api.Schema
        |import molecule.base.ast._
        |
        |
        |object ${schema.domain}Schema extends Schema
        |  with ${schema.domain}Schema_Datomic
        |  with ${schema.domain}Schema_H2
        |  with ${schema.domain}Schema_MariaDB
        |  with ${schema.domain}Schema_Mysql
        |  with ${schema.domain}Schema_PostgreSQL
        |  with ${schema.domain}Schema_SQlite {
        |
        |  val metaSchema: MetaSchema =
        |    ${schema.render(2)}
        |
        |
        |  val nsMap: Map[String, MetaNs] = ${schema.nsMap(1)}
        |
        |
        |  val attrMap: Map[String, (Card, String, Seq[String])] = ${schema.attrMap(1)}
        |
        |
        |  val uniqueAttrs: List[String] = ${schema.uniqueAttrs}
        |}""".stripMargin
}
