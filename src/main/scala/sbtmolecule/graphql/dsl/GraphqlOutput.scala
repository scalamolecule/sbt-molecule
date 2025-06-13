package sbtmolecule.graphql.dsl

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import sbtmolecule.graphql.FormatGraphql


case class GraphqlOutput(
  pkg: String,
  domain: String,
  maxArity: Int,
  typeNames: List[String],
  enumNames: List[String],
  entity: String,
  description: Option[String],
  fields: List[FieldDefinition]
) extends FormatGraphql(typeNames, enumNames, entity, fields) {

  println(typeNames)
  println(enumNames)

  private val imports: String = {
    val baseImports = Seq(
      "molecule.core.dataModel.*",
      "molecule.graphql.client.api.*",
    )
    baseImports.sorted.mkString("import ", "\nimport ", "")
  }

  val descr = formatDescription(description)

  private val baseEntity: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]

    attrs.collect {
      case Attr(description, attr, args, card, rawType, baseType, mandatory, ref) =>

        if (!typeNames.contains(rawType)) {
          val padA    = padAttr(attr)
          val padT0   = padType(baseType)
          val attrMan = "Attr" + card + "Man" + baseType
          val attrOpt = "Attr" + card + "Opt" + baseType

          man += s"""protected lazy val ${attr}_man$padA: $attrMan$padT0 = $attrMan$padT0("$entity", "$attr")"""
          if (attr != "id") {
            opt += s"""protected lazy val ${attr}_opt$padA: $attrOpt$padT0 = $attrOpt$padT0("$entity", "$attr")"""
          }
        }
    }
    val attrDefs = (man.result() ++ Seq("") ++ opt.result()).mkString("\n  ")

    s"""private[$domain] trait ${entity}_base {
       |  $attrDefs
       |}""".stripMargin
  }

  private val entities: String = (0 to maxArity)
    .map(
      //      GraphqlOutput_Arities(metaDomain, entityList, attrList, metaEntity, _).get
      i => s"arity $i ..."
    ).mkString("\n\n")

  def get: String = {
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
       |package $pkg.$domain
       |
       |$imports
       |
       |
       |$descr$baseEntity
       |
       |
       |$entities
       |""".stripMargin
  }
}
