package sbtmolecule.db.dsl

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.Formatting


case class Entity(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
) extends Formatting(metaDomain, metaEntity) {

  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private val attrList  : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attribute <- entity.attributes
    } yield entity.entity + "." + attribute.attribute
  }

  private val entityIndex = entityList.indexOf(entity)
  private val idCoord     = s"List($entityIndex, ${attrList.indexOf(entity + ".id")})"

  private val (rightRefTrait, rightRef) = if (refs.isEmpty) ("", "") else {
    (
      s" with $entity",
      s"""
         |
         |trait $entity {
         |  final def ?              (optEntity: Molecule_0     ) = new ops.$entity_refs_cur             (_DataModel(List(_OptEntity(attrsOnly(optEntity.dataModel)))))
         |  final def ?[T           ](optEntity: Molecule_1[T  ]) = new ops.$entity_refs_next[Option[T  ]](_DataModel(List(_OptEntity(attrsOnly(optEntity.dataModel)))))
         |  final def ?[Tpl <: Tuple](optEntity: Molecule_n[Tpl]) = new ops.$entity_refs_next[Option[Tpl]](_DataModel(List(_OptEntity(attrsOnly(optEntity.dataModel)))))
         |}""".stripMargin
    )
  }


  def get: String = {
    s"""// AUTO-GENERATED Molecule boilerplate code
       |package $pkg.$domain
       |
       |import molecule.core.dataModel.{AttrOneTacID as _AttrOneTacID, DataModel as _DataModel, Eq as _Eq, OptEntity as _OptEntity}
       |import molecule.db.common.api.*
       |import molecule.db.common.ops.ModelTransformations_.*
       |
       |
       |object $entity extends ops.${entity}_0(_DataModel(Nil, firstEntityIndex = 0))$rightRefTrait {
       |  final def apply(id: Long, ids: Long*) = new ops.${entity}_0(
       |    _DataModel(List(_AttrOneTacID("$entity", "id", _Eq, id +: ids, coord = $idCoord)), firstEntityIndex = $entityIndex)
       |  )
       |  final def apply(ids: Iterable[Long]) = new ops.${entity}_0(
       |    _DataModel(List(_AttrOneTacID("$entity", "id", _Eq, ids.toSeq, coord = $idCoord)), firstEntityIndex = $entityIndex)
       |  )
       |}$rightRef
       |""".stripMargin
  }
}
