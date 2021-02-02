package sbtmolecule

import sbtmolecule.Ast._

object SchemaTransactionLowerToUpper {

  // Generate ..........................................

  def apply(d: Model): String = {

    val attributeDefinitions: String = d.nss.filterNot(ns => ns.attrs.isEmpty || ns.attrs.forall(_.attr.startsWith("_"))).map {
      case Namespace(_, _, ns, _, opt, attrs) =>
        val exts                      = opt.getOrElse("").toString
        val header                    = "// " + ns + exts + " " + ("-" * (65 - (ns.length + exts.length)))
        val (attrsBefore, attrsAfter) = attrs.flatMap { a =>
          val attrStmt = (firstLow(ns) + "/" + a.attrClean, ns + "/" + a.attrClean)
          a match {
            case e: Enum    =>
              attrStmt +: e.enums.map(enum =>
                (firstLow(ns) + "." + a.attrClean + "/" + enum, ns + "." + a.attrClean + "/" + enum)
              )
            case _: BackRef => Nil
            case _          => Seq(attrStmt)
          }
        }.unzip
        val maxLength                 = attrsBefore.map(_.length).max
        val stmts                     = attrsBefore.zip(attrsAfter).map {
          case (attrBefore, attrAfter) =>
            val indent = " " * (maxLength - attrBefore.length)
            s"""map(read(":db/id"), read(":$attrBefore")$indent, read(":db/ident"), read(":$attrAfter"))"""
        }
        header + "\n\n    " + stmts.mkString(",\n    ")
    }.mkString(",\n\n\n    ")

    s"""|/*
        |* AUTO-GENERATED Datomic Schema exchange boilerplate code
        |*
        |* To change:
        |* 1. edit schema definition file in `${d.pkg}.schema/`
        |* 2. `sbt compile` in terminal
        |* 3. Refresh and re-compile project in IDE
        |*/
        |package ${d.pkg}.schema
        |import datomic.Util._
        |
        |object ${d.domain}SchemaLowerToUpper {
        |
        |  lazy val namespaces = list(
        |
        |    $attributeDefinitions
        |  )
        |}""".stripMargin
  }
}
