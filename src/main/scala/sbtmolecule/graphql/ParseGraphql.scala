package sbtmolecule.graphql

import java.io.File
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.*
import caliban.parsing.adt.Type.{ListType, NamedType}
import caliban.parsing.adt.{Directive, Document, Type}
import caliban.tools.SchemaLoader
import molecule.base.ast.*
import molecule.base.ast.Endpoint.GraphQL
import molecule.base.error.ModelError
import molecule.base.util.BaseHelpers
import zio.*
import scala.meta.*
import scala.util.Try

case class ParseGraphql(
  filePath: String,
  pkg: String,
  domain: String,
  maxArity: Int,
  urlOrPath: String,
  body: List[Stat]
) extends BaseHelpers {

  //  println(
  //    s"""filePath : $filePath
  //       |pkg      : $pkg
  //       |domain   : $domain
  //       |maxArity : $maxArity
  //       |urlOrPath: '$urlOrPath'
  //       |""".stripMargin
  //  )

  // Get Graphql schema with Caliban --------------------------------------------------------

  private val runtime = Runtime.default

  private val doc = urlOrPath match {
    case ""                            => docFromFile(filePath.dropRight(6) + ".graphql")
    case url if url.startsWith("http") => docFromUrl
    case _                             => docFromFile(urlOrPath)
  }


  private def docFromFile(path: String): Document = {
    if (new File(path).exists()) {
      Try {
        Unsafe.unsafe { implicit u =>
          runtime.unsafe.run(
            SchemaLoader.fromFile(path).load
          ).getOrThrow()
        }
      }.getOrElse(throw ModelError(s"Couldn't find graphql definition file at path: $path"))
    } else {
      val msg = if (urlOrPath.isEmpty)
        s"When no url or path is supplied, please add a graphql schema file named $domain.graphql " +
          s"beside your $domain.scala definition file."
      else
        s"Couldn't find definition file at path: $path."
      throw ModelError(msg)
    }
  }

  private def docFromUrl: Document = {
    try {
      Unsafe.unsafe { implicit u =>
        runtime.unsafe.run(
          SchemaLoader.fromIntrospection(urlOrPath, None).load
        ).getOrThrow()
      }
    } catch {
      case e: Throwable =>
        // Let it blow and show the problem
        e.printStackTrace()
        throw e
    }
  }

  private val types   = doc.typeDefinitions.collect {
    case ObjectTypeDefinition(description, name, implements, directives, fields)    => name
    case InterfaceTypeDefinition(description, name, implements, directives, fields) => name
  }
  private val enums   = doc.typeDefinitions.collect {
    case EnumTypeDefinition(description, name, directives, enumValuesDefinition) => name
  }
  private val scalars = doc.typeDefinitions.collect {
    case ScalarTypeDefinition(_, name, fields) => name
  }
  private val inputs  = doc.typeDefinitions.collect {
    case InputObjectTypeDefinition(_, name, _, fields) => name
  }

  private var backRefs = Map.empty[String, List[String]]

  private def addBackRef(backRefEntity: String, entity: String): Unit = {
    val curBackRefEntities = backRefs.getOrElse(entity, Nil)
    backRefs = backRefs + (entity -> (curBackRefEntities :+ backRefEntity))
  }

  //  println(s"types  : $types")
  //  println(s"enums  : $enums")
  //  println(s"scalars: $scalars")
  //  println(s"inputs : $inputs")


  // Build MetaDomain -----------------------------------------------------------------------

  def getMetaDomain: MetaDomain = {
    val entities = getEntities(doc)
    val segment  = MetaSegment("", addBackRefs(entities))
    val md       = MetaDomain(GraphQL, pkg, domain, maxArity, List(segment))
    //    println(md)
    md
  }

  private def addBackRefs(entities: List[MetaEntity]): List[MetaEntity] = {
    entities.map { entity =>
      entity.copy(backRefs = backRefs.getOrElse(entity.ent, Nil).distinct.sorted)
    }
  }

  private def getEntities(doc: Document): List[MetaEntity] = {
    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(description, name, implements, directives, fields) =>
        mkEntity(description, name, implements, directives, fields)

      //      case InputObjectTypeDefinition(_, name, _, fields)  => println("2 " + name + "\n    " + fields.map(_.name).mkString(", "))
      //      case EnumTypeDefinition(description, name, directives, enumValuesDefinition) =>
      //        println("3 " + name + "\n    " + enumValuesDefinition.map(_.enumValue).mkString(", "))
      //        mkEnum(description, name, directives, enumValuesDefinition)

      //      case UnionTypeDefinition(_, name, _, fields)        => println("4 " + name + "\n    " + fields.mkString(", "))
      //      case ScalarTypeDefinition(_, name, fields)          => println("5 " + name + "\n    " + fields.map(_.name).mkString(", "))

      case InterfaceTypeDefinition(description, name, implements, directives, fields) =>
        mkEntity(description, name, implements, directives, fields)
    }
  }

  private def mkEntity(
    description: Option[String],
    entity: String,
    implements: List[NamedType],
    directives: List[Directive],
    fields: List[FieldDefinition]
  ): MetaEntity = {
    val attributes     = fields.map(f => mkAttr(entity, f.description, f.name, f.args, f.ofType, f.directives))
    val mandatoryAttrs = fields.map(_.ofType).collect {
      case NamedType(name, true)             => name
      case ListType(NamedType(ref, _), true) => entity
    }
    //    val mandatoryRefs  = fields.map(_.ofType).collect {
    //      case NamedType(name, true)             => name
    //      case ListType(NamedType(ref, _), true) => name
    //    }

    //    println(s"implements: $implements")
    //    println(s"directives: $directives")

    MetaEntity(entity, attributes, Nil, mandatoryAttrs, Nil, description)
  }

  private def mkAttr(
    entity: String,
    description: Option[String],
    attr: String,
    args: List[InputValueDefinition],
    ofType: Type,
    directives: List[Directive]
  ): MetaAttribute = {
    def isRef(tpe: String) = types.contains(tpe)
    def getTpe(tpe: String): String = tpe match {
      case "ID"                   => "String"
      case t if enums.contains(t) => "String"
      case t if isRef(t)          =>

        "" // no ref attributes in graphql
      case t                      => t
    }
    ofType match {
      case NamedType(tpe, mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val ref  = Some(tpe).filter(types.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        MetaAttribute(attr, CardOne, getTpe(tpe), ref, opts)

      case ListType(NamedType(tpe, _), mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val ref  = Some(tpe).filter(types.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        val card = if (isRef(tpe)) CardSet else CardSeq
        MetaAttribute(attr, card, getTpe(tpe), ref, opts)

      case _ => throw ModelError(s"Unsupported type: $ofType")
    }
  }

  //  def mkEnum(
  //    description: Option[String],
  //    name: String,
  //    directives: List[Directive],
  //    enumValuesDefinition: List[EnumValueDefinition]
  //  ): MetaEnum = {
  //
  //
  //    MetaEnum(name, CardOne, "")
  //  }


}

