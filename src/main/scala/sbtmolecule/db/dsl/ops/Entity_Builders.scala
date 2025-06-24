package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class Entity_Builders(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int,
  attrIndexPrev: Int
) extends Formatting(metaDomain, metaEntity) {


//  val entity_Attrs = Entity_Attrs(metaDomain, metaEntity, nsIndex, attrIndexPrev).get
//
  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private val attrList  : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attribute <- entity.attributes
    } yield entity.entity + "." + attribute.attribute
  }
  private val entity_Builders: String = List(0, 1, 2)
    .map(Entity_Builder(metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")
//
//  private val entity_Expr = Entity_Expr(metaDomain, metaEntity, nsIndex, attrIndexPrev).get
//  private val entity_Sort = Entity_Sort(metaDomain, metaEntity).get

  def get: String = {
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
       |package $pkg.$domain
       |package ops // to access enums and let them be public to the user
       |
       |$imports
       |
       |$entity_Builders
       |""".stripMargin
  }
}
