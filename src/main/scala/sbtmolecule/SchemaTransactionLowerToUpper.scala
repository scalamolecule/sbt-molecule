package sbtmolecule

import sbtmolecule.Ast._

object SchemaTransactionLowerToUpper {

  // Generate ..........................................

  def apply(d: Definition): String = {

    val attributeDefinitions: String = d.nss.filterNot(ns => ns.attrs.isEmpty || ns.attrs.forall(_.attr.startsWith("_"))).map {
      case Namespace(part, _, ns, _, opt, attrs) =>
        val exts = opt.getOrElse("").toString
        val header = "// " + ns + exts + " " + ("-" * (65 - (ns.length + exts.length)))
        val stmts = attrs.flatMap { a =>
          val attrStmt = s"""Util.map(":db/id", ":${firstLow(ns)}/${a.attrClean}", ":db/ident", ":$ns/${a.attrClean}")"""
          a match {
            case e: Enum     => Seq(
              attrStmt,
              e.enums.map(enum =>
                s"""Util.map(":db/id", ":${firstLow(ns)}.${a.attrClean}/$enum", ":db/ident", ":$ns.${a.attrClean}/$enum")"""
              ).mkString(",\n    ")
            )
            case br: BackRef => Nil
            case _           => Seq(attrStmt)
          }
        }
        header + "\n\n    " + stmts.mkString(",\n\n    ")
    }.mkString(",\n\n\n    ")

    s"""|/*
        |* AUTO-GENERATED Molecule DSL schema boilerplate code
        |*
        |* To change:
        |* 1. edit schema definition file in `${d.pkg}.schema/`
        |* 2. `sbt compile` in terminal
        |* 3. Refresh and re-compile project in IDE
        |*/
        |package ${d.pkg}.schema
        |import datomic.{Util, Peer}
        |
        |object ${d.domain}SchemaLowerToUpper {
        |
        |  lazy val namespaces = Util.list(
        |
        |    $attributeDefinitions
        |  )
        |}""".stripMargin
  }
}
