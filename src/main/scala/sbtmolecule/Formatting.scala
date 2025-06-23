package sbtmolecule

import molecule.base.metaModel.*
import molecule.base.util.BaseHelpers


class Formatting(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int = 0
) extends BaseHelpers {
  val pkg      = metaDomain.pkg + ".dsl"
  val domain   = metaDomain.domain
  val maxArity = metaDomain.maxArity
  val entity   = metaEntity.entity
  val first    = arity == 0

  val (cur, next, next2) = arity match {
    case 0 => ("_0", "_1", "_n")
    case 1 => ("_1", "_n", "_n")
    case _ => ("_n", "_n", "_n")
  }

  val entA  = entity + cur
  val entB = entity + next
  val entC = entity + next2

  val entity_refs_cur = entity + "_refs" + cur
  val entity_refs_next = entity + "_refs" + next


  val attributes = metaEntity.attributes
  val refs       = attributes.filter(_.ref.nonEmpty)
  val backRefs   = metaEntity.backRefs

  def camel(s: String) = s"${s.head.toUpper}${s.tail}"

  def getTpe(s: String) = s match {
    case "ID" => "Long"
    case t    => t
  }

  lazy val maxAttr      = attributes.map(_.attribute.length).max
  lazy val maxBaseTpe   = attributes.map(a => getTpe(a.baseTpe).length).max
  lazy val maxBaseTpe1  = attributes.map(a => a.enumTpe.getOrElse(a.baseTpe).length).max
  lazy val maxRefAttr   = attributes.filter(_.ref.isDefined).map(entity => entity.attribute.length).max
  lazy val maxRefEntity = attributes.flatMap(_.ref.map(_.length)).max

  lazy val padAttr      = (s: String) => padS(maxAttr, s)
  lazy val padType      = (s: String) => padS(maxBaseTpe, s)
  lazy val padType1     = (s: String) => padS(maxBaseTpe1, s)
  lazy val padRefAttr   = (s: String) => padS(maxRefAttr, s)
  lazy val padRefEntity = (s: String) => padS(maxRefEntity, s)

  //  lazy val V        = ('A' + arity - 1).toChar
  //  lazy val tpes     = (0 until arity) map (n => (n + 'A').toChar)
  //  lazy val _0       = "_" + arity
  //  lazy val _1       = "_" + (arity + 1)
  //  lazy val _2       = "_" + (arity + 2)
  //  lazy val ent_0    = entity + _0
  //  lazy val ent_1    = entity + _1
  //  lazy val ent_2    = entity + _2
  //  lazy val `, A`    = if (arity == 0) "" else ", " + tpes.mkString(", ")
  //  lazy val `A..U`   = if (arity <= 1) "" else tpes.init.mkString("", ", ", ", ")
  //  lazy val `A..V`   = if (arity == 0) "" else tpes.mkString(", ")
  //  lazy val `A..V, ` = if (arity == 0) "" else tpes.mkString("", ", ", ", ")
  //  lazy val `[A..V]` = if (arity == 0) "" else tpes.mkString("[", ", ", "]")
  //
  //  def padN(n: Int) = if (n < 10) s"0$n" else n
  //  val n0 = padN(arity)
}
