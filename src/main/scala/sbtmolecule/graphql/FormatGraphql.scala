package sbtmolecule.graphql

import caliban.InputValue
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import caliban.parsing.adt.Type
import caliban.parsing.adt.Type.{ListType, NamedType}
import molecule.base.util.BaseHelpers


class FormatGraphql(
  typeNames: List[String],
  enumNames: List[String],
  entity: String,
  fields: List[FieldDefinition],
  arity: Int = 0
) extends BaseHelpers {
  //  val pkg      = metaDomain.pkg + ".dsl"
  //  val domain   = metaDomain.domain
  //  val maxArity = metaDomain.maxArity
  //  val ent      = metaEntity.ent

  //  val attrs    = metaEntity.attrs
  //  val refs     = attrs.filter(_.ref.nonEmpty)
  //  val backRefs = metaEntity.backRefs

  var refs = List.empty[Attr]



  val attrs = fields.map {
    case FieldDefinition(description, name, args, ofType, _) =>
      val TypeInfo(card, rawType, baseType, mandatory) = typeInfo(ofType)
      val args1                                        = args.map {
        case InputValueDefinition(description, arg, ofType, defaultValue, _) =>
          val TypeInfo(card, rawType, baseType, mandatory) = typeInfo(ofType)
          Arg(description, arg, card, rawType, baseType, mandatory, defaultValue)
      }

      val attr = Attr(
        description, name, args1, card, rawType, baseType, mandatory, getRef(rawType)
      )
      if (typeNames.contains(name)) {
        refs = refs :+ attr
      }
      attr
  }

  val attrs2 = fields.map {
    case FieldDefinition(description, name, args, ofType, _) =>
      val TypeInfo(card, rawType, baseType, mandatory) = typeInfo(ofType)
      val args1                                        = args.map {
        case InputValueDefinition(description, arg, ofType, defaultValue, _) =>
          val TypeInfo(card, rawType, baseType, mandatory) = typeInfo(ofType)
          Arg(description, arg, card, rawType, baseType, mandatory, defaultValue)
      }

      val attr = Attr(
        description, name, args1, card, rawType, baseType, mandatory, getRef(rawType)
      )
      if (typeNames.contains(name)) {
        refs = refs :+ attr
      }
      attr
  }

  case class Entity(
    description: Option[String],
    entity: String,
    args: List[Arg],
    ofType: Type,
    fields: List[Attr]
  )

  case class Attr(
    description: Option[String],
    attr: String,
    args: List[Arg],
    card: String,
    rawType: String,
    baseType: String,
    mandatory: Boolean,
    ref: Option[String]
  )
  case class TypeInfo(
    card: String,
    rawType: String,
    baseType: String,
    mandatory: Boolean
  )
  case class Arg(
    description: Option[String],
    name: String,
    card: String,
    rawType: String,
    baseType: String,
    mandatory: Boolean,
    defaultValue: Option[InputValue],
  )

  def camel(s: String) = s"${s.head.toUpper}${s.tail}"


  def formatDescription(description: Option[String]): String = description.fold("") {
    case line if !line.contains("\n") => s"// $line\n"
    case lines                        => lines.split("\n").map(line => s"// $line\n").mkString
  }



  def typeInfo(ofType: Type): TypeInfo = ofType match {
    case NamedType(rawTpe, mandatory)              => TypeInfo("One", rawTpe, getTpe(rawTpe), mandatory)
    case ListType(NamedType(rawTpe, _), mandatory) => TypeInfo("Seq", rawTpe, getTpe(rawTpe), mandatory)
    case _                                         => throw new Exception(s"Unsupported type: $ofType")
  }

  //  lazy val maxAttr      = attrs.map(_.attr.length).max
  //  lazy val maxBaseTpe   = attrs.map(a => getTpe(a.baseTpe).length).max
  //  lazy val maxRefAttr   = attrs.filter(_.ref.isDefined).map(entity => entity.attr.length).max
  //  lazy val maxRefEntity = attrs.flatMap(_.ref.map(_.length)).max

  //  lazy val padAttr      = (s: String) => padS(maxAttr, s)
  //  lazy val padType      = (s: String) => padS(maxBaseTpe, s)
  //  lazy val padRefAttr   = (s: String) => padS(maxRefAttr, s)
  //  lazy val padRefEntity = (s: String) => padS(maxRefEntity, s)

  lazy val maxAttr      = attrs.map(_.attr.length).max
  lazy val maxRefAttr   = attrs.filter(_.ref.isDefined).map(_.attr.length).max
  lazy val maxBaseTpe   = attrs.map(_.baseType.length).max
  lazy val maxRefEntity = attrs.flatMap(_.ref.map(_.length)).max

  lazy val padAttr      = (s: String) => padS(maxAttr, s)
  lazy val padType      = (s: String) => padS(maxBaseTpe, s)
  lazy val padRefAttr   = (s: String) => padS(maxRefAttr, s)
  lazy val padRefEntity = (s: String) => padS(maxRefEntity, s)

  lazy val V        = ('A' + arity - 1).toChar
  lazy val tpes     = (0 until arity) map (n => (n + 'A').toChar)
  lazy val _0       = "_" + arity
  lazy val _1       = "_" + (arity + 1)
  lazy val _2       = "_" + (arity + 2)
  lazy val ent_0    = entity + _0
  lazy val ent_1    = entity + _1
  lazy val ent_2    = entity + _2
  lazy val `, A`    = if (arity == 0) "" else ", " + tpes.mkString(", ")
  lazy val `A..U`   = if (arity <= 1) "" else tpes.init.mkString("", ", ", ", ")
  lazy val `A..V`   = if (arity == 0) "" else tpes.mkString(", ")
  lazy val `A..V, ` = if (arity == 0) "" else tpes.mkString("", ", ", ", ")
  lazy val `[A..V]` = if (arity == 0) "" else tpes.mkString("[", ", ", "]")

  def padN(n: Int) = if (n < 10) s"0$n" else n
  val n0 = padN(arity)

  def isRef(tpe: String) = typeNames.contains(tpe)

  def getTpe(tpe: String): String = tpe match {
    case "ID"                       => "String"
    case t if enumNames.contains(t) => "String"
    case t if isRef(t)              => "" // no ref attributes in graphql
    case t                          => t
  }

  def getRef(tpe: String): Option[String] = Some(tpe).filter(typeNames.contains)

}
