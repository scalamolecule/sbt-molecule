package sbtmolecule.generate

import sbtmolecule.Ast.{BackRef, DefAttr, Enum, Model, Namespace, Optional, Ref, Val, firstLow, padS}

class Helpers(
  model: Model,
  namespace: Namespace,
  in: Int = 0,
  out: Int = 0,
  level: Int = 0,
  genericPkg: String = ""
) {
  lazy val domain   = model.domain
  lazy val maxIn    = model.maxIn
  lazy val maxOut   = model.maxOut
  lazy val maxLevel = maxOut.min(7)
  lazy val ns       = namespace.ns
  lazy val attrs    = namespace.attrs

  lazy val isGeneric = genericPkg.nonEmpty
  lazy val isDatom   = domain == "Datom"
  lazy val isSchema  = domain == "Schema"

  lazy val padAttr      = (s: String) => padS(attrs.map(_.attr).filterNot(_.startsWith("_")).map(_.length + 1).max, s)
  lazy val padAttrClean = (s: String) => padS(attrs.map(_.attrClean).filterNot(_.startsWith("_")).map(_.length).max, s)
  lazy val padType      = (s: String) => padS(attrs.map(_.tpe).map(_.length).max, s)
  lazy val padBaseType  = (s: String) => padS(attrs.map(_.baseTpe).map(_.length).max, s)
  lazy val padClass     = (s: String) => padS(attrs.map(_.clazz).filterNot(_.startsWith("Back")).map(_.length).max, s)

  lazy val maxRefs    : Seq[Int] = 0 +: attrs.collect {
    case Ref(_, attrClean, _, _, _, _, _, _, _, _, _) => attrClean.length
    case BackRef(_, attrClean, _, _, _, _, _, _, _)   => attrClean.length
  }
  lazy val maxRefNs   : Seq[Int] = 0 +: attrs.collect {
    case Ref(_, _, _, _, _, _, refNs, _, _, _, _) => refNs.length
  }
  lazy val maxBackRefs: Seq[Int] = 0 +: attrs.collect {
    case BackRef(_, _, _, _, _, _, backRef, _, _) => backRef.length
  }

  lazy val padRef      = (attrClean: String) => padS(maxRefs.max, attrClean)
  lazy val padRefNs    = (refNs: String) => padS(maxRefNs.max, refNs)
  lazy val padBackRefs = (backRef: String) => padS(maxBackRefs.max, backRef)

  def formatted(a: DefAttr) = {
    val attrSp   = padAttrClean(a.attrClean)
    val typeSp   = padType(a.tpe)
    val ref      = a.attrClean.capitalize + padRef(a.attrClean)
    val ref_     = a.attrClean.capitalize + "_" + padRef(a.attrClean)
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
  lazy val ns_0_1 = if (out < maxOut) ns_0_1_ else "Nothing"
  lazy val ns_1_0 = if (maxIn > 0 && in < maxIn) ns_1_0_ else "Nothing"
  lazy val ns_1_1 = if (maxIn > 0 && in < maxIn && out < maxOut) ns_1_1_ else "Nothing"

  lazy val ns_0_0_L0 = ns_0_0_ + "_L" + level
  lazy val ns_0_1_L0 = if (out < maxOut) ns_0_1_ + "_L" + level else "Nothing"
  lazy val ns_1_0_L0 = if (maxIn > 0 && in < maxIn) ns_1_0_ + "_L" + level else "Nothing"
  lazy val ns_1_1_L0 = if (maxIn > 0 && in < maxIn && out < maxOut) ns_1_1_ + "_L" + level else "Nothing"

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
  lazy val `, I1`    = if (in == 0) "" else ", " + (1 to in).map("I" + _).mkString(", ")
  lazy val `, A`     = if (out == 0) "" else ", " + (65 until (65 + out)).map(_.toChar).mkString(", ")
  lazy val `, I1, A` = `, I1` + `, A`


  def nsData(
    a: DefAttr
  ) = {
    (
      a.clazz + padClass(a.clazz),
      a.clazz + "$" + padClass(a.clazz)
    )
  }
  def getExtras(a: DefAttr, bi: Option[String]) = {
    val classes            = a.options.filter(_.clazz.nonEmpty).map(_.clazz)
    val indexed            = if (classes.contains("Indexed")) Seq("Indexed") else Nil
    val optsWithoutIndexed = classes.filterNot(_ == "Indexed")
    def render(opts: Seq[String]) = {
      val biOptions = a match {
        case Ref(_, _, _, _, _, _, refNs, _, bi, revRef, _) =>
          bi match {
            case Some("BiSelfRef_")     => Seq(s"BiSelfRefAttr_")
            case Some("BiOtherRef_")    => Seq(s"BiOtherRefAttr_[${refNs}_$revRef]")
            case Some("BiEdgeRef_")     => Seq(s"BiEdgeRefAttr_[${refNs}_$revRef]")
            case Some("BiEdgePropAttr") => Seq(s"BiEdgePropAttr_")
            case Some("BiEdgePropRef_") => Seq(s"BiEdgePropRefAttr_")
            case Some("BiTargetRef_")   => Seq(s"BiTargetRefAttr_[${refNs}_$revRef]")
            case _                      => Nil
          }
        case _                                              => bi.toList
      }

      if ((indexed ++ opts ++ biOptions).isEmpty) {
        ""
      } else {
        val typedOpts = opts.map {
          case "Fulltext" => s"Fulltext[Stay, Next]"
          case other      => other
        }
        (indexed ++ typedOpts ++ biOptions).mkString(" with ", " with ", "")
      }
    }
    (
      render(optsWithoutIndexed),
      render("MapAttrK" +: optsWithoutIndexed)
    )
  }

  // Generic Datoms generation

  lazy val Ns_0_0 = "Ns_" + in + "_" + out
  lazy val Ns_0_1 = "Ns_" + in + "_" + (out + 1)
  lazy val Ns_1_0 = "Ns_" + (in + 1) + "_" + out
  lazy val Ns_1_1 = "Ns_" + (in + 1) + "_" + (out + 1)

  //  lazy val p_0 = (Seq(`o0, p0`) ++ List.fill(in + out)("_")).mkString(",")
  //  lazy val p_1 = (Seq(`o0, p0`) ++ List.fill(in + out + 1)("_")).mkString(",")
  //  lazy val p_2 = (Seq(`o0, p0`) ++ List.fill(in + out + 2)("_")).mkString(",")


  lazy val o_  = (0 to level).map(l => s"o$l[_],_")
  //  lazy val o_ = (0 to level).map(l => s"o[_],_")
  lazy val p_0 = (o_ ++ List.fill(in + out)("_")).mkString(",")
  lazy val p_1 = (o_ ++ List.fill(in + out + 1)("_")).mkString(",")
  lazy val p_2 = (o_ ++ List.fill(in + out + 2)("_")).mkString(",")

  lazy val Ns_0_0_ = Ns_0_0 + "[" + p_0 + "]"
  lazy val Ns_0_1_ = Ns_0_1 + "[" + p_1 + "]"
  lazy val Ns_1_0_ = Ns_1_0 + "[" + p_1 + "]"
  lazy val Ns_1_1_ = Ns_1_1 + "[" + p_2 + "]"
}