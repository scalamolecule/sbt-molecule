package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting

case class Entity_(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int,
  attrIndexPrev: Int
) extends Formatting(metaDomain, metaEntity) {

  val javaImports: String = {
    val typeImports = attributes.collect {
      case MetaAttribute(_, _, "Duration", _, _, _, _, _, _, _, _, _)       => "java.time.*"
      case MetaAttribute(_, _, "Instant", _, _, _, _, _, _, _, _, _)        => "java.time.*"
      case MetaAttribute(_, _, "LocalDate", _, _, _, _, _, _, _, _, _)      => "java.time.*"
      case MetaAttribute(_, _, "LocalTime", _, _, _, _, _, _, _, _, _)      => "java.time.*"
      case MetaAttribute(_, _, "LocalDateTime", _, _, _, _, _, _, _, _, _)  => "java.time.*"
      case MetaAttribute(_, _, "OffsetTime", _, _, _, _, _, _, _, _, _)     => "java.time.*"
      case MetaAttribute(_, _, "OffsetDateTime", _, _, _, _, _, _, _, _, _) => "java.time.*"
      case MetaAttribute(_, _, "ZonedDateTime", _, _, _, _, _, _, _, _, _)  => "java.time.*"
      case MetaAttribute(_, _, "Date", _, _, _, _, _, _, _, _, _)           => "java.util.Date"
      case MetaAttribute(_, _, "UUID", _, _, _, _, _, _, _, _, _)           => "java.util.UUID"
      case MetaAttribute(_, _, "URI", _, _, _, _, _, _, _, _, _)            => "java.net.URI"
    }.distinct
    if(typeImports.isEmpty) "" else "\n" + typeImports.sorted.mkString("import ", "\nimport ", "")
  }

  private val entityList     : Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private val attrList       : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attribute <- entity.attributes
    } yield entity.entity + "." + attribute.attribute
  }
  private val entity_Builders: String      = List(0, 1, 2)
    .map(Entity_Builder(metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")

  private val entity_Expr  = Entity_Exprs(metaDomain, metaEntity).get
  private val entity_Attrs = Entity_Attrs(metaDomain, metaEntity, nsIndex, attrIndexPrev).get

  def get: String = {
    s"""// AUTO-GENERATED Molecule boilerplate code
       |package $pkg.$domain
       |package ops // to access enums and let them be public to the user
       |$javaImports
       |import molecule.base.metaModel.*
       |import molecule.core.dataModel as _dm
       |import molecule.core.dataModel.*
       |import molecule.db.common.api.*
       |import molecule.db.common.api.expression.*
       |import molecule.db.common.ops.ModelTransformations_.*
       |import scala.Tuple.:*
       |
       |
       |$entity_Builders
       |
       |$entity_Expr
       |
       |
       |$entity_Attrs
       |
       |
       |trait ${entity}_Sort_1[T] extends Sort[${entity}_1[T]] { self: Molecule =>
       |  override def sortEntity: DataModel => ${entity}_1[T] = (dm: DataModel) => ${entity}_1[T](dm)
       |}
       |
       |trait ${entity}_Sort_n[Tpl <: Tuple] extends Sort[${entity}_n[Tpl]] { self: Molecule =>
       |  override def sortEntity: DataModel => ${entity}_n[Tpl] = (dm: DataModel) => ${entity}_n[Tpl](dm)
       |}
       |""".stripMargin
  }
}
