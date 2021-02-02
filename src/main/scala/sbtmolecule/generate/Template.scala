package sbtmolecule.generate

import sbtmolecule.Ast.firstLow

object Template {

  def apply(
    ns: String,
    pkg: String,
    domain: String,
    body: String,
    extraImports: Seq[String] = Nil,
  ): String = {
    val imports = (Seq(
      "java.util.Date", // for txInstant
      "molecule.core._2_dsl.boilerplate.api._",
      "molecule.core._2_dsl.boilerplate.attributes._",
      "molecule.core._2_dsl.boilerplate.base._",
      "molecule.core._2_dsl.boilerplate.dummyTypes._",
//      "molecule.core.boilerplate.obj._",
      "scala.language.higherKinds",
    ) ++ extraImports).sorted.mkString("import ", "\nimport ", "")

    s"""/*
       |* AUTO-GENERATED Molecule DSL boilerplate code for namespace `$ns`
       |*
       |* To change:
       |* 1. edit data model file in `$pkg.dataModel/`
       |* 2. `sbt compile` in terminal
       |* 3. Refresh and re-compile project in IDE
       |*/
       |package $pkg.dsl.${firstLow(domain)}
       |
       |$imports
       |
       |$body
       |""".stripMargin
  }
}
