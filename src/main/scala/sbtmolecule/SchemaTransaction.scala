package sbtmolecule

import Ast._

object SchemaTransaction {

  // Generate ..........................................

  def apply(d: Model): String = {

    def attrStmts(ns: String, a: DefAttr): String = {
      val ident = s"""read(":db/ident")             , read(":$ns/${a.attrClean}")"""
      def tpe(t: String) = s"""read(":db/valueType")         , read(":db.type/$t")"""
      def card(c: String) = s"""read(":db/cardinality")       , read(":db.cardinality/$c")"""
      val stmts = a match {
        case Val(_, _, clazz, _, _, t, options, _, _, _) if clazz.take(3) == "One" => Seq(tpe(t), card("one")) ++ options.map(_.datomicKeyValue)
        case Val(_, _, _, _, _, t, options, _, _, _)                               => Seq(tpe(t), card("many")) ++ options.map(_.datomicKeyValue)
        case a: DefAttr if a.clazz.take(3) == "One"                                => Seq(tpe("ref"), card("one")) ++ a.options.map(_.datomicKeyValue)
        case a: DefAttr                                                            => Seq(tpe("ref"), card("many")) ++ a.options.map(_.datomicKeyValue)
        case unexpected                                                            => throw new DataModelException(s"Unexpected attribute statement:\n" + unexpected)
      }
      s"map(${(ident +: stmts).mkString(",\n        ")})"
    }

    def enums(part: String, ns: String, a: String, es: Seq[String]): String = es.map(e =>
      s"""map(read(":db/id"), tempid(":$part"), read(":db/ident"), read(":$ns.$a/$e"))"""
    ).mkString(",\n    ")


    // Prepare schema for edge interlink meta data if a property edge is defined
    val (partitions: Seq[String], nss: Seq[Namespace]) = {
      val parts = d.nss.map(_.part).filterNot(_ == "db.part/user").distinct
      d.nss.collectFirst {
        case ns if ns.attrs.collectFirst {
          case Ref(_, _, _, _, _, _, _, _, Some("BiTargetRef_"), _, _) => true
        }.getOrElse(false) => {
          val moleculeMetaNs = Namespace("molecule", None, "molecule_Meta", None, None, Seq(
            Ref("otherEdge", "otherEdge", "OneRefAttr", "OneRef", "Long", "", "molecule_Meta", Seq(
              Optional("""read(":db/index")             , true.asInstanceOf[Object]""", "Indexed"),
              // Is component so that retracts automatically retracts the other edge
              Optional("""read(":db/isComponent")       , true.asInstanceOf[Object]""", "IsComponent")
            ))))
          (parts :+ "molecule", d.nss :+ moleculeMetaNs)
        }
      } getOrElse(parts, d.nss)
    }

    val partitionDefinitions: String = if (partitions.isEmpty) {
      "lazy val partitions = list()\n"
    } else {
      val ps = partitions.map { p =>
        s"""|map(read(":db/ident")             , read(":$p"),
            |        read(":db/id")                , tempid(":db.part/db"),
            |        read(":db.install/_partition"), read(":db.part/db"))""".stripMargin
      }
      s"""|lazy val partitions = list(
          |
          |    ${ps.mkString(",\n\n    ")}
          |  )
          |""".stripMargin
    }

    val attributeDefinitions: String = nss
      .filterNot(ns => ns.attrs.isEmpty || ns.attrs.forall(_.attr.startsWith("_"))) // No namespaces with no attributes or only back refs
      .map { ns =>
        val exts   = ns.opt.getOrElse("").toString
        val header = "// " + ns.ns + exts + " " + ("-" * (65 - (ns.ns.length + exts.length)))
        val attrs  = ns.attrs.flatMap { a =>
          val attr = attrStmts(ns.ns, a)
          a match {
            case e: Enum     => Seq(attr, enums(ns.part, ns.ns, a.attrClean, e.enums))
            case br: BackRef => Nil
            case _           => Seq(attr)
          }
        }
        header + "\n\n    " + attrs.mkString(",\n\n    ")
      }.mkString(",\n\n\n    ")

    s"""|/*
        |* AUTO-GENERATED Datomic Schema generation boilerplate code
        |*
        |* To change:
        |* 1. edit data model file in `${d.pkg}.dataModel/`
        |* 2. `sbt compile` in terminal
        |* 3. Refresh and re-compile project in IDE
        |*/
        |package ${d.pkg}.schema
        |import molecule.core.data.SchemaTransaction
        |import datomic.Util._
        |import datomic.Peer._
        |
        |object ${d.domain}Schema extends SchemaTransaction {
        |
        |  $partitionDefinitions
        |
        |  lazy val namespaces = list(
        |
        |    $attributeDefinitions
        |  )
        |}""".stripMargin
  }
}
