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
  lazy val maxIn    = model.maxIn
  lazy val maxOut   = model.maxOut
  lazy val maxLevel = maxOut.min(7)
  lazy val domain   = firstLow(domain0)
  lazy val ns       = namespace.ns
  lazy val attrs    = namespace.attrs

  lazy val padAttr      = (s: String) => padS(attrs.map(_.attr).filterNot(_.startsWith("_")).map(_.length + 1).max, s)
  lazy val padAttrClean = (s: String) => padS(attrs.map(_.attrClean).filterNot(_.startsWith("_")).map(_.length).max, s)
  lazy val padType      = (s: String) => padS(attrs.map(_.tpe).map(_.length).max, s)
  lazy val padBaseType  = (s: String) => padS(attrs.map(_.baseTpe).map(_.length).max, s)
  lazy val padClass     = (s: String) => padS(attrs.map(_.clazz).filterNot(_.startsWith("Back")).map(_.length).max, s)

  lazy val maxRefs    : Seq[Int] = attrs.collect {
    case Ref(_, attrClean, _, _, _, _, _, _, _, _, _) => attrClean.length
    case BackRef(_, attrClean, _, _, _, _, _, _, _)   => attrClean.length
  }
  lazy val maxRefNs   : Seq[Int] = attrs.collect {
    case Ref(_, _, _, _, _, _, refNs, _, _, _, _) => refNs.length
  }
  lazy val maxBackRefs: Seq[Int] = attrs.collect {
    case BackRef(_, _, _, _, _, _, backRef, _, _) => backRef.length
  }

  lazy val padRef      = (attrClean: String) => padS(maxRefs.max, attrClean)
  lazy val padRefNs    = (refNs: String) => padS(maxRefNs.max, refNs)
  lazy val padBackRefs = (backRef: String) => padS(maxBackRefs.max, backRef)

  def formatted(a: DefAttr) = {
    val attrSp = padAttrClean(a.attrClean)
    val typeSp = padType(a.tpe)
    val ref    = a.attrClean.capitalize + padRef(a.attrClean)
    val ref_   = a.attrClean.capitalize + "_" + padRef(a.attrClean)
    val refNsPad = (refNs: String) => refNs + "_" + padRefNs(refNs)
    (
      ns + "_" + a.attrClean + attrSp,
      ns + "_" + a.attrClean + "$" + attrSp,
      ns + "_" + a.attrClean + "K" + attrSp,
      ns + "_" + ref,
      ns + "_" + ref_,
      a.attr + padAttr(a.attr),
      a.attrClean + "$" + attrSp,
      a.attrClean + "_" + attrSp,
      a.attrClean + "K" + attrSp,
      a.attrClean + "K_" + attrSp,
      a.tpe + typeSp,
      "Option[" + a.tpe + typeSp + "]",
      a.baseTpe + padBaseType(a.baseTpe),
      ref,
      refNsPad
    )
  }

  def nn(i: Int) = if (i < 10) s"0$i" else s"$i"

  lazy val ns_ = ns + "_"

  lazy val ns_0_0_ = ns + "_" + in + "_" + out
  lazy val ns_0_1_ = ns + "_" + in + "_" + (out + 1)
  lazy val ns_1_0_ = ns + "_" + (in + 1) + "_" + out
  lazy val ns_1_1_ = ns + "_" + (in + 1) + "_" + (out + 1)

  lazy val ns_0_0 = ns_0_0_
  lazy val ns_0_1 = if (out < maxOut) ns_0_1_ else "D" + nn(in + out + 1)
  lazy val ns_1_0 = if (maxIn > 0 && in < maxIn) ns_1_0_ else "D" + nn(in + 1 + out)
  lazy val ns_1_1 = if (maxIn > 0 && in < maxIn && out < maxOut) ns_1_1_ else "D" + nn(in + 1 + out + 1)

  lazy val ns_0_0_L0 = ns_0_0_ + "_L" + level
  lazy val ns_0_1_L0 = if (out < maxOut) ns_0_1_ + "_L" + level else "D" + nn(in + out + 1)
  lazy val ns_1_0_L0 = if (maxIn > 0 && in < maxIn) ns_1_0_ + "_L" + level else "D" + nn(in + 1 + out)
  lazy val ns_1_1_L0 = if (maxIn > 0 && in < maxIn && out < maxOut) ns_1_1_ + "_L" + level else "D" + nn(in + 1 + out + 1)

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

  // Current and next+
  lazy val `, I1`      = if (in == 0) "" else ", " + (1 to in).map("I" + _).mkString(", ")
  lazy val `, I1+`     = if (in == 0) "" else ", " + (1 to (in + 1)).map("I" + _).mkString(", ")
  lazy val `, A`       = if (out == 0) "" else ", " + (65 until (65 + out)).map(_.toChar).mkString(", ")
  lazy val `, A+`      = if (out == 0) "" else ", " + (65 until (65 + out + 1)).map(_.toChar).mkString(", ")
  lazy val `, I1, A`   = `, I1` + `, A`
  lazy val `, I1+, A`  = `, I1+` + `, A`
  lazy val `, I1, A+`  = `, I1` + `, A+`
  lazy val `, I1+, A+` = `, I1+` + `, A+`

  lazy val D01 = "D" + nn(in + out + 1)
  lazy val D00 = "D" + nn(in + out)
}
