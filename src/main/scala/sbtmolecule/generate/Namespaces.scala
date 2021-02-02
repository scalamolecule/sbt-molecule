//package sbtmolecule.generate
//
//import sbtmolecule.Ast.{BackRef, Enum, Model, Namespace, Optional, Ref, Val, firstLow, padS}
//
//case class Namespaces(model: Model) {
//
//  def nsBodies(namespace: Namespace): (String, Seq[String], Seq[(Int, String)]) = {
//    val inArity  = model.in
//    val outArity = model.out
//    val ns       = namespace.ns
//    val Ns       = ns.capitalize
//    val attrs    = namespace.attrs
//    val p1       = (s: String) => padS(attrs.map(_.attr).filterNot(_.startsWith("_")).map(_.length + 1).max, s)
//    val p2       = (s: String) => padS(attrs.map(_.clazz).filterNot(_.startsWith("Back")).map(_.length).max, s)
//    val p3       = (s: String) => padS(attrs.map(_.attrClean).filterNot(_.startsWith("_")).map(_.length).max, s)
//    val p4       = (s: String) => padS(attrs.map(_.tpe).map(_.length).max, s)
//
//    def indexedFirst(opts: Seq[Optional]): Seq[String] = {
//      val classes = opts.filter(_.clazz.nonEmpty).map(_.clazz)
//      if (classes.contains("Indexed"))
//        "Indexed" +: classes.filterNot(_ == "Indexed")
//      else
//        classes
//    }
//
//    val attrClasses: String = attrs.flatMap {
//      case Val(attr, attrClean, clazz, _, baseTpe, _, opts, bi, _, _) if clazz.startsWith("Map") =>
//        val extensions0 = indexedFirst(opts) ++ bi.toList
//        val extensions1 = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
//        val extensions2 = " with " + (extensions0 :+ "MapAttrK").mkString(" with ")
//        Seq(
//          s"final class $attr${p3(attr)} [Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions1",
//          s"final class ${attrClean}K${p3(attrClean)}[Ns, In] extends One$baseTpe${p2("One" + baseTpe)}[Ns, In]$extensions2"
//        )
//
//      case Val(attr, _, clazz, _, _, _, opts, bi, _, _) =>
//        val extensions0 = indexedFirst(opts) ++ bi.toList
//        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
//        Seq(s"final class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")
//
//      case Enum(attr, _, clazz, _, _, enums, opts, bi, _, _) =>
//        val extensions0 = indexedFirst(opts) ++ bi.toList
//        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
//        val enumValues  = s"private lazy val ${enums.mkString(", ")} = EnumValue"
//        Seq(s"""final class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions { $enumValues }""")
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
//        Seq(s"final class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")
//
//      case _: BackRef => Nil
//    }.mkString("\n  ").trim
//
//
//    val attrClassesOpt = attrs.flatMap {
//      case Val(_, attrClean, clazz, _, _, _, opts, bi, _, _) =>
//        val extensions0 = indexedFirst(opts) ++ bi.toList
//        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
//        Some(s"final class $attrClean$$${p3(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions")
//
//      case Enum(_, attrClean, clazz, _, _, enums, opts, bi, _, _) =>
//        val extensions0 = indexedFirst(opts) ++ bi.toList
//        val extensions  = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
//        val enumValues  = s"private lazy val ${enums.mkString(", ")} = EnumValue"
//        Some(s"""final class $attrClean$$${p3(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions { $enumValues }""")
//
//      case Ref(_, attrClean, clazz, _, _, _, revNs, opts, bi, revRef, _) =>
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
//        Some(s"final class $attrClean$$${p3(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions")
//
//      case _: BackRef => None
//    }.mkString("\n  ").trim
//
//    var propTraits0     = List.empty[String]
//    var propTraitsOpt0  = List.empty[String]
//    var propTraitsMapK0 = List.empty[String]
//    var refTraits0      = List.empty[String]
//
//    attrs.foreach {
//      case Val(attr, attrClean, clazz, tpe, _, _, _, _, _, _) if clazz.startsWith("Map") =>
//        propTraits0 = propTraits0 :+
//          s"trait ${ns}_$attrClean${p3(attrClean)}  { lazy val $attr${p1(attr)}: $tpe${p4(tpe)} = ??? }"
//        propTraitsOpt0 = propTraitsOpt0 :+
//          s"trait ${ns}_$attrClean$$${p3(attrClean)} { lazy val $attrClean$$${p3(attrClean)}: Option[$tpe]${p4(tpe)} = ??? }"
//        propTraitsMapK0 = propTraitsMapK0 :+
//          s"trait ${ns}_${attrClean}K${p3(attrClean)} { lazy val ${attrClean}K${p3(attrClean)}: $tpe${p4(tpe)} = ??? }"
//
//      case Val(attr, attrClean, _, tpe, _, _, _, _, _, _) =>
//        propTraits0 = propTraits0 :+
//          s"trait ${ns}_$attrClean${p3(attrClean)}  { lazy val $attr${p1(attr)}: $tpe${p4(tpe)} = ??? }"
//        propTraitsOpt0 = propTraitsOpt0 :+
//          s"trait ${ns}_$attrClean$$${p3(attrClean)} { lazy val $attrClean$$${p3(attrClean)}: Option[$tpe]${p4(tpe)} = ??? }"
//
//      case Enum(attr, attrClean, _, tpe, _, _, _, _, _, _) =>
//        propTraits0 = propTraits0 :+
//          s"trait ${ns}_$attrClean${p3(attrClean)}  { lazy val $attr${p1(attr)}: $tpe${p4(tpe)} = ??? }"
//        propTraitsOpt0 = propTraitsOpt0 :+
//          s"trait ${ns}_$attrClean$$${p3(attrClean)} { lazy val $attrClean$$${p3(attrClean)}: Option[$tpe]${p4(tpe)} = ??? }"
//
//      case Ref(attr, attrClean, _, tpe0, _, _, _, _, _, _, _) =>
//        val tpe = if (tpe0 == "OneRef") "Long" else "Set[Long]"
//        propTraits0 = propTraits0 :+
//          s"trait ${ns}_$attrClean${p3(attrClean)}  { lazy val $attr${p1(attr)}: $tpe${p4(tpe)} = ??? }"
//        propTraitsOpt0 = propTraitsOpt0 :+
//          s"trait ${ns}_$attrClean$$${p3(attrClean)} { lazy val $attrClean$$${p3(attrClean)}: Option[$tpe]${p4(tpe)} = ??? }"
//
//        val Attr = attrClean.capitalize
//        refTraits0 = refTraits0 :+
//          s"trait ${ns}_${Attr}_${p3(attrClean)}[props] { def $Attr${p3(attrClean)}: props = ??? }"
//
//      case _ =>
//    }
//    val propTraits     = propTraits0.mkString("\n").trim
//    val propTraitsOpt  = propTraitsOpt0.mkString("\n").trim
//    val refTraits      = refTraits0.mkString("\n").trim
//    val propTraitsMapK = {
//      if (propTraitsMapK0.isEmpty) "" else propTraitsMapK0.mkString("\n\n", "\n", "")
//    }
//
//    val nestedImports: List[String] = if (
//      attrs.exists {
//        case Ref(_, _, _, _, _, baseTpe, _, _, _, _, _) if baseTpe.nonEmpty => true
//        case _                                                              => false
//      }
//    )
//      List("nested", "Nested_In_1", "Nested_In_2", "Nested_In_3")
//        .map("molecule.core.composition." + _ + "._").take(inArity + 1)
//    else
//      Nil
//
//    val extraImports: String = (("import java.util.Date" +: attrs.collect {
//      case Val(_, _, _, "UUID", _, _, _, _, _, _) => "java.util.UUID"
//      case Val(_, _, _, "URI", _, _, _, _, _, _)  => "java.net.URI"
//    }).distinct ++ nestedImports).mkString("\nimport ")
//
//    val inputEids = if (inArity == 0) "" else
//      s"\n  final override def apply(eids: ?)               : ${ns}_In_1_0[Nothing, Long] = ???"
//
//
//    def mkOutBody(content: String): String =
//      s"""/*
//         |* AUTO-GENERATED Molecule DSL boilerplate code for namespace `$ns`
//         |*
//         |* To change:
//         |* 1. edit data model file in `${model.pkg}.dataModel/`
//         |* 2. `sbt compile` in terminal
//         |* 3. Refresh and re-compile project in IDE
//         |*/
//         |package ${model.pkg}.dsl
//         |package ${firstLow(model.domain)}
//         |$extraImports
//         |import scala.language.higherKinds
//         |import molecule.core.boilerplate.attributes._
//         |import molecule.core.boilerplate.base._
//         |import molecule.core.boilerplate.dummyTypes._
//         |import molecule.core.boilerplate.obj._
//         |import molecule.core.boilerplate.out._
//         |import molecule.core.expression.AttrExpressions.?
//         |
//         |
//         |$content""".stripMargin
//
//    def mkBody(in: Int, content: String): String = {
//      s"""/*
//         |* AUTO-GENERATED Molecule DSL boilerplate code for namespace `$ns`
//         |*
//         |* To change:
//         |* 1. edit data model file in `${model.pkg}.dataModel/`
//         |* 2. `sbt compile` in terminal
//         |* 3. Refresh and re-compile project in IDE
//         |*/
//         |package ${model.pkg}.dsl
//         |package ${firstLow(model.domain)}
//         |$extraImports
//         |import scala.language.higherKinds
//         |import molecule.core.boilerplate.attributes._
//         |import molecule.core.boilerplate.base._
//         |import molecule.core.boilerplate.dummyTypes._
//         |import molecule.core.boilerplate.in$in._
//         |import molecule.core.boilerplate.obj._
//         |
//         |$content""".stripMargin
//    }
//
//    // Adding underscore to Ns-trait to avoid collision with "Obj", "Prop", "Tpe" etc
//    val outBody: String = mkOutBody(
//      s"""object $ns extends ${ns}_0 with FirstNS {
//         |  final override def apply(eid: Long, eids: Long*): ${ns}_0 = ???
//         |  final override def apply(eids: Iterable[Long])  : ${ns}_0 = ???$inputEids
//         |}
//         |
//         |trait ${ns}_ {
//         |  $attrClasses
//         |
//         |  $attrClassesOpt$propTraitsMapK
//         |}
//         |
//         |trait ${ns}_Obj[o0[_], p0] extends Composite[o0, p0] {
//         |  def $Ns: p0 = ???
//         |}
//         |
//         |$propTraits
//         |
//         |$propTraitsOpt$propTraitsMapK
//         |
//         |$refTraits
//         |""".stripMargin)
//
//    val outBodies: Seq[String] = (0 to outArity).map(arity =>
//      mkOutBody(nsTrait(model.domain, namespace, 0, arity, inArity, outArity))
//    )
//
//    val inBodies: Seq[(Int, String)] = if (inArity == 0) {
//      Nil
//    } else {
//      for {
//        in <- 0 to inArity
//        out <- 0 to outArity
//      } yield {
//        (in, mkBody(in, nsTrait(model.domain, namespace, in, out, inArity, outArity)))
//      }
//    }
//
//    (outBody, outBodies, inBodies)
//  }
//
//}
