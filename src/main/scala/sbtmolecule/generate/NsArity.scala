package sbtmolecule.generate

import sbtmolecule.Ast._

case class NsArity(
  model: Model,
  namespace: Namespace,
  in: Int,
  out: Int
) extends Spacing(model, namespace, in, out) {

  val nestedImports: List[String] = if (
    attrs.exists {
      case Ref(_, _, _, _, _, baseTpe, _, _, _, _, _) if baseTpe.nonEmpty => true
      case _                                                              => false
    }
  ) {
    List("nested", "Nested_1", "Nested_2", "Nested_3")
      .map("molecule.core._2_dsl.composition." + _ + "._").take(model.maxIn + 1)
  } else {
    Nil
  }


  val extraImports: Seq[String] = attrs.collect {
    case Val(_, _, _, "UUID", _, _, _, _, _, _) => "java.util.UUID"
    case Val(_, _, _, "URI", _, _, _, _, _, _)  => "java.net.URI"
  }.distinct ++ nestedImports

  // Collect traits for each level for this namespace/arity
  val levelTraits = (0 to maxLevel).map(level =>
    NsArityLevel(model, namespace, in, out, level).get
  ).mkString("\n\n")

  val Api = "Api_" + in + "_" + nn(out)

  val body =
    s"""trait $ns_0_0[o0[_], p0${`, I1, A`}] extends $ns_[p0] with $Api[o0, p0, $ns_0_0, $ns_0_1, $ns_1_0, $ns_1_1${`, I1, A`}]
       |
       |$levelTraits
     """.stripMargin

  def get: String = Template(ns, model.pkg, model.domain, body, extraImports)
}
