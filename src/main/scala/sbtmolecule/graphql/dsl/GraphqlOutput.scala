package sbtmolecule.graphql.dsl

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.Formatting


case class GraphqlOutput(
    metaDomain: MetaDomain,
    metaEntity: MetaEntity,
    attrIndexPrev: Int = 0
) extends Formatting(metaDomain, metaEntity){

  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private val attrList  : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.entities
      a <- entity.attributes
    } yield entity.entity + "." + a.attribute
  }

  var attrIndex = attrIndexPrev

  val imports: String = {
    val baseImports = Seq(
      "molecule.core.dataModel.*",
      "molecule.graphql.client.api.*",
    )
    val typeImports = attributes.collect {
      case MetaAttribute(_, _, "Date", _, _, _, _, _, _, _, _, _, _, _, _, _, _) => "java.util.Date"
      case MetaAttribute(_, _, "UUID", _, _, _, _, _, _, _, _, _, _, _, _, _, _) => "java.util.UUID"
      case MetaAttribute(_, _, "URI", _, _, _, _, _, _, _, _, _, _, _, _, _, _)  => "java.net.URI"
    }.distinct
    (baseImports ++ typeImports).sorted.mkString("import ", "\nimport ", "")
  }

  private val baseEntity: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]

    attributes.collect {
      case MetaAttribute(attr, value, tpe, _, _, _, _, _, _, _, _, _, _, _, _, _, _) if tpe.nonEmpty =>
        val padA    = padAttr(attr)
        val padT0   = padType(tpe)
        val attrMan = "Attr" + value._marker + "Man" + tpe
        val attrOpt = "Attr" + value._marker + "Opt" + tpe
        attrIndex += 1

        man += s"""protected lazy val ${attr}_man$padA: $attrMan$padT0 = $attrMan$padT0("$entity", "$attr")"""
        if (attr != "id") {
          opt += s"""protected lazy val ${attr}_opt$padA: $attrOpt$padT0 = $attrOpt$padT0("$entity", "$attr")"""
        }
    }
    val attrDefs = (man.result() ++ Seq("") ++ opt.result()).mkString("\n  ")

    s"""private[$domain] trait ${entity}_base {
       |  $attrDefs
       |}""".stripMargin
  }

  private val entities: String = "???"
//  private val entities: String = (0 to metaDomain.maxArity)
//    .map(GraphqlOutput_Arities(metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")

  def get: String = {
    s"""// AUTO-GENERATED Molecule boilerplate code
       |package $pkg.$domain
       |
       |$imports
       |
       |
       |$baseEntity
       |
       |
       |$entities
       |""".stripMargin
  }
}
