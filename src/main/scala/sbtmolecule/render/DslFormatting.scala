package sbtmolecule.render

import molecule.base.ast.*
import molecule.base.util.BaseHelpers


class DslFormatting(schema: MetaSchema, namespace: MetaNs, arity: Int = 0) extends BaseHelpers {
  val pkg      = schema.pkg + ".dsl"
  val domain   = schema.domain
  val maxArity = schema.maxArity
  val ns       = namespace.ns
  val part     = if (ns.contains("_")) ns.take(ns.indexOf("_") + 1) else ""
  val attrs    = namespace.attrs
  val refs     = attrs.filter(_.refNs.nonEmpty)
  val backRefs = namespace.backRefNss

  def camel(s: String) = s"${s.head.toUpper}${s.tail}"

  def getTpe(s: String) = s match {
    case "ID" => "Long"
    case t    => t
  }

  lazy val maxAttr    = attrs.map(_.attr.length).max
  lazy val maxBaseTpe = attrs.map(a => getTpe(a.baseTpe).length).max
  lazy val maxRefAttr = attrs.filter(_.refNs.isDefined).map(ns => ns.attr.length).max
  lazy val maxRefNs   = attrs.flatMap(_.refNs.map(_.length)).max

  lazy val padAttr    = (s: String) => padS(maxAttr, s)
  lazy val padType    = (s: String) => padS(maxBaseTpe, s)
  lazy val padRefAttr = (s: String) => padS(maxRefAttr, s)
  lazy val padRefNs   = (s: String) => padS(maxRefNs, s)

  lazy val V        = ('A' + arity - 1).toChar
  lazy val tpes     = (0 until arity) map (n => (n + 'A').toChar)
  lazy val _0       = "_" + arity
  lazy val _1       = "_" + (arity + 1)
  lazy val _2       = "_" + (arity + 2)
  lazy val ns_0     = ns + _0
  lazy val ns_1     = ns + _1
  lazy val ns_2     = ns + _2
  lazy val `, A`    = if (arity == 0) "" else ", " + tpes.mkString(", ")
  lazy val `A..U`   = if (arity <= 1) "" else tpes.init.mkString("", ", ", ", ")
  lazy val `A..V`   = if (arity == 0) "" else tpes.mkString(", ")
  lazy val `A..V, ` = if (arity == 0) "" else tpes.mkString("", ", ", ", ")
  lazy val `[A..V]` = if (arity == 0) "" else tpes.mkString("[", ", ", "]")

  def padN(n: Int) = if (n < 10) s"0$n" else n
  val n0 = padN(arity)
}
