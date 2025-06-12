package sbtmolecule.db.schema

import molecule.core.model.*
import molecule.core.util.RegexMatching


case class Schema_Datomic(dbModel: DbModel) extends RegexMatching {

  private val flatEntities: Seq[DbEntity] = dbModel.segments.flatMap(_.ents)

  private val datomicPartitions: String = {
    val parts = dbModel.segments.filterNot(_.segment.isEmpty).map(_.segment)
    if (parts.isEmpty) "\"\"" else {
      edn(parts.map { part =>
        s"""|        {:db/id      "$part"
            |         :db/ident   :$part}
            |        [:db/add :db.part/db :db.install/partition "$part"]""".stripMargin
      }.mkString("\n\n"))
    }
  }

  private val datomicAliases: String = {
    val attrsWithAlias = flatEntities.flatMap(_.attrs.tail).filter(_.alias.nonEmpty)
    if (attrsWithAlias.isEmpty) "\"\"" else {
      edn(attrsWithAlias.map { a =>
        val (attr, alias) = (a.attr, a.alias.get)
        s"""|        {:db/id      "$alias"
            |         :db/ident   :$attr}""".stripMargin
      }.mkString("\n\n"))
    }
  }

  private def datomicCardinality(dbAttribute: DbAttribute): String = dbAttribute.card match {
    case CardOne => "one"
    case CardSet => "many"
    case CardSeq => "one" // Array values encoded in one byte Array
    case CardMap => "many"
    case other   => throw new Exception("Yet unsupported cardinality: " + other)
  }

  private def datomicType(a: DbAttribute): String = {
    if (a.card == CardSeq && a.baseTpe == "Byte") {
      "bytes"
    } else a.baseTpe match {
      case "ID" if a.ref.nonEmpty => "ref"
      case "ID"                   => "ref"
      case "String"               => "string"
      case "Int"                  => "long"
      case "Long"                 => "long"
      case "Float"                => "float"
      case "Double"               => "double"
      case "Boolean"              => "boolean"
      case "BigInt"               => "bigint"
      case "BigDecimal"           => "bigdec"
      case "Date"                 => "instant"
      case "Duration"             => "string"
      case "Instant"              => "string"
      case "LocalDate"            => "string"
      case "LocalTime"            => "string"
      case "LocalDateTime"        => "string"
      case "OffsetTime"           => "string"
      case "OffsetDateTime"       => "string"
      case "ZonedDateTime"        => "string"
      case "UUID"                 => "uuid"
      case "URI"                  => "uri"
      case "Byte"                 => "long"
      case "Short"                => "long"
      case "Char"                 => "string"
    }
  }

  private def attrStmts(ns: String, a: DbAttribute): String = {
    val mandatory = Seq(
      s""":db/ident         :$ns/${a.attr}""",
      s""":db/valueType     :db.type/${datomicType(a)}""",
      s""":db/cardinality   :db.cardinality/${datomicCardinality(a)}""",
      s""":db/index         true"""
    )
    val options   = a.options.flatMap {
      case "index"          => Seq(s""":db/index         true""")
      case "noHistory"      => Seq(s""":db/noHistory     true""")
      case "unique"         => Seq(s""":db/unique        :db.unique/value""")
      case "uniqueIdentity" => Seq(s""":db/unique        :db.unique/identity""")
      case "fulltext"       => Seq(s""":db/fulltext      true""")
      case "owner"          => Seq(s""":db/isComponent   true""")
      case _                => Nil
    }
    val descr     = a.description.fold(Seq.empty[String])(txt => Seq(s""":db/doc           "$txt""""))

    if (a.card == CardSeq && a.baseTpe != "Byte") {
      s""":db/ident         :$ns/${a.attr}
         |         :db/valueType     :db.type/ref
         |         :db/cardinality   :db.cardinality/many
         |         :db/index         true}
         |
         |        {:db/ident         :$ns.${a.attr}/i_
         |         :db/valueType     :db.type/long
         |         :db/cardinality   :db.cardinality/one
         |         :db/index         true}
         |
         |        {:db/ident         :$ns.${a.attr}/v_
         |         :db/valueType     :db.type/${datomicType(a)}
         |         :db/cardinality   :db.cardinality/one
         |         :db/index         true""".stripMargin
    } else if (a.card == CardMap) {
      s""":db/ident         :$ns/${a.attr}
         |         :db/valueType     :db.type/ref
         |         :db/cardinality   :db.cardinality/many
         |         :db/index         true}
         |
         |        {:db/ident         :$ns.${a.attr}/k_
         |         :db/valueType     :db.type/string
         |         :db/cardinality   :db.cardinality/one
         |         :db/index         true}
         |
         |        {:db/ident         :$ns.${a.attr}/v_
         |         :db/valueType     :db.type/${datomicType(a)}
         |         :db/cardinality   :db.cardinality/one
         |         :db/index         true""".stripMargin
    } else {
      (mandatory ++ options ++ descr).distinct.mkString("\n         ")
    }
  }

  private def attrDefs(dbEntity: DbEntity): String = dbEntity.attrs.tail // no id attribute in Datomic
    .map(attrStmts(dbEntity.ent, _))
    .mkString("{", "}\n\n        {", "}")

  private val datomicSchema: String = edn(flatEntities.map { ns =>
    val delimiter = "-" * (50 - ns.ent.length)
    s"""|        ;; ${ns.ent} $delimiter
        |
        |        ${attrDefs(ns)}""".stripMargin
  }.mkString("\n\n\n"))

  private val schemaData = List(
    datomicPartitions, datomicSchema, datomicAliases
  ).mkString("List(\n    ", ",\n\n    ", "\n  )")

  private def edn(defs: String): String =
    s"""|
        |    \"\"\"
        |      [
        |$defs
        |      ]
        |    \"\"\"""".stripMargin


  val domain = dbModel.domain
  def get: String =
    s"""|// AUTO-GENERATED Molecule Schema boilerplate code for the `$domain` domain
        |package ${dbModel.pkg}.schema
        |
        |import molecule.core.model.*
        |
        |
        |object ${domain}Schema_datomic extends ${domain}Schema with Schema_datomic {
        |
        |  override val schemaData: List[String] = $schemaData
        |}""".stripMargin
}
