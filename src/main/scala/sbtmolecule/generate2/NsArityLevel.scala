package sbtmolecule.generate2

import sbtmolecule.Ast._
import scala.collection.mutable

case class NsArityLevel(
  model: Model,
  namespace: Namespace,
  in: Int,
  out: Int,
  level: Int,
  isGeneric: Boolean
) extends Spacing(model, namespace, in, out, level) {

  def getRef(r: Ref) = {
    val Ref(_, refAttrClean, _, clazz2, _, baseType, refNs, opts, bi, revRef, _)                                            = r
    val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(r)

    val biDirectionals = r match {
      case Ref(_, _, _, _, _, _, refNs, _, bi, revRef, _) =>
        bi match {
          case Some("BiSelfRef_")     => Seq(s"BiSelfRef_")
          case Some("BiOtherRef_")    => Seq(s"BiOtherRef_ [${refNs}_$revRef]")
          case Some("BiEdgePropRef_") => Seq(s"BiEdgePropRef_")
          case Some("BiEdgeRef_")     => Seq(s"BiEdgeRef_  [${refNs}_$revRef]")
          case Some("BiTargetRef_")   => Seq(s"BiTargetRef_[${refNs}_$revRef]")
          case _                      => Nil
        }

      case _ => Nil
    }

    val cls          = if (clazz2 == "OneRef") "OneRef " else "ManyRef"
    val refNs_       = refNsPad(refNs)
    val refNs_0_0_L1 = refNs + "_" + in + "_" + out + "_L" + (level + 1) + padRefNs(refNs)
    val refNsDef     = refNs_0_0_L1 + "[" + `o0, p0` + s", $ns_ref_, Nothing" + `, I1, A` + "]"
    val refNsClean   = ns + "_" + r.attrClean.capitalize + "_"
    val nestedDef    = if (clazz2 == "ManyRef" && out < maxOut) {
      val nsTypes = (in to 3).map {
        case i if i >= maxIn => "Nothing"
        case i               => refNs + "_" + i + "_" + (out + 1)
      }.mkString(", ")
      Seq(s"Nested_${in}_${nn(out)}[$refNsClean, " + `p0 with o1[p1]` + s", $nsTypes" + `, I1, A` + "]")
    } else {
      Nil
    }

    val extras    = biDirectionals ++ nestedDef
    val extrasStr = if (extras.isEmpty) "" else extras.mkString(" with ", " with ", "")

    s"final def $ref: $cls[$ns_[p0], $refNs_[p0]] with $refNsDef$extrasStr = ??? "
  }

  def getBackRef(br: BackRef) = {
    val backRefNs         = br.backRefNs
    val sp                = padBackRefs(backRefNs)
    val backRefNsPrefixed = "_" + backRefNs + sp
    val backRef_0_0_L0    = backRefNs + "_" + in + "_" + out + "_L" + (level - 1) + sp
    val concatLast        = level match {
      case 1 => "o0, p0 with o1[p1]"
      case 2 => "o0, p0, o1, p1 with o2[p2]"
      case 3 => "o0, p0, o1, p1, o2, p2 with o3[p3]"
      case 4 => "o0, p0, o1, p1, o2, p2, o3, p3 with o4[p4]"
      case 5 => "o0, p0, o1, p1, o2, p2, o3, p3, o4, p4 with o5[p5]"
      case 6 => "o0, p0, o1, p1, o2, p2, o3, p3, o4, p4, o5, p5 with o6[p6]"
      case 7 => "o0, p0, o1, p1, o2, p2, o3, p3, o4, p4, o5, p5, o6, p6 with o7[p7]"
    }
    s"final def $backRefNsPrefixed: $backRef_0_0_L0[$concatLast${`, I1, A`}] = ???"
  }


  def indexedFirst(opts: Seq[Optional]): Seq[String] = {
    val classes = opts.filter(_.clazz.nonEmpty).map(_.clazz)
    if (classes.contains("Indexed"))
      "Indexed" +: classes.filterNot(_ == "Indexed")
    else
      classes
  }

  def nsData(
    a: DefAttr,
    ns_attr: String,
    ns_attrO: String,
    ns_attrK: String,
    tpe: String,
    tpO: String,
    baseTpe: String,
  ) = {
    val (ns_0_0_L0_, ns_0_1_L0_, ns_1_0_L0_, ns_1_1_L0_) = if (domain == "Datom")
      (Ns_0_0, Ns_0_1, Ns_1_0, Ns_1_1)
    else
      (ns_0_0_L0, ns_0_1_L0, ns_1_0_L0, ns_1_1_L0)
    (
      a.clazz + padClass(a.clazz),
      a.clazz + "$" + padClass(a.clazz),

      // Stay, mandatory
      s"$ns_0_1_L0_[" + `o0, p0` + s" with $ns_attr" + `, I1, A` + s", $tpe]",

      // Stay, optional
      s"$ns_0_1_L0_[" + `o0, p0` + s" with $ns_attrO" + `, I1, A` + s", $tpO]",

      // Stay, mapK
      s"$ns_0_1_L0_[" + `o0, p0` + s" with $ns_attrK" + `, I1, A` + s", $baseTpe]",

      // Stay, tacit
      s"$ns_0_0_L0_[" + `o0, p0` + `, I1, A` + "]",

      // Next, mandatory
      if (maxIn > 0 && in < maxIn && out < maxOut)
        s"$ns_1_1_L0_[" + `o0, p0` + s" with $ns_attr" + `, I1` + s", $tpe" + `, A` + s", $tpe]"
      else {
        "Nothing"
      },

      // Next, optional
      if (maxIn > 0 && in < maxIn && out < maxOut)
        s"$ns_1_1_L0_[" + `o0, p0` + s" with $ns_attrO" + `, I1` + s", $tpO" + `, A` + s", $tpO]"
      else
        "Nothing",

      // Next, mapK
      if (maxIn > 0 && in < maxIn && out < maxOut)
        s"$ns_1_1_L0_[" + `o0, p0` + s" with $ns_attrK" + `, I1` + s", $baseTpe" + `, A` + s", $baseTpe]"
      else
        "Nothing",

      // Next, tacit
      if (maxIn > 0 && in < maxIn)
        s"$ns_1_0_L0_[" + `o0, p0` + s" with $ns_attr" + `, I1` + s", $tpe" + `, A` + s"]"
      else
        "Nothing",

      // Next, tacit mapK
      if (maxIn > 0 && in < maxIn)
        s"$ns_1_0_L0_[" + `o0, p0` + s" with $ns_attrK" + `, I1` + s", $baseTpe" + `, A` + s"]"
      else
        "Nothing"
    )
  }

  val val_Attr   = mutable.MutableList.empty[String]
  val valAttr$   = mutable.MutableList.empty[String]
  val valAttr_   = mutable.MutableList.empty[String]
  val val_AttrK  = mutable.MutableList.empty[String]
  val valAttrK_  = mutable.MutableList.empty[String]
  val defRef     = mutable.MutableList.empty[String]
  val defBackRef = mutable.MutableList.empty[String]

  def getExtraLamdas(a: DefAttr, bi: Option[String]) = {
    val classes            = a.options.filter(_.clazz.nonEmpty).map(_.clazz)
    val indexed            = if (classes.contains("Indexed")) Seq("Indexed") else Nil
    val optsWithoutIndexed = classes.filterNot(_ == "Indexed")
    def render(ns0: String, ns1: String, opts: Seq[String]) = {
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
          case "Fulltext" => s"Fulltext[$ns0, $ns1]"
          case other      => other
        }
        (indexed ++ typedOpts ++ biOptions).mkString(" with ", " with ", "")
      }
    }
    (
      (ns0: String, ns1: String) => render(ns0, ns1, optsWithoutIndexed),
      (ns0: String, ns1: String) => render(ns0, ns1, "MapAttrK" +: optsWithoutIndexed)
    )
  }

  attrs.foreach {
    case a: Val =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      val (clsMan, clsOpt, ns0man, ns0opt, ns0map, ns0tac, ns1man, ns1opt, ns1map, ns1tac, ns1tacK)                           = nsData(a, ns_attr, ns_attrO, ns_attrK, tpe, tpO, baseTpe)
      val (opts, optsK)                                                                                                       = getExtraLamdas(a, a.bi)


      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan [$ns0man, $ns1man] with $ns0man${opts(ns0man, ns1man)} = ???"
        if (isGeneric) {
          if (domain == "Schema" && Seq("doc", "index", "unique", "fulltext", "isComponent", "noHistory").contains(a.attr))
            valAttr$ += s"final lazy val $attrO : $clsOpt[$ns0opt] with $ns0opt${opts(ns0opt, ns1opt)} = ???"
        } else {
          valAttr$ += s"final lazy val $attrO : $clsOpt[$ns0opt] with $ns0opt${opts(ns0opt, ns1opt)} = ???"
        }
      }

      if (isGeneric) {
        if (domain == "Datom" || domain == "Schema")
          valAttr_ += s"final lazy val $attr_ : $clsMan [$ns0tac, $ns1tac] with $ns0tac${opts(ns0tac, ns1tac)} = ???"
      } else {
        valAttr_ += s"final lazy val $attr_ : $clsMan [$ns0tac, $ns1tac] with $ns0tac${opts(ns0tac, ns1tac)} = ???"
      }

      if (a.clazz.startsWith("Map")) {
        val oneCls = "One" + baseTpe
        val_AttrK += s"final lazy val $attrK  : String => $oneCls[$ns0map, $ns1map] with $ns0map${optsK(ns0map, ns1map)} = ???"
        valAttrK_ += s"final lazy val $attrK_ : String => $oneCls[$ns0tac, $ns1tacK] with $ns0tac${optsK(ns0tac, ns1tacK)} = ???"
      }

    case a: Ref =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      val (clsMan, clsOpt, ns0man, ns0opt, ns0map, ns0tac, ns1man, ns1opt, ns1map, ns1tac, ns1tacK)                           = nsData(a, ns_attr, ns_attrO, ns_attrK, tpe, tpO, baseTpe)
      val (opts, optsK)                                                                                                       = getExtraLamdas(a, a.bi)

      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan [$ns0man, $ns1man] with $ns0man${opts(ns0man, ns1man)} = ???"
        valAttr$ += s"final lazy val $attrO : $clsOpt[$ns0opt] with $ns0opt${opts(ns0opt, ns1opt)} = ???"
      }
      valAttr_ += s"final lazy val $attr_ : $clsMan [$ns0tac, $ns1tac] with $ns0tac${opts(ns0tac, ns1tac)} = ???"

      if (level < maxLevel)
        defRef += getRef(a)

    case a: Enum =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      val (clsMan, clsOpt, ns0man, ns0opt, ns0map, ns0tac, ns1man, ns1opt, ns1map, ns1tac, ns1tacK)                           = nsData(a, ns_attr, ns_attrO, ns_attrK, tpe, tpO, baseTpe)
      val (opts, optsK)                                                                                                       = getExtraLamdas(a, a.bi)

      //      val enumValues  = s"private lazy val ${a.enums.mkString(", ")} = EnumValue"
      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan [$ns0man, $ns1man] with $ns0man${opts(ns0man, ns1man)} = ???"
        valAttr$ += s"final lazy val $attrO : $clsOpt[$ns0opt] with $ns0opt${opts(ns0opt, ns1opt)} = ???"
      }
      valAttr_ += s"final lazy val $attr_ : $clsMan [$ns0tac, $ns1tac] with $ns0tac${opts(ns0tac, ns1tac)} = ???"

    case br: BackRef =>
      if (level > 0)
        defBackRef += getBackRef(br)
  }

  val selfJoinDef = s"final def Self: $ns_0_0_L0[${`o0, p0`}${`, I1, A`}] with SelfJoin = ???"

  val (datomNss, datomNs) = if (domain == "Datom") {
    (s", $Ns_0_0_, $Ns_0_1_, $Ns_1_0_, $Ns_1_1_", "")
  } else {
    val ns_0_0_L0 = ns_0_0_ + "_L" + level
    val ns_0_1_L0 = if (out < maxOut) ns_0_1_ + "_L" + level else "Nothing"
    val ns_1_0_L0 = if (maxIn > 0 && in < maxIn) ns_1_0_ + "_L" + level else "Nothing"
    val ns_1_1_L0 = if (maxIn > 0 && in < maxIn && out < maxOut) ns_1_1_ + "_L" + level else "Nothing"
    ("", s" with Datom_${in}_${out}_L$level[${`o0, p0`}${`, I1, A`}, $ns_0_0_L0, $ns_0_1_L0, $ns_1_0_L0, $ns_1_1_L0]")
  }

  val attrsNext: Seq[String] = if (out < maxOut) {
    val_Attr ++ Seq("") ++
      (if (val_AttrK.nonEmpty) val_AttrK ++ Seq("") else Nil) ++
      valAttr$ ++ Seq("")
  } else Nil
  val attrKStay: Seq[String] = if (valAttrK_.nonEmpty) Seq("") ++ valAttrK_ else Nil
  val refs     : Seq[String] = if (defRef.nonEmpty) Seq("") ++ defRef else Nil
  val backRefs : Seq[String] = if (defBackRef.nonEmpty) Seq("") ++ defBackRef else Nil
  val selfJoin : Seq[String] = if (out > 0 && !isGeneric) Seq("", selfJoinDef) else Nil

  val body = (attrsNext ++ valAttr_ ++ attrKStay ++ refs ++ backRefs ++ selfJoin).mkString("\n  ").trim

  def get: String =
    s"""trait $ns_0_0_L0[${`o0[_], p0`}${`, I1, A`}$datomNss] extends $ns_0_0[${`o0, p0 with o1[p1]`}${`, I1, A`}]$datomNs {
       |
       |  $body
       |}
       |""".stripMargin
}
