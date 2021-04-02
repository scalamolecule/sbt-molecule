package sbtmolecule.schema

import sbtmolecule.ast.model._

object SchemaTransactionUpperToLower {

  // Generate ..........................................

  def apply(d: Model): String = {

    val attributeDefinitions: String = d.nss.filterNot(ns => ns.attrs.isEmpty || ns.attrs.forall(_.attr.startsWith("_"))).map {
      case Namespace(_, _, ns, _, opt, attrs) =>
        val exts   = opt.getOrElse("").toString
        val header = ";; " + ns + exts + " " + ("-" * (50 - (ns.length + exts.length)))
        val (attrsBefore, attrsAfter) = attrs.flatMap { a =>
          val attrStmt = (ns + "/" + a.attrClean, firstLow(ns) + "/" + a.attrClean)
          a match {
            case e: Enum    =>
              attrStmt +: e.enums.map(enum =>
                (ns + "." + a.attrClean + "/" + enum, firstLow(ns) + "." + a.attrClean + "/" + enum)
              )
            case _: BackRef => Nil
            case _          => Seq(attrStmt)
          }
        }.unzip
        val maxLength                 = attrsBefore.map(_.length).max
        val stmts                     = attrsBefore.zip(attrsAfter).map {
          case (attrBefore, attrAfter) =>
            val indent = " " * (maxLength - attrBefore.length)
            s"""{ :db/id :$attrBefore$indent   :db/ident :$attrAfter$indent }"""
        }
        header + "\n\n       " + stmts.mkString("\n       ")
    }.mkString("\n\n\n       ")

    s"""|/*
        |* AUTO-GENERATED Datomic Schema exchange boilerplate code
        |*
        |* To change:
        |* 1. edit schema definition file in `${d.pkg}.schema/`
        |* 2. `sbt compile` in terminal
        |* 3. Refresh and re-compile project in IDE
        |*/
        |package ${d.pkg}.schema
        |
        |object ${d.domain}SchemaUpperToLower {
        |
        |  lazy val edn =
        |    \"\"\"
        |     [
        |       $attributeDefinitions
        |     ]
        |    \"\"\"
        |}""".stripMargin
  }
}
