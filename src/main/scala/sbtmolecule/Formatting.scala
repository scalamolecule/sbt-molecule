package sbtmolecule

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import molecule.base.util.BaseHelpers

class Formatting(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int = 0
) extends BaseHelpers {
  val pkg         = metaDomain.pkg + ".dsl"
  val domain      = metaDomain.domain
  val entity      = metaEntity.entity
  val isJoinTable = metaEntity.isJoinTable

  val (cur, next) = arity match {
    case 0 => ("_0", "_1")
    case 1 => ("_1", "_n")
    case _ => ("_n", "_n")
  }

  val entA = entity + cur
  val entB = entity + next

  val entity_refs_cur  = entity + "_refs" + cur
  val entity_refs_next = entity + "_refs" + next


  val allAttributes = metaEntity.attributes
  val attributes    = allAttributes.filterNot(_.relationship.contains("OneToMany"))
  val refs          = allAttributes.filter(_.ref.nonEmpty)
  val backRefs      = metaEntity.backRefs

  def camel(s: String) = s"${s.head.toUpper}${s.tail}"

  def getTpe(s: String) = s match {
    case "ID" => "Long"
    case t    => t
  }

  lazy val maxAttr      = attributes.map(_.attribute.length).max
  lazy val maxAttrClean = attributes.map(a => a.alias.getOrElse(a.attribute).length).max
  lazy val maxBaseTpe   = attributes.map(a => getTpe(a.baseTpe).length).max
  lazy val maxBaseTpe1  = attributes.map(a => a.enumTpe.getOrElse(getTpe(a.baseTpe)).length).max
  lazy val maxRefAttr   = allAttributes.filter(_.ref.isDefined).map(entity => entity.attribute.length).max
  lazy val maxRefEntity = allAttributes.flatMap(_.ref.map(_.length)).max

  lazy val padAttr      = (s: String) => padS(maxAttr, s)
  lazy val padAttrClean = (s: String) => padS(maxAttrClean, s)
  lazy val padType      = (s: String) => padS(maxBaseTpe, s)
  lazy val padType1     = (s: String) => padS(maxBaseTpe1, s)
  lazy val padRefAttr   = (s: String) => padS(maxRefAttr, s)
  lazy val padRefEntity = (s: String) => padS(maxRefEntity, s)
}
