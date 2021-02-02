package sbtmolecule.generate

import sbtmolecule.Ast.{BackRef, DefAttr, Enum, Model, Namespace, Optional, Ref, Val, firstLow, padS}

class Spacing(
  model: Model,
  namespace: Namespace,
  in: Int = 0,
  out: Int = 0,
  level: Int = 0
) {
  lazy val domain0  = model.domain
  lazy val maxIn    = model.in
  lazy val maxOut   = model.out
  lazy val maxLevel = maxOut.min(7)
  lazy val domain   = firstLow(domain0)
  lazy val ns       = namespace.ns
  lazy val attrs    = namespace.attrs

  lazy val padAttr      = (s: String) => padS(attrs.map(_.attr).filterNot(_.startsWith("_")).map(_.length + 1).max, s)
  lazy val padAttrClean = (s: String) => padS(attrs.map(_.attrClean).filterNot(_.startsWith("_")).map(_.length).max, s)
  lazy val padType      = (s: String) => padS(attrs.map(_.tpe).map(_.length).max, s)
  lazy val padClass     = (s: String) => padS(attrs.map(_.clazz).filterNot(_.startsWith("Back")).map(_.length).max, s)


  lazy val maxRefs : Seq[Int] = attrs.collect {
    case Ref(_, attrClean, _, _, _, _, _, _, _, _, _) => attrClean.length
    case BackRef(_, attrClean, _, _, _, _, _, _, _)   => attrClean.length
  }
  lazy val maxRefNs: Seq[Int] = attrs.collect {
    case Ref(_, _, _, _, _, _, refNs, _, _, _, _) => refNs.length
    case BackRef(_, _, _, _, _, _, backRef, _, _) => backRef.length
  }
  lazy val maxBackRefs: Seq[Int] = attrs.collect {
    case BackRef(_, _, _, _, _, _, backRef, _, _) => backRef.length
  }

  lazy val padRef   = (attrClean: String) => padS(maxRefs.max, attrClean)
  lazy val padRefNs = (refNs: String) => padS(maxRefNs.max, refNs)
  lazy val padBackRefs = (backRef: String) => padS(maxBackRefs.max, backRef)


  def formatted(a: DefAttr) = {
    val attrSp = padAttrClean(a.attrClean)
    val typeSp = padType(a.tpe)
    val ref    = a.attrClean.capitalize + padRef(a.attrClean)
    val ref_   = a.attrClean.capitalize + "_" + padRef(a.attrClean)
    (
      ns + "_" + a.attrClean + attrSp,
      ns + "_" + a.attrClean + "$" + attrSp,
      ns + "_" + ref,
      ns + "_" + ref_,
      a.attr + padAttr(a.attr),
      a.attrClean + "$" + attrSp,
      a.attrClean + "_" + attrSp,
      a.attrClean + "K" + attrSp,
      a.attrClean + "K_" + attrSp,
      a.tpe + typeSp,
      "Option[" + a.tpe + typeSp + "]",
      ref
    )
  }


  def nn(i: Int) = if (i < 10) s"0$i" else s"$i"

  lazy val ns_       = ns + "_"
  lazy val ns_0_0    = ns + "_" + in + "_" + out
  lazy val ns_0_0_L0 = ns + "_" + in + "_" + out + "_L" + level
  lazy val ns_0_1    = if (out < maxOut) ns + "_" + in + "_" + (out + 1) else "D" + nn(out + 1)
  lazy val ns_0_1_L0 = ns + "_" + in + "_" + (out + 1) + "_L" + level
  lazy val ns_1_0    = if (maxIn > 0 && in < maxIn) ns + "_" + (in + 1) + "_" + out else "D" + nn(out + 1)
  lazy val ns_1_1    = if (maxIn > 0) ns + "_" + (in + 1) + "_" + (out + 1) else "D" + nn(out + 2)

  lazy val `o0, p0`             = (0 to level).map(l => s"o$l, p$l").mkString(", ")
  lazy val `o0[_], p0`          = (0 to level).map(l => s"o$l[_], p$l").mkString(", ")
  lazy val `p0 with o1[p1]`     = level match {
    case 0 => "p0"
    case 1 => "p0 with o1[p1]"
    case 2 => "p0 with o1[p1 with o2[p2]]"
    case 3 => "p0 with o1[p1 with o2[p2 with o3[p3]]]"
    case 4 => "p0 with o1[p1 with o2[p2 with o3[p3 with o4[p4]]]]"
    case 5 => "p0 with o1[p1 with o2[p2 with o3[p3 with o4[p4 with o5[p5]]]]]"
    case 6 => "p0 with o1[p1 with o2[p2 with o3[p3 with o4[p4 with o5[p5 with o6[p6]]]]]]"
    case 7 => "p0 with o1[p1 with o2[p2 with o3[p3 with o4[p4 with o5[p5 with o6[p6 with o7[p7]]]]]]]"
  }
  lazy val `o0, p0 with o1[p1]` = "o0, " + `p0 with o1[p1]`

  lazy val `, I1, I2` = if (in == 0) "" else ", " + (1 to in).map("I" + _).mkString(", ")
  lazy val `, A, B`   = if (out == 0) "" else ", " + (65 until (65 + out)).map(_.toChar).mkString(", ")
  lazy val `, I1, A`  = `, I1, I2` + `, A, B`

  lazy val D01 = "D" + nn(out + 1)
  lazy val D00 = "D" + nn(out)
}
