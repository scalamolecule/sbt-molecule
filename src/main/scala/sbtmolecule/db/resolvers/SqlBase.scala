package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import molecule.base.util.{BaseHelpers, RegexMatching}
import sbtmolecule.db.sqlDialect.{Dialect, PostgreSQL}
import scala.collection.mutable.ListBuffer


abstract class SqlBase(metaDomain: MetaDomain) extends RegexMatching with BaseHelpers {

  protected val entities: Seq[MetaEntity] = metaDomain.segments.flatMap(_.entities)

  protected var hasReserved         = false
  protected var reservedEntities    = Array.empty[Byte]
  protected var reservedAttrs       = Array.empty[Byte]
  protected var reservedEntityAttrs = Array.empty[String]
  protected val refs                = ListBuffer.empty[(String, String, String)] // entity, refAttr, ref

  val pkg    = metaDomain.pkg + ".dsl"
  val domain = metaDomain.domain
  def schemaResourcePath(db: String) = s"moleculeGen/${metaDomain.pkg.replace('.', '/')}/$domain/${domain}_$db"

  val b0 = 0.toByte
  val b1 = 1.toByte

  private def createTable(metaEntity: MetaEntity, dialect: Dialect): Seq[String] = {
    val entity = metaEntity.entity
    def reserved(a: MetaAttribute): Byte =
      if (dialect.reservedKeyWords.contains(a.attribute.toLowerCase)) b1 else b0
    val max = metaEntity.attributes.map {
      case a if a.cardinality == CardSet && a.ref.nonEmpty => 0
      case a if reserved(a) == b1                          => a.attribute.length + 1
      case a                                               => a.attribute.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(entity.toLowerCase)) "_" else ""

    val columns = metaEntity.attributes.flatMap {
      case a if a.attribute == "id" =>
        reservedAttrs = reservedAttrs :+ b0
        Some("id" + padS(max, "id") + " " + dialect.tpe(a))

      case a if a.cardinality == CardSet && a.ref.nonEmpty =>
        reservedAttrs = reservedAttrs :+ reserved(a)
        None

      case a =>
        val column = if (reserved(a) == b1) {
          hasReserved = true
          reservedAttrs = reservedAttrs :+ b1
          a.attribute + "_"
        } else {
          reservedAttrs = reservedAttrs :+ b0
          a.attribute
        }
        // Add foreign key references
        a.ref.foreach(refEntity => refs += ((entity, a.attribute, refEntity)))

        Some(column + padS(max, column) + " " + dialect.tpe(a))
    }.mkString(s",\n  ")

    val table =
      s"""CREATE TABLE IF NOT EXISTS $entity$tableSuffix (
         |  $columns
         |);
         |""".stripMargin

    val joinTables = metaEntity.attributes.collect {
      case MetaAttribute(refAttr, CardSet, _, _, Some(ref), _, _, _, _, _, _, _) =>
        val joinTable  = s"${entity}_${refAttr}_$ref"
        val (id1, id2) = if (entity == ref) ("1_id", "2_id") else ("id", "id")
        val (l1, l2)   = (entity.length, ref.length)
        val (p1, p2)   = if (l1 > l2) ("", " " * (l1 - l2)) else (" " * (l2 - l1), "")
        val ref1       = s"${entity}_$id1$p1"
        val ref2       = s"${ref}_$id2$p2"
        refs ++= List(
          (joinTable, s"${entity}_$id1", entity),
          (joinTable, s"${ref}_$id2", ref),
        )
        s"""CREATE TABLE IF NOT EXISTS $joinTable (
           |  $ref1 BIGINT,
           |  $ref2 BIGINT
           |);
           |""".stripMargin
    }

    table +: joinTables
  }

  protected def clean(dialect: Dialect, s: String) = {
    s + (if (dialect.reservedKeyWords.contains(s.toLowerCase)) "_" else "")
  }

  protected def getTables(dialect: Dialect): String = {
    hasReserved = false
    reservedEntities = Array.empty[Byte]
    reservedAttrs = Array.empty[Byte]
    reservedEntityAttrs = Array.empty[String]

    if (entities.isEmpty) {
      throw new Exception(s"No Entity traits defined in $domain")
    }
    val entityMax = entities.map(_.entity.length).max
    val pEntity   = (ent: String) => " " * (entityMax - ent.length)

    val tableDefinitions = entities.flatMap { entity =>
      val reservedEntity: Byte =
        if (dialect.reservedKeyWords.contains(entity.entity.toLowerCase)) b1 else b0
      reservedEntities = reservedEntities :+ reservedEntity
      val result = createTable(entity, dialect)
      reservedEntityAttrs = reservedEntityAttrs :+
        reservedAttrs.mkString(s"/* ${entity.entity}${pEntity(entity.entity)} */   ", ", ", "")
      reservedAttrs = Array.empty[Byte]
      result
    }

    val quote       = dialect match {
      case _: PostgreSQL.type => ""
      case _                  => "`"
    }
    val foreignKeys = if (refs.isEmpty) Nil else {
      var m1 = 0
      var m2 = 0
      var m3 = 0
      var m4 = 0
      refs.foreach {
        case (ent, refAttr, ref) =>
          m1 = clean(dialect, ent).length.max(m1)
          m2 = clean(dialect, refAttr).length.max(m2)
          m3 = clean(dialect, ref).length.max(m3)
          m4 = ((clean(dialect, refAttr) + quote).length + 2).max(m4)
      }

      val constraints = ListBuffer.empty[String]
      List(refs.map {
        case (ent, refAttr, ref) =>
          constraints += refAttr
          val ent1       = clean(dialect, ent)
          val table      = ent1 + padS(m1, ent1)
          val refAttr1   = clean(dialect, refAttr)
          val key        = refAttr1 + padS(m2, refAttr1)
          val refEnt1    = clean(dialect, ref)
          val ref1       = refEnt1 + padS(m3, refEnt1)
          val count      = constraints.count(_ == refAttr)
          val refAttr2   = refAttr1 + (if (count == 1) quote else "_" + count + quote)
          val constraint = s"${quote}_$refAttr2" + padS(m4, refAttr2)
          s"-- ALTER TABLE $table ADD CONSTRAINT $constraint FOREIGN KEY ($key) REFERENCES $ref1 (id);"
      }.mkString(
        "-- Optional reference constraints to avoid orphan relationships (add manually)\n",
        "\n",
        "\n"
      ))
    }

    (tableDefinitions ++ foreignKeys).mkString("\n")
  }

  protected def getReserved = if (hasReserved) {
    s"""
       |
       |  /** Indexes to lookup if entity names collide with db keyword */
       |  override val reservedEntities: IArray[Byte] = IArray(
       |    ${reservedEntities.mkString(", ")}
       |  )
       |
       |  /** Indexes to lookup if attribute names collide with db keyword */
       |  override val reservedAttributes: IArray[Byte] = IArray(
       |    ${reservedEntityAttrs.mkString(",\n    ")}
       |  )""".stripMargin
  } else ""
}
