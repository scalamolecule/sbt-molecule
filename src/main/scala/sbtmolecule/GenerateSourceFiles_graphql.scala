package sbtmolecule

import java.io.File
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.*
import caliban.parsing.adt.Type.{ListType, NamedType}
import caliban.parsing.adt.{Directive, Document, Type}
import molecule.base.metaModel.*
import sbt.*
import sbtmolecule.graphql.dsl.GraphqlQuery

case class GenerateSourceFiles_graphql(doc: Document, pkg: String, domain: String, maxArity: Int) {

  private lazy val (queryType, mutationType, subscriptionType) = doc.schemaDefinition.fold(
    ("Query", "Mutation", "Subscription")
  )(schema =>
    (
      schema.query.getOrElse("Query"),
      schema.mutation.getOrElse("Mutation"),
      schema.subscription.getOrElse("Subscription")
    )
  )

  private lazy val reservedTypes = List(queryType, mutationType, subscriptionType)

  private lazy val typeNames: List[String] = doc.typeDefinitions.collect {
    case ObjectTypeDefinition(_, name, _, _, _) if !reservedTypes.contains(name)    => name
    case InterfaceTypeDefinition(_, name, _, _, _) if !reservedTypes.contains(name) => name
    //    case InputObjectTypeDefinition(_, name, _, _) if !reservedTypes.contains(name)  => name
    //    case ScalarTypeDefinition(_, name, _) if !reservedTypes.contains(name)          => name
  }

  private lazy val enumNames: List[String] = doc.typeDefinitions.collect {
    case EnumTypeDefinition(_, name, _, _) => name
  }

  private lazy val scalarNames: List[String] = doc.typeDefinitions.collect {
    case ScalarTypeDefinition(_, name, _) => name
  }

  private lazy val inputNames: List[String] = doc.typeDefinitions.collect {
    case InputObjectTypeDefinition(_, name, _, _) => name
  }

  private var backRefs = Map.empty[String, List[String]]
  private def addBackRef(backRefEntity: String, entity: String): Unit = {
    val curBackRefEntities = backRefs.getOrElse(entity, Nil)
    backRefs = backRefs + (entity -> (curBackRefEntities :+ backRefEntity))
  }


  def generate(domainDir: File): Unit = {
    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(descr, name, _, _, fields)            =>
        name match {
          case `queryType` =>
            val code = GraphqlQuery(pkg, domain, typeNames, enumNames, descr, fields).get
            IO.write(domainDir / "query.scala", code)

          case `mutationType`                => ""
          case `subscriptionType`            => ""
          case _ if name.endsWith("Input")   => ""
          case _ if name.endsWith("Payload") => ""
          case _                             => ""
        }
      case InputObjectTypeDefinition(descr, name, directives, fields) => ""

      case EnumTypeDefinition(descr, name, directives, enumValuesDefinition) =>
        ""

      case ScalarTypeDefinition(descr, name, directives) =>
        ""

      case InterfaceTypeDefinition(descr, name, _, directives, fields) =>
        ""
    }
  }

  def getCode(name: String): List[String] = {
    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(descr, `name`, _, _, fields) =>
        name match {
          case `queryType`                   =>

            GraphqlQuery(pkg, domain, typeNames, enumNames, descr, fields).get


          case `mutationType`                => ""
          case `subscriptionType`            => ""
          case _ if name.endsWith("Input")   => ""
          case _ if name.endsWith("Payload") => ""
          case _                             => name
        }

      case InputObjectTypeDefinition(descr, `name`, _, fields) =>
        ""

      case EnumTypeDefinition(descr, `name`, _, enumValuesDefinition) =>
        s"""enum $name:
           |  ${enumValuesDefinition.map(_.enumValue).mkString(", ")}
           |""".stripMargin

      case ScalarTypeDefinition(descr, `name`, _) =>
        ""

      case InterfaceTypeDefinition(descr, `name`, _, _, fields) =>
//        GraphqlOutput(pkg, domain, maxArity, typeNames, enumNames, name, descr, fields).get
//        GraphqlOutput(metaDomain, metaEntity)
        ???
    }
  }

  def printCode(name: String): Unit = {
    println(getCode(name).filterNot(_.isEmpty).mkString("\n===================\n\n"))
  }

  // Collect MetaDomain elements ---------------------------------

  private var inputs  = List.empty[MetaEntity]
  private var outputs = List.empty[MetaEntity]

  // Leaf
  private var enums   = List.empty[MetaEntity]
  private var scalars = List.empty[MetaEntity]

  // Root types
  private var queryMethods        = List.empty[MetaEntity]
  private var mutationMethods     = List.empty[MetaEntity]
  private var subscriptionMethods = List.empty[MetaEntity]

  // Relay types by convention
  private var paginations = List.empty[MetaEntity] // Connection, Edge
  private var payloads    = List.empty[MetaEntity]


  //  println(s"types  : $types")
  //  println(s"enums  : $enums")
  //  println(s"scalars: $scalars")
  //  println(s"inputs : $inputs")


  // Build MetaDomain -----------------------------------------------------------------------

  def getMetaDomain: MetaDomain = {
    mkMetaModel(doc)
    val input  = if (inputs.nonEmpty) Some(MetaSegment("input", inputs)) else None
    val output = if (outputs.nonEmpty) Some(MetaSegment("output", withBackRefs(outputs))) else None
    val enum   = if (enums.nonEmpty) Some(MetaSegment("enum", enums)) else None
    val scalar = if (scalars.nonEmpty) Some(MetaSegment("scalar", scalars)) else None

    val query = Some(MetaSegment(s"query $queryType", queryMethods))

    val mutation = if (mutationMethods.nonEmpty)
      Some(MetaSegment(s"mutation $mutationType", mutationMethods))
    else None

    val subscription = if (subscriptionMethods.nonEmpty)
      Some(MetaSegment(s"subscription $subscriptionType", subscriptionMethods))
    else None

    val pagination = if (paginations.nonEmpty) Some(MetaSegment("pagination", paginations)) else None
    val payload    = if (payloads.nonEmpty) Some(MetaSegment("payload", payloads)) else None

    val segments = List(input, output, enum, scalar, query, mutation, subscription, pagination, payload).flatten
    val md       = MetaDomain(pkg, domain, maxArity, segments)
    println(md)
    md
  }

  private def withBackRefs(entities: List[MetaEntity]): List[MetaEntity] = {
    // Add BackRefs once all entities are known
    entities.map { entity =>
      entity.copy(backRefs = backRefs.getOrElse(entity.entity, Nil).distinct.sorted)
    }
  }

  private def mkMetaModel(doc: Document): Unit = {
    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(description, name, _, directives, fields) =>
        name match {
          case `queryType` =>
            //            val code = GraphqlQuery(doc, pkg, domain, maxArity).get
            //            IO.write(domainDir / "query.scala", code)


            queryMethods = mkRootMethods(description, directives, fields)

          case `mutationType` =>
            mutationMethods = mkRootMethods(description, directives, fields)

          case `subscriptionType` =>
            subscriptionMethods = mkRootMethods(description, directives, fields)

          case _ if name.endsWith("Input") =>
            paginations = paginations :+ mkOutput(description, name, directives, fields)

          case _ if name.endsWith("Payload") =>
            payloads = payloads :+ mkOutput(description, name, directives, fields)

          case _ =>
            outputs = outputs :+ mkOutput(description, name, directives, fields)
        }

      case InputObjectTypeDefinition(description, name, directives, fields) =>
        inputs = inputs :+ mkInput(description, name, directives, fields)

      case EnumTypeDefinition(description, name, directives, enumValuesDefinition) =>
        enums = enums :+ mkEnum(description, name, directives, enumValuesDefinition)

      case ScalarTypeDefinition(description, name, directives) =>
        scalars = scalars :+ mkScalar(description, name, directives)

      case InterfaceTypeDefinition(description, name, _, directives, fields) =>
        outputs = outputs :+ mkOutput(description, name, directives, fields)
    }
  }

  private def mkOutput(
    description: Option[String],
    name: String,
    directives: List[Directive],
    fields: List[FieldDefinition]
  ): MetaEntity = {
    val attributes     = fields.map(f =>
      mkAttr(name, f.description, f.name, f.args, f.ofType, f.directives)
    )
    val mandatoryAttrs = fields.map(_.ofType).collect {
      case NamedType(name, true)             => name
      case ListType(NamedType(ref, _), true) => name
    }
    MetaEntity(name, attributes, Nil, mandatoryAttrs, Nil, description)
  }

  private def mkRootMethods(
    description: Option[String],
    //    queryType: String,
    directives: List[Directive],
    fields: List[FieldDefinition]
  ): List[MetaEntity] = {
    fields.map {
      case FieldDefinition(description, name, args, ofType, directives) =>
        val attrs = args.map {
          case InputValueDefinition(description, attr, ofType, defaultValue, directives) =>
            ofType match {
              case NamedType(tpe, mandatory) =>
                val ref  = Some(tpe).filter(typeNames.contains)
                val opts = if (mandatory) List("mandatory") else Nil
                MetaAttribute(attr, CardOne, getTpe(tpe), Nil, ref, options = opts)

              case ListType(NamedType(tpe, _), mandatory) =>
                val ref  = Some(tpe).filter(typeNames.contains)
                val opts = if (mandatory) List("mandatory") else Nil
                val card = if (isRef(tpe)) CardSet else CardSeq
                MetaAttribute(attr, card, getTpe(tpe), Nil, ref, options = opts)

              case _ => throw new Exception(s"Unsupported type: $ofType")
            }


          //            MetaAttribute(name, CardOne, ofType.toString, description = description)
        }
        MetaEntity(name, attrs, List(), Nil, Nil, description)
    }
  }


  private def mkInput(
    description: Option[String],
    name: String,
    directives: List[Directive],
    fields: List[InputValueDefinition]
  ): MetaEntity = {
    val args = fields.map {
      case InputValueDefinition(description, name, ofType, defaultValue, directives) =>
        MetaAttribute(name, CardOne, ofType.toString, description = description)
    }
    MetaEntity(name, args, Nil, Nil, Nil, description)
  }

  private def mkEnum(
    description: Option[String],
    name: String,
    directives: List[Directive],
    enumValuesDefinition: List[EnumValueDefinition]
  ): MetaEntity = {
    val enumValues = enumValuesDefinition.map(e => MetaAttribute(e.enumValue, CardOne, "String"))
    MetaEntity(name, enumValues, Nil, Nil, Nil, description)
  }

  private def mkScalar(
    description: Option[String],
    tpe: String,
    directives: List[Directive]
  ): MetaEntity = {
    val guessedType = tpe match {
      case "Date"                              => "java.util.Date"
      case "UUID"                              => "java.util.UUID"
      case "DateTime"                          => "java.time.LocalDateTime"
      case "LocalDateTime"                     => "java.time.LocalDateTime"
      case _ if tpe.toLowerCase.endsWith("id") => "String"
      case unresolved                          => unresolved
    }
    val attr        = MetaAttribute("", CardOne, guessedType)
    MetaEntity(tpe, List(attr), description = description)
  }

  private def mkAttr(
    entity: String,
    description: Option[String],
    attr: String,
    args: List[InputValueDefinition],
    ofType: Type,
    directives: List[Directive]
  ): MetaAttribute = {
    // todo: enum
    // todo: args

    ofType match {
      case NamedType(tpe, mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val optRef  = Some(tpe).filter(typeNames.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        MetaAttribute(attr, CardOne, getTpe(tpe), Nil, optRef, options = opts)

      case ListType(NamedType(tpe, _), mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val optRef  = Some(tpe).filter(typeNames.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        val card = if (isRef(tpe)) CardSet else CardSeq
        MetaAttribute(attr, card, getTpe(tpe), Nil, optRef, options = opts)

      case _ => throw new Exception(s"Unsupported type: $ofType")
    }
  }

  private def isRef(tpe: String) = typeNames.contains(tpe)

  private def getTpe(tpe: String): String = tpe match {
    case "ID"                       => "String"
    case t if enumNames.contains(t) => "String"
    case t if isRef(t)              => "" // no ref attributes in graphql
    case t                          => t
  }
}
