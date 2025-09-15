package sbtmolecule.graphql.dsl

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import caliban.parsing.adt.Type.{ListType, NamedType}
import sbtmolecule.graphql.FormatGraphql


case class GraphqlQuery(
  pkg: String,
  domain: String,
  typeNames: List[String],
  enumNames: List[String],
  description: Option[String],
  fields: List[FieldDefinition]
) extends FormatGraphql(typeNames, enumNames,domain, fields) {

  val queries = fields.map {
    case FieldDefinition(description, name, args, ofType, _) =>
      val (inputs, attrs) = if (args.isEmpty) {
        (Seq.empty[String], Seq.empty[String])
      } else {
        args.map {
          case InputValueDefinition(description, arg, ofType, defaultValue, _) =>
            val (value, tpe, mandatory) = ofType match {
              case NamedType(tpe, mandatory)              => ("One", tpe, mandatory)
              case ListType(NamedType(tpe, _), mandatory) => ("Seq", tpe, mandatory)
              case _                                      => throw new Exception(s"Unsupported type: $ofType")
            }

            val ref     = Some(tpe).filter(typeNames.contains).fold("")(t => s", ref = Some($t)")
            val tpe1    = getTpe(tpe)
            val optType = if (mandatory) tpe1 else s"Option[$tpe1]"
            (s"$arg: $optType", s"""Attr${value}Tac$tpe1("$name", "$arg", Eq, Seq($arg)$ref)""")

        }.unzip
      }

      val inputs1 = if (inputs.nonEmpty) inputs.mkString("(", ", ", ")") else ""
      val attrs1  = if (inputs.nonEmpty) attrs.mkString("List(\n      ", ",\n      ", "\n    )") else "Nil"

      s"""def $name$inputs1: ${ofType}_0[Nothing] = {
         |    new ${ofType}_0[Nothing](DataModel($attrs1))
         |  }""".stripMargin

  }.mkString("\n\n  ")


  def get: String = {
    s"""// AUTO-GENERATED Molecule boilerplate code
       |package $pkg.$domain
       |
       |import molecule.core.dataModel.*
       |
       |object query extends query
       |
       |trait query {
       |
       |  $queries
       |}
       |""".stripMargin
  }
}
