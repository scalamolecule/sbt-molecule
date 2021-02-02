package sbtmolecule.generate

import sbtmolecule.Ast._
import scala.collection.mutable

case class NsArityLevel(
  model: Model,
  namespace: Namespace,
  in: Int,
  out: Int,
  level: Int
) extends Spacing(model, namespace, in, out, level) {


  //  val InTypes : Seq[String] = (0 until in) map (n => "I" + (n + 1))
  //  val OutTypes: Seq[String] = (0 until out) map (n => (n + 'A').toChar.toString)
  //  val maxTpe  : Int         = if (attrs.nonEmpty) {
  //    val tps = attrs.filterNot(_.attr.startsWith("_")).map(_.tpe.length)
  //    if (tps.nonEmpty) tps.max else 0
  //  } else 0
  //  val maxAttr : Int         = if (attrs.nonEmpty) {
  //    val tps = attrs.map(_.attr).filterNot(_.startsWith("_")).map(_.length)
  //    if (tps.nonEmpty) tps.max else 0
  //  } else 0
  //  val maxTpeK : Int         = (0 +: attrs.filter(_.clazz.startsWith("Map")).map(a => a.baseTpe.length)).max
  //
  //
  //  def d(n: Int) = if (n < 10) "D0" + n else "D" + n
  //  def pp(n: Int) = {
  //    val prefix = if (n < 10) "D0" else "D"
  //    prefix + s"$n[" + (1 to n).map(j => "_").mkString(",") + "]"
  //  }
  //  def p(n: Int) = if (n < 10) "0" + n else n
  //
  //  val nextStay = {
  //    val ins  = InTypes.mkString(", ")
  //    val outs = OutTypes.mkString(", ")
  //    (in, out) match {
  //      case (0, 0) if maxIn == 0 => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_1[Prop, Tpe], D03[_,_,_]] with ${ns}_1[Prop, Tpe]",
  //        s"private type Stay[Attr[_, _]           ] = Attr[${ns}_0, D02[_,_]] with ${ns}_0"
  //      )
  //
  //      case (0, `maxOut`) if maxIn == 0 => Seq(
  //        s"private type Stay[Attr[_, _], Tpe] = Attr[${ns}_$maxOut[Obj, $outs], ${pp(maxOut + 2)}] with ${ns}_$maxOut[Obj, $outs]"
  //      )
  //
  //      case (0, o) if maxIn == 0 => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_${o + 1}[Obj, $outs, Tpe], ${pp(o + 2)}] with ${ns}_${o + 1}[Obj with Prop, $outs, Tpe]",
  //        s"private type Stay[Attr[_, _]           ] = Attr[${ns}_$o[Obj, $outs], ${pp(o + 1)}] with ${ns}_$o[Obj, $outs]"
  //      )
  //
  //      case (0, 0) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_1[Prop, Tpe], ${ns}_In_1_1[Prop, Tpe, Tpe]] with ${ns}_1[Prop, Tpe]",
  //        s"private type Stay[Attr[_, _], Prop, Tpe] = Attr[${ns}_0, ${ns}_In_1_0[Prop, Tpe]] with ${ns}_0"
  //      )
  //
  //      case (0, `maxOut`) => Seq(
  //        s"private type Stay[Attr[_, _], Tpe] = Attr[${ns}_$maxOut[Obj, $outs], ${ns}_In_1_$maxOut[Obj, Tpe, $outs]] with ${ns}_$maxOut[Obj, $outs]"
  //      )
  //
  //      case (0, o) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_${o + 1}[Obj, $outs, Tpe], ${ns}_In_1_${o + 1}[Obj, Tpe, $outs, Tpe]] with ${ns}_${o + 1}[Obj with Prop, $outs, Tpe]",
  //        s"private type Stay[Attr[_, _],       Tpe] = Attr[${ns}_$o[Obj, $outs], ${ns}_In_1_$o[Obj, Tpe, $outs]] with ${ns}_$o[Obj, $outs]"
  //      )
  //
  //      case (`maxIn`, 0) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_In_${maxIn}_1[Obj, $ins, Tpe], ${pp(maxIn + 2)}] with ${ns}_In_${maxIn}_1[Obj with Prop, $ins, Tpe]",
  //        s"private type Stay[Attr[_, _]           ] = Attr[${ns}_In_${maxIn}_0[Obj, $ins], ${pp(maxIn + 1)}] with ${ns}_In_${maxIn}_0[Obj, $ins]"
  //      )
  //
  //      case (`maxIn`, `maxOut`) => Seq(
  //        s"private type Stay[Attr[_, _]] = Attr[${ns}_In_${maxIn}_$maxOut[Obj, $ins, $outs], ${pp(maxIn + maxOut + 1)}] with ${ns}_In_${maxIn}_$maxOut[Obj, $ins, $outs]"
  //      )
  //
  //      case (`maxIn`, o) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_In_${maxIn}_${o + 1}[Obj, $ins, $outs, Tpe], ${pp(maxIn + o + 2)}] with ${ns}_In_${maxIn}_${o + 1}[Obj with Prop, $ins, $outs, Tpe]",
  //        s"private type Stay[Attr[_, _]           ] = Attr[${ns}_In_${maxIn}_$o[Obj, $ins, $outs], ${pp(maxIn + o + 1)}] with ${ns}_In_${maxIn}_$o[Obj, $ins, $outs]"
  //      )
  //
  //      case (i, 0) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_In_${i}_1[Obj, $ins, Tpe], ${ns}_In_${i + 1}_1[Obj, $ins, Tpe, Tpe]] with ${ns}_In_${i}_1[Obj with Prop, $ins, Tpe]",
  //        s"private type Stay[Attr[_, _],       Tpe] = Attr[${ns}_In_${i}_0[Obj, $ins], ${ns}_In_${i + 1}_0[Obj, $ins, Tpe]] with ${ns}_In_${i}_0[Obj, $ins]"
  //      )
  //
  //      case (i, `maxOut`) => Seq(
  //        s"private type Stay[Attr[_, _], Tpe] = Attr[${ns}_In_${i}_$maxOut[Obj, $ins, $outs], ${ns}_In_${i + 1}_$maxOut[Obj, $ins, Tpe, $outs]] with ${ns}_In_${i}_$maxOut[Obj, $ins, $outs]"
  //      )
  //
  //      case (i, o) => Seq(
  //        s"private type Next[Attr[_, _], Prop, Tpe] = Attr[${ns}_In_${i}_${o + 1}[Obj, $ins, $outs, Tpe], ${ns}_In_${i + 1}_${o + 1}[Obj, $ins, Tpe, $outs, Tpe]] with ${ns}_In_${i}_${o + 1}[Obj with Prop, $ins, $outs, Tpe]",
  //        s"private type Stay[Attr[_, _],       Tpe] = Attr[${ns}_In_${i}_$o[Obj, $ins, $outs], ${ns}_In_${i + 1}_$o[Obj, $ins, Tpe, $outs]] with ${ns}_In_${i}_$o[Obj, $ins, $outs]"
  //      )
  //    }
  //  }
  //
  //  //  val (attrVals: Seq[String], attrVals_ : Seq[String]) = attrs.flatMap {
  //  //    case BackRef(_, _, _, _, _, _, _, _, _) => None
  //  //    case a                                  => {
  //  //      val (attr, attrClean, tpe) = (a.attr, a.attrClean, a.tpe)
  //  //      val p1                     = padS(maxAttr, attr)
  //  //      val p2                     = padS(maxAttr, attrClean)
  //  //      val p3                     = padS(maxTpe, tpe)
  //  //      val next                   = s"final lazy val $attr  $p1: Next[$attr$p1 , ${ns}_$attrClean$p2 , $tpe$p3] = ???"
  //  //      val stay                   = (in, out) match {
  //  //        case (0, 0) if maxIn == 0        => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1] = ???"
  //  //        case (0, `maxOut`) if maxIn == 0 => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //        case (0, o) if maxIn == 0        => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1] = ???"
  //  //        case (0, 0)                      => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, ${ns}_$attrClean$p1, $tpe$p3] = ???"
  //  //        case (0, `maxOut`)               => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //        case (0, o)                      => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //        case (`maxIn`, 0)                => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1] = ???"
  //  //        case (`maxIn`, `maxOut`)         => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1] = ???"
  //  //        case (`maxIn`, o)                => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1] = ???"
  //  //        case (i, 0)                      => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //        case (i, `maxOut`)               => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //        case (i, o)                      => s"final lazy val ${attrClean}_ $p2: Stay[$attr$p1, $tpe$p3] = ???"
  //  //      }
  //  //      Some(next, stay)
  //  //    }
  //  //  }.unzip
  //
  //
  //  val (attrValsK: Seq[String], attrValsK_ : Seq[String]) = attrs.flatMap {
  //    case BackRef(_, _, _, _, _, _, _, _, _) => None
  //    case a                                  => {
  //      val (attrClean, baseTpe) = (a.attrClean, a.baseTpe)
  //      val p1                   = padS(maxAttr, attrClean)
  //      val p2                   = padS(maxTpeK, baseTpe)
  //      a match {
  //        case _ if a.clazz.startsWith("Map") =>
  //          val next = s"final lazy val ${attrClean}K $p1: String => Next[${attrClean}K$p1, ${ns}_${attrClean}K$p1, $baseTpe$p2] = ???"
  //          val stay = (in, out) match {
  //            case (0, 0) if maxIn == 0        => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1] = ???"
  //            case (0, `maxOut`) if maxIn == 0 => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (0, o) if maxIn == 0        => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1] = ???"
  //            case (0, 0)                      => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, ${ns}_${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (0, `maxOut`)               => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (0, o)                      => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (`maxIn`, 0)                => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1] = ???"
  //            case (`maxIn`, `maxOut`)         => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1] = ???"
  //            case (`maxIn`, o)                => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1] = ???"
  //            case (i, 0)                      => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (i, `maxOut`)               => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //            case (i, o)                      => s"final lazy val ${attrClean}K_$p1 : String => Stay[${attrClean}K$p1, $baseTpe$p2] = ???"
  //          }
  //          Some(next, stay)
  //        case _                              => None
  //      }
  //    }
  //  }.unzip match {
  //    case (Nil, Nil) => (Nil, Nil)
  //    case (s1, s2)   => (Seq("") ++ s1, Seq("") ++ s2) // Add empty line to separate group of K-attributes
  //  }
  //
  //  val attrValsOpt: Seq[String] = attrs.flatMap {
  //    case BackRef(_, _, _, _, _, _, _, _, _) => None
  //    case a                                  => {
  //      val (attrClean, tpe) = (a.attrClean, a.tpe)
  //      val p1               = padS(maxAttr, attrClean)
  //      val p2               = padS(maxTpe, tpe)
  //      Some(s"final lazy val $attrClean$$ $p1: Next[$attrClean$$$p1, ${ns}_$attrClean$$$p1, Option[$tpe]$p2] = ???")
  //    }
  //  }
  //
  //  val maxRefNs: Seq[Int] = attrs.collect {
  //    case Ref(_, _, _, _, _, _, refNs, _, _, _, _) => refNs.length
  //    case BackRef(_, _, _, _, _, _, backRef, _, _) => backRef.length
  //  }
  //
  //  val maxRefs: Seq[Int] = attrs.collect {
  //    case Ref(_, attrClean, _, _, _, _, _, _, _, _, _) => attrClean.length
  //    case BackRef(_, attrClean, _, _, _, _, _, _, _)   => attrClean.length
  //  }
  //
  //  val biDirectionals   : Map[String, String] = attrs.flatMap {
  //    case Ref(attr, _, _, _, _, _, refNs, _, Some("BiSelfRef_"), _, _)        => Some(attr -> s" with BiSelfRef_")
  //    case Ref(attr, _, _, _, _, _, refNs, _, Some("BiOtherRef_"), revRef, _)  => Some(attr -> s" with BiOtherRef_[$domain.$refNs.$revRef[NS, NS]]")
  //    case Ref(attr, _, _, _, _, _, refNs, _, Some("BiEdgePropRef_"), _, _)    => Some(attr -> s" with BiEdgePropRef_")
  //    case Ref(attr, _, _, _, _, _, refNs, _, Some("BiEdgeRef_"), revRef, _)   => Some(attr -> s" with BiEdgeRef_[$domain.$refNs.$revRef[NS, NS]]")
  //    case Ref(attr, _, _, _, _, _, refNs, _, Some("BiTargetRef_"), revRef, _) => Some(attr -> s" with BiTargetRef_[$domain.$refNs.$revRef[NS, NS]]")
  //    case _                                                                   => None
  //  }.toMap
  //  val maxBiDirectionals: Iterable[Int]       = biDirectionals.values.map(_.length)
  //
  //
  //  val refCode: Seq[String] = attrs.foldLeft(Seq("")) {
  //    case (acc, Ref(attr, attrClean, _, clazz2, _, baseType, refNs, _, _, _, _)) => {
  //      val p1 = padS(maxRefs.max, attrClean)
  //      val p2 = padS("ManyRef".length, clazz2)
  //      val p3 = padS(maxRefNs.max, refNs)
  //
  //      val biDirectional = if (biDirectionals.nonEmpty && biDirectionals.contains(attr))
  //        biDirectionals(attr) + padS(maxBiDirectionals.max, biDirectionals(attr))
  //      else ""
  //
  //      val ref = (in, out, maxIn) match {
  //        case (0, 0, _) if baseType.isEmpty                => s"${refNs}_0$p3$biDirectional"
  //        case (0, 0, 3)                                    => s"${refNs}_0$p3$biDirectional with Nested00[Nothing, ${refNs}_1$p3, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3]"
  //        case (0, 0, 2)                                    => s"${refNs}_0$p3$biDirectional with Nested00[Nothing, ${refNs}_1$p3, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, D05]"
  //        case (0, 0, 1)                                    => s"${refNs}_0$p3$biDirectional with Nested00[Nothing, ${refNs}_1$p3, ${refNs}_In_1_1$p3, D04, D05]"
  //        case (0, 0, 0)                                    => s"${refNs}_0$p3$biDirectional with Nested00[Nothing, ${refNs}_1$p3, D03, D04, D05]"
  //        case (0, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_$o$p3[Obj, ${OutTypes mkString ", "}]$biDirectional"
  //        case (0, o, 3)                                    => s"${refNs}_$o$p3[Obj, ${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[Obj, ${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${OutTypes mkString ", "}]"
  //        case (0, o, 2)                                    => s"${refNs}_$o$p3[Obj, ${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[Obj, ${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${d(o + 5)}, ${OutTypes mkString ", "}]"
  //        case (0, o, 1)                                    => s"${refNs}_$o$p3[Obj, ${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[Obj, ${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, ${d(o + 4)}, ${d(o + 5)}, ${OutTypes mkString ", "}]"
  //        case (0, o, 0)                                    => s"${refNs}_$o$p3[Obj, ${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[Obj, ${refNs}_${o + 1}$p3, ${d(o + 3)}, ${d(o + 4)}, ${d(o + 5)}, ${OutTypes mkString ", "}]"
  //
  //        case (1, 0, _) if baseType.isEmpty                => s"${refNs}_In_1_0$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (1, 0, 3)                                    => s"${refNs}_In_1_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[Obj, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
  //        case (1, 0, 2)                                    => s"${refNs}_In_1_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[Obj, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, D05, ${InTypes mkString ", "}]"
  //        case (1, 0, 1)                                    => s"${refNs}_In_1_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[Obj, ${refNs}_In_1_1$p3, D04, D05, ${InTypes mkString ", "}]"
  //        case (1, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_1_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (1, o, 3)                                    => s"${refNs}_In_1_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[Obj, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
  //        case (1, o, 2)                                    => s"${refNs}_In_1_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[Obj, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${d(o + 5)}, ${(InTypes ++ OutTypes) mkString ", "}]"
  //        case (1, o, 1)                                    => s"${refNs}_In_1_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[Obj, ${refNs}_In_1_${o + 1}$p3, ${d(o + 4)}, ${d(o + 5)}, ${(InTypes ++ OutTypes) mkString ", "}]"
  //
  //        case (2, 0, _) if baseType.isEmpty                => s"${refNs}_In_2_0$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (2, 0, 3)                                    => s"${refNs}_In_2_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_2_00[Obj, ${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
  //        case (2, 0, 2)                                    => s"${refNs}_In_2_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_2_00[Obj, ${refNs}_In_2_1$p3, D05, ${InTypes mkString ", "}]"
  //        case (2, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_2_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (2, o, 3)                                    => s"${refNs}_In_2_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_2_${padI(o)}[Obj, ${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
  //        case (2, o, 2)                                    => s"${refNs}_In_2_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_2_${padI(o)}[Obj, ${refNs}_In_2_${o + 1}$p3, ${d(o + 5)}, ${(InTypes ++ OutTypes) mkString ", "}]"
  //
  //        case (3, 0, _) if baseType.isEmpty                => s"${refNs}_In_3_0$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (3, 0, 3)                                    => s"${refNs}_In_3_0$p3[Obj, ${InTypes mkString ", "}]$biDirectional with Nested_In_3_00[Obj, ${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
  //        case (3, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_3_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
  //        case (3, o, 3)                                    => s"${refNs}_In_3_$o$p3[Obj, ${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_3_${padI(o)}[Obj, ${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
  //      }
  //      acc :+ s"final def ${attrClean.capitalize} $p1: $clazz2$p2[${ns}_, ${refNs}_$p3] with $ref = ???"
  //    }
  //
  //    case (acc, BackRef(_, _, _, _, _, _, backRefNs, _, _)) =>
  //      val p1  = padS(maxRefs.max, backRefNs)
  //      val ref = (in, out) match {
  //        case (0, 0) => s"${backRefNs}_0$p1"
  //        case (0, o) => s"${backRefNs}_$o$p1[Obj, ${OutTypes mkString ", "}]"
  //        case (i, o) => s"${backRefNs}_In_${i}_$o$p1[Obj, ${(InTypes ++ OutTypes) mkString ", "}]"
  //      }
  //      acc :+ s"final def _$backRefNs$p1: $ref = ???"
  //
  //    case (acc, _) => acc
  //  }.distinct
  //
  //  (in, out) match {
  //    // First output trait
  //    case (0, 0) =>
  //      val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) (d(out + in + 2), d(out + in + 3)) else (s"${ns}_In_1_0", s"${ns}_In_1_1")
  //      s"""trait ${ns}_0 extends ${ns}_ with Out_00[${ns}_0, ${ns}_1, $thisIn, $nextIn] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |}
  //         """.stripMargin
  //
  //    // Last output trait
  //    case (0, o) if o == maxOut =>
  //      val thisIn = if (maxIn == 0 || in == maxIn) d(out + in + 2) else s"${ns}_In_1_$o"
  //      val types  = OutTypes mkString ", "
  //      s"""trait ${ns}_$o[Obj, $types] extends ${ns}_ with Out_${p(o)}[Obj, ${ns}_$o, ${d(out + in + 2)}, $thisIn, ${d(out + in + 3)}, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |}""".stripMargin
  //
  //    // Other output traits
  //    case (0, o) =>
  //      val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) (d(out + in + 2), d(out + in + 3)) else (s"${ns}_In_1_$o", s"${ns}_In_1_${o + 1}")
  //      val types            = OutTypes mkString ", "
  //      s"""trait ${ns}_$o[Obj, $types] extends ${ns}_ with Out_${p(o)}[Obj, ${ns}_$o, ${ns}_${o + 1}, $thisIn, $nextIn, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |
  //         |  final def Self: ${ns}_$o[Obj, $types] with SelfJoin = ???
  //         |}
  //         """.stripMargin
  //
  //
  //    // First input trait
  //    case (i, 0) =>
  //      val s                = if (in > 1) "s" else ""
  //      val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) (d(out + in + 2), d(out + in + 3)) else (s"${ns}_In_${i + 1}_0", s"${ns}_In_${i + 1}_1")
  //      val types            = InTypes mkString ", "
  //      s"""trait ${ns}_In_${i}_0[Obj, $types] extends ${ns}_ with In_${i}_0[Obj, ${ns}_In_${i}_0, ${ns}_In_${i}_1, $thisIn, $nextIn, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |}
  //         """.stripMargin
  //
  //    // Last input trait
  //    case (i, o) if i <= maxIn && o == maxOut =>
  //      val thisIn = if (maxIn == 0 || i == maxIn) d(out + in + 2) else s"${ns}_In_${i + 1}_$o"
  //      val types  = (InTypes ++ OutTypes) mkString ", "
  //      s"""trait ${ns}_In_${i}_$o[Obj, $types] extends ${ns}_ with In_${i}_$o[Obj, ${ns}_In_${i}_$o, ${d(out + in + 2)}, $thisIn, ${d(out + in + 3)}, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |}""".stripMargin
  //
  //    // Max input traits
  //    case (i, o) if i == maxIn =>
  //      val types = (InTypes ++ OutTypes) mkString ", "
  //      s"""trait ${ns}_In_${i}_$o[Obj, $types] extends ${ns}_ with In_${i}_$o[Obj, ${ns}_In_${i}_$o, ${ns}_In_${i}_${o + 1}, ${d(out + in + 2)}, ${d(out + in + 3)}, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |}
  //         """.stripMargin
  //
  //    // Other input traits
  //    case (i, o) =>
  //      val (thisIn, nextIn) = if (i == maxIn) (d(out + in + 2), d(out + in + 3)) else (s"${ns}_In_${i + 1}_$o", s"${ns}_In_${i + 1}_${o + 1}")
  //      val types            = (InTypes ++ OutTypes) mkString ", "
  //      s"""trait ${ns}_In_${i}_$o[Obj, $types] extends ${ns}_ with In_${i}_$o[Obj, ${ns}_In_${i}_$o, ${ns}_In_${i}_${o + 1}, $thisIn, $nextIn, $types] {
  //         |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
  //         |
  //         |  final def Self : ${ns}_In_${i}_$o[Obj, $types] with SelfJoin = ???
  //         |}
  //         """.stripMargin
  //  }
  //
  //
  //  def attrClassesX(a: DefAttr) = {
  //
  //    a match {
  //      case Val(attr, attrClean, clazz, _, baseTpe, _, opts, bi, _, _) if clazz.startsWith("Map") =>
  //        val extensions0 = indexedFirst(opts) ++ bi.toList
  //        val extensions1 = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
  //        val extensions2 = " with " + (extensions0 :+ "MapAttrK").mkString(" with ")
  //
  //        (
  //          clazz + padClass(clazz)
  //          )
  //
  //
  //        Seq(
  //          s"final class $attr${padAttrClean(attr)} [Ns, In] extends $clazz${padClass(clazz)}[Ns, In]$extensions1",
  //          s"final class ${attrClean}K${padAttrClean(attrClean)}[Ns, In] extends One$baseTpe${padClass("One" + baseTpe)}[Ns, In]$extensions2"
  //        )
  //
  //      case Val(attr, _, clazz, _, _, _, opts, bi, _, _) =>
  //        val extensions0 = indexedFirst(opts) ++ bi.toList
  //        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
  //        Seq(s"final class $attr${padAttr(attr)}[Ns, In] extends $clazz${padClass(clazz)}[Ns, In]$extensions")
  //
  //      case Enum(attr, _, clazz, _, _, enums, opts, bi, _, _) =>
  //        val extensions0 = indexedFirst(opts) ++ bi.toList
  //        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
  //        val enumValues  = s"private lazy val ${enums.mkString(", ")} = EnumValue"
  //        Seq(s"""final class $attr${padAttr(attr)}[Ns, In] extends $clazz${padClass(clazz)}[Ns, In]$extensions { $enumValues }""")
  //
  //      case Ref(attr, _, clazz, _, _, _, revNs, opts, bi, revRef, _) =>
  //        val extensions0 = indexedFirst(opts) ++ (bi match {
  //          case Some("BiSelfRef_")     => Seq(s"BiSelfRefAttr_")
  //          case Some("BiOtherRef_")    => Seq(s"BiOtherRefAttr_[$revNs.$revRef[NS, NS]]")
  //          case Some("BiEdgeRef_")     => Seq(s"BiEdgeRefAttr_[$revNs.$revRef[NS, NS]]")
  //          case Some("BiEdgePropAttr") => Seq(s"BiEdgePropAttr_")
  //          case Some("BiEdgePropRef_") => Seq(s"BiEdgePropRefAttr_")
  //          case Some("BiTargetRef_")   => Seq(s"BiTargetRefAttr_[$revNs.$revRef[NS, NS]]")
  //          case other                  => Nil
  //        })
  //        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
  //
  //        Seq(s"final class $attr${padAttr(attr)}[Ns, In] extends $clazz${padClass(clazz)}[Ns, In]$extensions")
  //
  //      case _: BackRef => Nil
  //    }
  //  }


  def getRef(r: Ref) = {
    val Ref(_, refAttrClean, _, clazz2, _, baseType, refNs0, _, _, _, _) = r
    val ((attr, attrO, attr_, attrK, attrK_, tpe, tpO, ns_ref, ns_ref_, ref),
    clsMan, clsOpt, ns0, ns1man, ns1opt, in1, in0, options0, options)    = nsData(r, r.bi)

    val cls          = if (clazz2 == "OneRef") "OneRef " else "ManyRef"
    val curNs        = ns + "_[p0]"
    val refNs        = refNs0 + "_[p0]"
    val refNs_0_0_L1 = refNs0 + "_" + in + "_" + out + "_L" + (level + 1)

    val refNsDef = refNs_0_0_L1 + "[" + `o0, p0` + s", $ns_ref_, Nothing" + `, I1, A` + "]"

    val nestedDef = if (clazz2 == "ManyRef") {
      val refNs2  = refAttrClean.capitalize
      val ref_0_0 = if (out < maxOut) refNs0 + "_" + in + "_" + (out + 1) else "D" + nn(out + 1)
      val ref_1_0 = if (maxIn > 0 && in < maxIn && out < maxOut) refNs0 + "_" + (in + 1) + "_" + (out + 1) else "D" + nn(out + 2)
      val ref_2_0 = if (maxIn > 0 && in < maxIn && out < maxOut) refNs0 + "_" + (in + 2) + "_" + (out + 1) else "D" + nn(out + 3)
      val ref_3_0 = if (maxIn > 0 && in < maxIn && out < maxOut) refNs0 + "_" + (in + 3) + "_" + (out + 1) else "D" + nn(out + 4)
      val nsTypes = s"$ref_0_0, $ref_1_0, $ref_2_0, $ref_3_0"

      s" with Nested${nn(out)}[$ns_ref_, " + `p0 with o1[p1]` + s", $nsTypes" + `, I1, A` + "]"
    } else {
      ""
    }
    s"final def $ref: $cls[$curNs, $refNs] with $refNsDef$nestedDef = ??? "
  }


  def indexedFirst(opts: Seq[Optional]): Seq[String] = {
    val classes = opts.filter(_.clazz.nonEmpty).map(_.clazz)
    if (classes.contains("Indexed"))
      "Indexed" +: classes.filterNot(_ == "Indexed")
    else
      classes
  }

  def nsData(a: DefAttr, bi: Option[String]) = {
    val (ns_attr, ns_attrO, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, ref) = formatted(a)

    val `,_,_`   = ",_" * (out + 1)
    val `,_`     = ",_" * out
    val options0 = indexedFirst(a.options) ++ bi.toList
    (
      (attr, attrO, attr_, attrK, attrK_, tpe, tpO, ns_ref, ns_ref_, ref),
      a.clazz + padClass(a.clazz),
      a.clazz + "$" + padClass(a.clazz),
      s"$ns_0_0_L0[" + `o0, p0` + `, I1, A` + "]",
      s"$ns_0_1_L0[" + `o0, p0` + s" with $ns_attr" + `, I1, A` + s", $tpe]",
      s"$ns_0_1_L0[" + `o0, p0` + s" with $ns_attrO" + `, I1, A` + s", $tpO]",
      if (maxIn == 0) s"$D01[o0,_" + `,_,_` + "]" else s"$D01[o0,_" + `,_,_` + "]", // todo
      if (maxIn == 0) s"$D00[o0,_" + `,_` + "]" else s"$D00[o0,_" + `,_` + "]", // todo
      options0,
      if (options0.isEmpty) "" else " with " + options0.mkString(" with ")
    )
  }

  val val_Attr   = mutable.MutableList.empty[String]
  val valAttr$   = mutable.MutableList.empty[String]
  val valAttr_   = mutable.MutableList.empty[String]
  val val_AttrK  = mutable.MutableList.empty[String]
  val valAttrK_  = mutable.MutableList.empty[String]
  val defRef     = mutable.MutableList.empty[String]
  val defBackRef = mutable.MutableList.empty[String]

  def getBackRef(br: BackRef) = {
    val backRefNs         = br.backRefNs
    val backRefNsPrefixed = "_" + backRefNs + padBackRefs(backRefNs)
    val backRef_0_0_L0    = backRefNs + "_" + in + "_" + out + "_L" + (level - 1)
    val concatLast     = level match {
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

  attrs.foreach {
    case a: Val =>
      val ((attr, attrO, attr_, attrK, attrK_, tpe, tpO, ns_ref, ns_ref_, ref),
      clsMan, clsOpt, ns0, ns1man, ns1opt, in1, in0, options0, options) = nsData(a, a.bi)
      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan[$ns1man, $in1] with $ns1man$options = ???"
        valAttr$ += s"final lazy val $attrO: $clsOpt[$ns1opt] with $ns1opt$options = ???"
      }
      valAttr_ += s"final lazy val $attr_ : $clsMan[$ns0, $in0] with $ns0$options = ???"
      if (a.clazz.startsWith("Map")) {
        val mapAttrKOptions = " with " + (options0 :+ "MapAttrK").mkString(" with ")
        val_AttrK += s"final lazy val $attrK  : String => $clsMan[$ns1man, $in1] with $ns1man$mapAttrKOptions = ???"
        valAttrK_ += s"final lazy val $attrK_ : String => $clsMan[$ns1opt, $in0] with $ns1opt$mapAttrKOptions = ???"
      }

    case r: Ref =>
      val ((attr, attrO, attr_, attrK, attrK_, tpe, tpO, ns_ref, ns_ref_, ref),
      clsMan, clsOpt, ns0, ns1man, ns1opt, in1, in0, options0, options) = nsData(r, r.bi)
      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan[$ns1man, $in1] with $ns1man$options = ???"
        valAttr$ += s"final lazy val $attrO: $clsOpt[$ns1opt] with $ns1opt$options = ???"
      }
      valAttr_ += s"final lazy val $attr_ : $clsMan[$ns0, $in0] with $ns0$options = ???"
      if (level < maxLevel)
        defRef += getRef(r)

    case a: Enum =>
      val ((attr, attrO, attr_, attrK, attrK_, tpe, tpO, ns_Ref, ns_Ref_, ref),
      clsMan, clsOpt, ns0, ns1man, ns1opt, in1, in0, options0, options) = nsData(a, a.bi)
      if (out < maxOut) {
        val_Attr += s"final lazy val $attr : $clsMan[$ns1man, $in1] with $ns1man$options = ???"
        valAttr$ += s"final lazy val $attrO: $clsOpt[$ns1opt] with $ns1opt$options = ???"
      }
      valAttr_ += s"final lazy val $attr_ : $clsMan[$ns0, $in0] with $ns0$options = ???"

    case br: BackRef =>
      if (level > 0)
        defBackRef += getBackRef(br)
  }

  val attrsNext: Seq[String] = if (level < maxLevel) val_Attr ++ val_AttrK ++ Seq("") ++ valAttr$ ++ Seq("") else Nil
  val attrKStay: Seq[String] = if (valAttrK_.nonEmpty) Seq("") ++ valAttrK_ else Nil
  val refs     : Seq[String] = if (defRef.nonEmpty) Seq("") ++ defRef else Nil
  val backRefs : Seq[String] = if (defBackRef.nonEmpty) Seq("") ++ defBackRef else Nil

  val body = (attrsNext ++ valAttr_ ++ attrKStay ++ refs ++ backRefs).mkString("\n  ").trim

  def get: String =
    s"""trait $ns_0_0_L0[${`o0[_], p0`}${`, I1, A`}] extends $ns_0_0[${`o0, p0 with o1[p1]`}${`, I1, A`}] {
       |
       |  $body
       |}
       |""".stripMargin
}
