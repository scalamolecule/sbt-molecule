package sbtmolecule.render

import molecule.base.ast.*
import molecule.base.util.BaseHelpers


class DslFormatting(metaDomain: MetaDomain, metaEntity: MetaEntity, arity: Int = 0) extends BaseHelpers {
  val pkg      = metaDomain.pkg + ".dsl"
  val domain   = metaDomain.domain
  val maxArity = metaDomain.maxArity
  val ent      = metaEntity.ent
  val attrs    = metaEntity.attrs
  val refs     = attrs.filter(_.ref.nonEmpty)
  val backRefs = metaEntity.backRefs

  def camel(s: String) = s"${s.head.toUpper}${s.tail}"

  def getTpe(s: String) = s match {
    case "ID" => "Long"
    case t    => t
  }

  lazy val maxAttr      = attrs.map(_.attr.length).max
  lazy val maxBaseTpe   = attrs.map(a => getTpe(a.baseTpe).length).max
  lazy val maxRefAttr   = attrs.filter(_.ref.isDefined).map(entity => entity.attr.length).max
  lazy val maxRefEntity = attrs.flatMap(_.ref.map(_.length)).max

  lazy val padAttr      = (s: String) => padS(maxAttr, s)
  lazy val padType      = (s: String) => padS(maxBaseTpe, s)
  lazy val padRefAttr   = (s: String) => padS(maxRefAttr, s)
  lazy val padRefEntity = (s: String) => padS(maxRefEntity, s)

  lazy val V        = ('A' + arity - 1).toChar
  lazy val tpes     = (0 until arity) map (n => (n + 'A').toChar)
  lazy val _0       = "_" + arity
  lazy val _1       = "_" + (arity + 1)
  lazy val _2       = "_" + (arity + 2)
  lazy val ent_0    = ent + _0
  lazy val ent_1    = ent + _1
  lazy val ent_2    = ent + _2
  lazy val `, A`    = if (arity == 0) "" else ", " + tpes.mkString(", ")
  lazy val `A..U`   = if (arity <= 1) "" else tpes.init.mkString("", ", ", ", ")
  lazy val `A..V`   = if (arity == 0) "" else tpes.mkString(", ")
  lazy val `A..V, ` = if (arity == 0) "" else tpes.mkString("", ", ", ", ")
  lazy val `[A..V]` = if (arity == 0) "" else tpes.mkString("[", ", ", "]")

  def padN(n: Int) = if (n < 10) s"0$n" else n
  val n0 = padN(arity)
}
