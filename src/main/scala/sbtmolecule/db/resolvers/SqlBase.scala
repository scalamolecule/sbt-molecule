package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import molecule.base.util.{BaseHelpers, RegexMatching}
import sbtmolecule.db.sqlDialect.{Dialect, MySQL, PostgreSQL}
import scala.collection.mutable.ListBuffer


abstract class SqlBase(metaDomain: MetaDomain) extends RegexMatching with BaseHelpers {

  protected val entities: Seq[MetaEntity] = metaDomain.segments.flatMap(_.entities)

  protected var hasReserved         = false
  protected var reservedEntities    = Array.empty[Byte]
  protected var reservedAttrs       = Array.empty[Byte]
  protected var reservedEntityAttrs = Array.empty[String]
  protected val refs                = ListBuffer.empty[(String, String, String, Boolean)] // entity, refAttr, ref, isOwner
  protected val indexes             = ListBuffer.empty[String]

  val pkg    = metaDomain.pkg + ".dsl"
  val domain = metaDomain.domain
  def schemaResourcePath(db: String) = s"db/schema/${metaDomain.pkg.replace('.', '/')}/$domain/${domain}_$db"

  val b0 = 0.toByte
  val b1 = 1.toByte

  private def createTable(metaEntity: MetaEntity, dialect: Dialect): Seq[String] = {
    val entity = metaEntity.entity
    def reserved(a: MetaAttribute): Byte =
      if (dialect.reservedKeyWords.contains(a.attribute.toLowerCase)) b1 else b0
    val max = metaEntity.attributes.map {
      case a if a.value == SetValue && a.ref.nonEmpty => 0
      case a if reserved(a) == b1                     => a.attribute.length + 1
      case a                                          => a.attribute.length
    }.max.max(2)

    val tableSuffix = if (dialect.reservedKeyWords.contains(entity.toLowerCase)) "_" else ""

    // Get general custom column properties for this specific database
    val generalPropsForDb = metaDomain.generalDbColumnProps.getOrElse(dialect.dbId, Map.empty)

    val columns = metaEntity.attributes.flatMap {
      case a if a.attribute == "id" =>
        reservedAttrs = reservedAttrs :+ b0
        Some("id" + padS(max, "id") + " " + dialect.tpe(a, generalPropsForDb))

      case a if a.value == SetValue && a.ref.nonEmpty =>
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
        a.ref.foreach { refEntity =>
          refs += ((entity, a.attribute, refEntity, a.options.contains("owner")))
        }

        if (a.options.contains("index")) {
          val ifNotExists = if (dialect == MySQL) "" else " IF NOT EXISTS"
          indexes += s"CREATE INDEX$ifNotExists _${entity}_$column ON $entity ($column);"
        }

        Some(column + padS(max, column) + " " + dialect.tpe(a, generalPropsForDb))
    }.mkString(s",\n  ")

    val table =
      s"""CREATE TABLE IF NOT EXISTS $entity$tableSuffix (
         |  $columns
         |);
         |""".stripMargin

    List(table)
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

    val customIndexes = if (indexes.isEmpty) Nil else List(indexes.mkString("-- Column indexes\n", "\n", "\n"))

    val quote  = dialect match {
      case _: PostgreSQL.type => ""
      case _                  => "`"
    }
    val extras = if (refs.isEmpty) customIndexes else {
      var m1 = 0
      var m2 = 0
      var m3 = 0
      var m4 = 0
      refs.foreach {
        case (ent, refAttr, ref, _) =>
          m1 = clean(dialect, ent).length.max(m1)
          m2 = clean(dialect, refAttr).length.max(m2)
          m3 = clean(dialect, ref).length.max(m3)
          m4 = ((clean(dialect, refAttr) + quote).length + 2).max(m4)
      }

      val constraints                 = ListBuffer.empty[String]
      val (constraintStrs, indexStrs) = refs.map {
        case (ent, refAttr, ref, isOwner) =>
          constraints += refAttr
          val ent1            = clean(dialect, ent)
          val table           = ent1 + padS(m1, ent1)
          val refAttr1        = clean(dialect, refAttr)
          val key             = refAttr1 + padS(m2, refAttr1)
          val refEnt1         = clean(dialect, ref)
          val ref1            = refEnt1 + padS(m3, refEnt1)
          val count           = constraints.count(_ == refAttr)
          val refAttr2        = refAttr1 + (if (count == 1) quote else "_" + count + quote)
          val constraint      = s"${quote}_$refAttr2" + padS(m4, refAttr2)
          val onDeleteCascade = if (isOwner) " ON DELETE CASCADE" else ""
          val ifNotExists = if (dialect == MySQL) "" else " IF NOT EXISTS"
          (
            s"ALTER TABLE $table ADD CONSTRAINT $constraint FOREIGN KEY ($key) REFERENCES $ref1 (id)$onDeleteCascade;",
            s"CREATE INDEX$ifNotExists _${ent1}_$refAttr1 ON $ent1 ($key);"
          )
      }.unzip


      List(
        constraintStrs.mkString("-- Foreign key constraints\n", "\n", "\n"),
        indexStrs.mkString("-- Foreign key indexes\n", "\n", "\n")
      ) ++ customIndexes
    }

    (tableDefinitions ++ extras).mkString("\n")
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
