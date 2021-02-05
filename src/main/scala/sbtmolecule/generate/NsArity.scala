package sbtmolecule.generate

import sbtmolecule.Ast._

case class NsArity(
  model: Model,
  namespace: Namespace,
  in: Int,
  out: Int,
  genericPkg: String
) extends Helpers(model, namespace, in, out, genericPkg = genericPkg) {

  val nestedImports: List[String] = if (
    attrs.exists {
      case Ref(_, _, _, _, _, baseTpe, _, _, _, _, _) if baseTpe.nonEmpty => true
      case _                                                              => false
    }
  ) {
    List("nested", "Nested_1", "Nested_2", "Nested_3")
      .map("molecule.core.composition." + _ + "._").take(model.maxIn + 1)
  } else {
    Nil
  }

  // Collect traits for each level for this namespace/arity
  val levelTraits = (0 to maxLevel).map(level =>
    NsArityLevel(model, namespace, in, out, level, genericPkg).get
  ).mkString("\n\n")

  val (pkg, datomImport) = if (isGeneric)
    (genericPkg, Nil)
  else
    (model.pkg + ".dsl", Seq("molecule.core.generic.Datom._"))

  val extraImports = attrs.collect {
    case Val(_, _, _, "UUID", _, _, _, _, _, _) => "java.util.UUID"
    case Val(_, _, _, "URI", _, _, _, _, _, _)  => "java.net.URI"
  }.distinct ++ nestedImports ++ datomImport :+ s"$pkg.$domain._"

  val (baseNs, baseApi) = if (isGeneric) {
    if (isDatom)
      (ns, "")
    else
      (ns, s" with NS_${in}_${nn(out)}[o0, p0${`, I1, A`}]")
  } else {
    // Avoid any ns name clash like "A"
    ("_" + ns + "_", s" with Api_${in}_${nn(out)}[o0, p0, $ns_0_0, $ns_0_1, $ns_1_0, $ns_1_1${`, I1, A`}]")
  }

  val body =
    s"""trait $ns_0_0[o0[_], p0${`, I1, A`}] extends $ns_[p0] with $baseNs$baseApi
       |
       |$levelTraits
     """.stripMargin

  def get: String = Template(ns, pkg, model.domain, body, extraImports)
}
