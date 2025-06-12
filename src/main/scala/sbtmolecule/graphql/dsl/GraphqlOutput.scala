package sbtmolecule.graphql.dsl

import molecule.core.model.*
import sbtmolecule.DslFormatting


case class GraphqlOutput(
  dbModel: DbModel,
  dbEntity: DbEntity,
  attrIndexPrev: Int = 0
) extends DslFormatting(dbModel, dbEntity) {

  private val entityList: Seq[String] = dbModel.segments.flatMap(_.ents.map(_.ent))
  private val attrList  : Seq[String] = {
    for {
      segment <- dbModel.segments
      entity <- segment.ents
      a <- entity.attrs
    } yield entity.ent + "." + a.attr
  }

  var attrIndex = attrIndexPrev

  private val imports: String = {
    val baseImports = Seq(
      "molecule.core.model",
      "molecule.core.model.*",
      "molecule.graphql.client.api.*",
    )
    val typeImports = attrs.collect {
      case DbAttribute(_, _, "Date", _, _, _, _, _, _, _) => "java.util.Date"
      case DbAttribute(_, _, "UUID", _, _, _, _, _, _, _) => "java.util.UUID"
      case DbAttribute(_, _, "URI", _, _, _, _, _, _, _)  => "java.net.URI"
    }.distinct
    (baseImports ++ typeImports).sorted.mkString("import ", "\nimport ", "")
  }

  private val baseEntity: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]

    attrs.collect {
      case DbAttribute(attr, card, tpe, _, _, _, _, _, _, _) if tpe.nonEmpty =>
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

  private val entities: String = (0 to dbModel.maxArity)
    .map(GraphqlOutput_Arities(dbModel, entityList, attrList, dbEntity, _).get).mkString("\n\n")

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
