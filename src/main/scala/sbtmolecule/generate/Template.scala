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
    val dataModel = domain + "DataModel"
    val imports = (Seq(
      "java.util.Date", // for txInstant
      "molecule.core._2_dsl.boilerplate.api._",
      "molecule.core._2_dsl.boilerplate.attributes._",
      "molecule.core._2_dsl.boilerplate.base._",
      "molecule.core._2_dsl.boilerplate.dummyTypes._",
      "scala.language.higherKinds",
    ) ++ extraImports).sorted.mkString("import ", "\nimport ", "")

    s"""/*
       |* AUTO-GENERATED Molecule DSL for namespace `$ns`
       |*
       |* To change:
       |* 1. Edit data model in $pkg.dataModel/$dataModel
       |* 2. `sbt clean compile`
       |* 3. Re-compile project in IDE
       |*/
       |package $pkg.dsl.${firstLow(domain)}
       |
       |$imports
       |
       |$body
       |""".stripMargin
  }
}
