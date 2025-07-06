package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import molecule.base.util.RegexMatching


case class Db_Datomic(metaDomain: MetaDomain) extends RegexMatching {

  private val flatEntities: Seq[MetaEntity] = metaDomain.segments.filterNot(_.segment == "_enum").flatMap(_.entities)

  private val datomicPartitions: String = {
    val partitions = metaDomain.segments.filterNot(s => s.segment.isEmpty || s.segment == "_enums").map(_.segment)
    if (partitions.isEmpty) "" else {
      edn(partitions.map { partition =>
        s"""|{:db/id      "$partition"
            |  :db/ident   :$partition}
            | [:db/add :db.part/db :db.install/partition "$partition"]""".stripMargin
      }.mkString("\n\n "))
    }
  }

  private val datomicAliases: String = {
    val attrsWithAlias = flatEntities.flatMap(_.attributes.tail).filter(_.alias.nonEmpty)
    if (attrsWithAlias.isEmpty) "" else {
      edn(attrsWithAlias.map { a =>
        val (attr, alias) = (a.attribute, a.alias.get)
        s"""|{:db/id      "$alias"
            |  :db/ident   :$attr}""".stripMargin
      }.mkString("\n\n "))
    }
  }

  private def datomicCardinality(metaAttribute: MetaAttribute): String = metaAttribute.cardinality match {
    case CardOne => "one"
    case CardSet => "many"
    case CardSeq => "one" // Array values encoded in one byte Array
    case CardMap => "many"
    case other   => throw new Exception("Yet unsupported cardinality: " + other)
  }

  private def datomicType(a: MetaAttribute): String = {
    if (a.cardinality == CardSeq && a.baseTpe == "Byte") {
      "bytes"
    } else a.baseTpe match {
      case "ID"             => "ref"
      case "String"         => "string"
      case "Int"            => "long"
      case "Long"           => "long"
      case "Float"          => "float"
      case "Double"         => "double"
      case "Boolean"        => "boolean"
      case "BigInt"         => "bigint"
      case "BigDecimal"     => "bigdec"
      case "Date"           => "instant"
      case "Duration"       => "string"
      case "Instant"        => "string"
      case "LocalDate"      => "string"
      case "LocalTime"      => "string"
      case "LocalDateTime"  => "string"
      case "OffsetTime"     => "string"
      case "OffsetDateTime" => "string"
      case "ZonedDateTime"  => "string"
      case "UUID"           => "uuid"
      case "URI"            => "uri"
      case "Byte"           => "long"
      case "Short"          => "long"
      case "Char"           => "string"
      case unexpected       =>
        throw new Exception(s"Unexpected base type: '$unexpected'")
    }
  }

  private def attrStmts(ns: String, a: MetaAttribute): String = {
    val mandatory = Seq(
      s""":db/ident         :$ns/${a.attribute}""",
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

    if (a.cardinality == CardSeq && a.baseTpe != "Byte") {
      s""":db/ident         :$ns/${a.attribute}
         |  :db/valueType     :db.type/ref
         |  :db/cardinality   :db.cardinality/many
         |  :db/index         true}
         |
         | {:db/ident         :$ns.${a.attribute}/i_
         |  :db/valueType     :db.type/long
         |  :db/cardinality   :db.cardinality/one
         |  :db/index         true}
         |
         | {:db/ident         :$ns.${a.attribute}/v_
         |  :db/valueType     :db.type/${datomicType(a)}
         |  :db/cardinality   :db.cardinality/one
         |  :db/index         true""".stripMargin
    } else if (a.cardinality == CardMap) {
      s""":db/ident         :$ns/${a.attribute}
         |  :db/valueType     :db.type/ref
         |  :db/cardinality   :db.cardinality/many
         |  :db/index         true}
         |
         | {:db/ident         :$ns.${a.attribute}/k_
         |  :db/valueType     :db.type/string
         |  :db/cardinality   :db.cardinality/one
         |  :db/index         true}
         |
         | {:db/ident         :$ns.${a.attribute}/v_
         |  :db/valueType     :db.type/${datomicType(a)}
         |  :db/cardinality   :db.cardinality/one
         |  :db/index         true""".stripMargin
    } else {
      (mandatory ++ options ++ descr).distinct.mkString("\n  ")
    }
  }

  private def attrDefs(metaEntity: MetaEntity): String = metaEntity.attributes.tail // no id attribute in Datomic
    .map(attrStmts(metaEntity.entity, _))
    .mkString("{", "}\n\n {", "}")

  private val datomicSchema: String = edn(flatEntities.map { ns =>
    val delimiter = "-" * (50 - ns.entity.length)
    s"""|;; ${ns.entity} $delimiter
        |
        | ${attrDefs(ns)}""".stripMargin
  }.mkString("\n\n\n "))

  private def edn(defs: String): String =
    s"""|[
        | $defs
        |]
        |""".stripMargin

  def getEDN: String = {
    s""";; Attributes ==========================================================
       |$datomicSchema
       |
       |;; Aliases =============================================================
       |$datomicAliases
       |""".stripMargin
  }

  // We are actually not using the Datomic partitions, only prefixed entity names
  // And they also need to be transacted in a separate transaction before the attributes are transacted
  def getEDN_withUnusedPartitions: String = {
    s""";; Partitions ==========================================================
       |$datomicPartitions
       |
       |;; Attributes ==========================================================
       |$datomicSchema
       |
       |;; Aliases =============================================================
       |$datomicAliases
       |""".stripMargin
  }

  val pkg    = metaDomain.pkg + ".dsl"
  val domain = metaDomain.domain
  def schemaResourcePath(db: String) = s"moleculeGen/$domain/${domain}_Schema_$db"

  def get: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.base.metaModel.*
        |import molecule.db.core.api.*
        |
        |
        |object ${domain}_MetaDb_datomic extends ${domain}_MetaDb with MetaDb_datomic {
        |
        |  /** Resource path to SQL schema file or Datomic EDN file to create the database */
        |  override val schemaResourcePath: String = "${schemaResourcePath("datomic.edn")}"
        |}""".stripMargin
}
