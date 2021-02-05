package sbtmolecule.generate2

import sbtmolecule.Ast._
import scala.collection.mutable

case class NsBase(
  model: Model,
  namespace: Namespace,
  genericPkg: String
) extends Spacing(model, namespace) {

  val propTraitsMan = mutable.MutableList.empty[String]
  val propTraitsOpt = mutable.MutableList.empty[String]
  val propTraitsMap = mutable.MutableList.empty[String]
  val propTraitsRef = mutable.MutableList.empty[String]

  attrs.foreach {
    case a: Val =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      propTraitsMan += s"trait $ns_attr  { lazy val $attr: $tpe = ??? }"
      propTraitsOpt += s"trait $ns_attrO { lazy val $attrO: $tpO = ??? }"

      if (a.clazz.startsWith("Map")) {
        propTraitsMap += s"trait $ns_attrK { lazy val $attrK: $baseTpe = ??? }"
      }

    case a: Ref =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      propTraitsMan += s"trait $ns_attr  { lazy val $attr: $tpe = ??? }"
      propTraitsOpt += s"trait $ns_attrO { lazy val $attrO: $tpO = ??? }"

      // Add underscore to distinguish it from ref attr (gives case warning)
      propTraitsRef += s"trait $ns_ref_[props] { def $ref: props = ??? }"

    case a: Enum =>
      val (ns_attr, ns_attrO, ns_attrK, ns_ref, ns_ref_, attr, attrO, attr_, attrK, attrK_, tpe, tpO, baseTpe, ref, refNsPad) = formatted(a)
      propTraitsMan += s"trait $ns_attr  { lazy val $attr: $tpe = ??? }"
      propTraitsOpt += s"trait $ns_attrO { lazy val $attrO: $tpO = ??? }"

    case _ =>
  }

  val qmImport = if (model.maxIn > 0) Seq("molecule.core.expression.AttrExpressions.?") else Nil

  val propTraits = (
    propTraitsMan ++ Seq("") ++ propTraitsOpt ++
      (if (propTraitsMap.nonEmpty) Seq("") ++ propTraitsMap else Nil) ++
      (if (propTraitsRef.nonEmpty) Seq("") ++ propTraitsRef else Nil)
    ).mkString("\n").trim


  val (pkg, genericNsImports, extendsGenericNs) = if (genericPkg.nonEmpty)
    (genericPkg, Seq("molecule.core.generic.GenericNs", s"$genericPkg.$domain._$ns._"), " extends GenericNs")
  else
    (model.pkg + ".dsl", Nil, "")

  val pkgAppend    = model.domain
  val extraImports = attrs.collect {
    case Val(_, _, _, "UUID", _, _, _, _, _, _) => "java.util.UUID"
    case Val(_, _, _, "URI", _, _, _, _, _, _)  => "java.net.URI"
  }.distinct ++ qmImport ++ genericNsImports

  val inputEids = if (model.maxIn == 0) "" else
    s"\n  final override def apply(eids: ?)               : ${ns}_1_0_L0[$ns_, Nothing, Long] = ???"

  val nsObject: String = if(genericPkg.nonEmpty) "" else
    s"""
       |object $ns extends ${ns}_0_0_L0[$ns_, Nothing] with FirstNS {
       |  final override def apply(eid: Long, eids: Long*): ${ns}_0_0_L0[$ns_, Nothing] = ???
       |  final override def apply(eids: Iterable[Long])  : ${ns}_0_0_L0[$ns_, Nothing] = ???$inputEids
       |}
       |""".stripMargin

  val body: String =
    s"""trait $ns$extendsGenericNs
       |$nsObject
       |// Object properties
       |
       |trait $ns_[props] { def $ns: props = ??? }
       |
       |$propTraits
       |""".stripMargin

  def get: String = Template(ns, pkg, pkgAppend, model.domain, body, extraImports)
}