package sbtmolecule.graphql.dsl

import molecule.base.ast.*
import sbtmolecule.DslFormatting


case class GraphqlEntity(
  metaDomain: MetaDomain,
  segmentPrefix: String,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0
) extends DslFormatting(metaDomain, metaEntity) {

  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.ents.map(_.ent))
  private val attrList  : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.ents
      a <- entity.attrs
    } yield entity.ent + "." + a.attr
  }

  var attrIndex = attrIndexPrev

  private val imports: String = {
    val baseImports = Seq(
      "molecule.base.ast.*",
      "molecule.core.ast",
      "molecule.core.ast.*",
      "molecule.graphql.client.api.*",
    )
    val typeImports = attrs.collect {
      case MetaAttribute(_, _, "Date", _, _, _, _, _, _, _) => "java.util.Date"
      case MetaAttribute(_, _, "UUID", _, _, _, _, _, _, _) => "java.util.UUID"
      case MetaAttribute(_, _, "URI", _, _, _, _, _, _, _)  => "java.net.URI"
    }.distinct
    (baseImports ++ typeImports).sorted.mkString("import ", "\nimport ", "")
  }

  private val baseEntity: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]

    attrs.collect {
      case MetaAttribute(attr, card, tpe, _, _, _, _, _, _, _) if tpe.nonEmpty =>
        val padA    = padAttr(attr)
        val padT0   = padType(tpe)
        val attrMan = "Attr" + card._marker + "Man" + tpe
        val attrOpt = "Attr" + card._marker + "Opt" + tpe
        attrIndex += 1

        man += s"""protected lazy val ${attr}_man$padA: $attrMan$padT0 = $attrMan$padT0("$ent", "$attr")"""
        if (attr != "id") {
          opt += s"""protected lazy val ${attr}_opt$padA: $attrOpt$padT0 = $attrOpt$padT0("$ent", "$attr")"""
        }
    }
    val attrDefs = (man.result() ++ Seq("") ++ opt.result()).mkString("\n  ")

    s"""private[$domain] trait ${ent}_base {
       |  $attrDefs
       |}""".stripMargin
  }

  private val entities: String = (0 to metaDomain.maxArity)
    .map(GraphqlEntity_Arities(metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")

  def get: String = {
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$ent`
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
