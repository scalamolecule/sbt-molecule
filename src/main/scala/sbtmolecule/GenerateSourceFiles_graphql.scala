package sbtmolecule

import java.io.File
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.*
import caliban.parsing.adt.Type.{ListType, NamedType}
import caliban.parsing.adt.{Directive, Document, Type}
import molecule.core.model.*
import sbt.*
import sbtmolecule.graphql.dsl.GraphqlQuery

case class GenerateSourceFiles_graphql(

  doc: Document,
  pkg: String,
  domain: String,
  maxArity: Int
) {

  private val (queryType, mutationType, subscriptionType) = doc.schemaDefinition.fold(
    ("Query", "Mutation", "Subscription")
  )(schema =>
    (
      schema.query.getOrElse("Query"),
      schema.mutation.getOrElse("Mutation"),
      schema.subscription.getOrElse("Subscription")
    )
  )
  private val reservedTypes                               = List(queryType, mutationType, subscriptionType)

  private val typeNames   = doc.typeDefinitions.collect {
    case ObjectTypeDefinition(_, name, _, _, _) if !reservedTypes.contains(name)    => name
    case InterfaceTypeDefinition(_, name, _, _, _) if !reservedTypes.contains(name) => name
    //    case InputObjectTypeDefinition(_, name, _, _) if !reservedTypes.contains(name)  => name
    //    case ScalarTypeDefinition(_, name, _) if !reservedTypes.contains(name)          => name
  }
  private val enumNames   = doc.typeDefinitions.collect {
    case EnumTypeDefinition(_, name, _, _) => name
  }
  private val scalarNames = doc.typeDefinitions.collect {
    case ScalarTypeDefinition(_, name, _) => name
  }
  private val inputNames  = doc.typeDefinitions.collect {
    case InputObjectTypeDefinition(_, name, _, _) => name
  }

  private var backRefs = Map.empty[String, List[String]]
  private def addBackRef(backRefEntity: String, entity: String): Unit = {
    val curBackRefEntities = backRefs.getOrElse(entity, Nil)
    backRefs = backRefs + (entity -> (curBackRefEntities :+ backRefEntity))
  }

  def generate(domainDir: File): Unit = {

//    val query = doc.queryDefinitions
//    val code = GraphqlQuery(pkg, domain, typeNames, descr, fields).get
//    IO.write(domainDir / "query.scala", code)

    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(descr, name, _, _, fields) =>
        name match {
          case `queryType` =>
            val code = GraphqlQuery(pkg, domain, typeNames, descr, fields).get
            IO.write(domainDir / "query.scala", code)

          //          case `mutationType` =>
          //            mutationMethods = mkRootMethods(description, directives, fields)
          //
          //          case `subscriptionType` =>
          //            subscriptionMethods = mkRootMethods(description, directives, fields)
          //
          //          case _ if name.endsWith("Input") =>
          //            paginations = paginations :+ mkOutput(description, name, directives, fields)
          //
          //          case _ if name.endsWith("Payload") =>
          //            payloads = payloads :+ mkOutput(description, name, directives, fields)
          //
          //          case _ =>
          //            outputs = outputs :+ mkOutput(description, name, directives, fields)
        }

      //      case InputObjectTypeDefinition(description, name, directives, fields) =>
      //        inputs = inputs :+ mkInput(description, name, directives, fields)
      //
      //      case EnumTypeDefinition(description, name, directives, enumValuesDefinition) =>
      //        enums = enums :+ mkEnum(description, name, directives, enumValuesDefinition)
      //
      //      case ScalarTypeDefinition(description, name, directives) =>
      //        scalars = scalars :+ mkScalar(description, name, directives)
      //
      //      case InterfaceTypeDefinition(description, name, _, directives, fields) =>
      //        outputs = outputs :+ mkOutput(description, name, directives, fields)
    }
  }

  def getCode(target: String): List[String] = {

    doc.typeDefinitions.collect {
      case ObjectTypeDefinition(descr, `target`, _, _, fields) =>
        target match {
          case `queryType`  =>
            GraphqlQuery(pkg, domain, typeNames, descr, fields).get

          //          case `mutationType` =>
          //            mutationMethods = mkRootMethods(description, directives, fields)
          //
          //          case `subscriptionType` =>
          //            subscriptionMethods = mkRootMethods(description, directives, fields)
          //
          //          case _ if name.endsWith("Input") =>
          //            paginations = paginations :+ mkOutput(description, name, directives, fields)
          //
          //          case _ if name.endsWith("Payload") =>
          //            payloads = payloads :+ mkOutput(description, name, directives, fields)
          //
          case _ =>
            //                      outputs = outputs :+ mkOutput(description, name, directives, fields)
            "not found..."
        }

      //      case InputObjectTypeDefinition(description, name, directives, fields) =>
      //        inputs = inputs :+ mkInput(description, name, directives, fields)
      //
      //      case EnumTypeDefinition(description, name, directives, enumValuesDefinition) =>
      //        enums = enums :+ mkEnum(description, name, directives, enumValuesDefinition)
      //
      //      case ScalarTypeDefinition(description, name, directives) =>
      //        scalars = scalars :+ mkScalar(description, name, directives)
      //
      //      case InterfaceTypeDefinition(description, name, _, directives, fields) =>
      //        outputs = outputs :+ mkOutput(description, name, directives, fields)
    }
  }




  // Collect DbModel elements ---------------------------------

  private var inputs  = List.empty[DbEntity]
  private var outputs = List.empty[DbEntity]

  // Leaf
  private var enums   = List.empty[DbEntity]
  private var scalars = List.empty[DbEntity]

  // Root types
  private var queryMethods        = List.empty[DbEntity]
  private var mutationMethods     = List.empty[DbEntity]
  private var subscriptionMethods = List.empty[DbEntity]

  // Relay types by convention
  private var paginations = List.empty[DbEntity] // Connection, Edge
  private var payloads    = List.empty[DbEntity]


  //  println(s"types  : $types")
  //  println(s"enums  : $enums")
  //  println(s"scalars: $scalars")
  //  println(s"inputs : $inputs")


  // Build DbModel -----------------------------------------------------------------------

  def getDbModel: DbModel = {
    handleTypeDefinitions(doc)
    val input  = if (inputs.nonEmpty) Some(DbSegment("input", inputs)) else None
    val output = if (outputs.nonEmpty) Some(DbSegment("output", withBackRefs(outputs))) else None
    val enum   = if (enums.nonEmpty) Some(DbSegment("enum", enums)) else None
    val scalar = if (scalars.nonEmpty) Some(DbSegment("scalar", scalars)) else None

    val query = Some(DbSegment(s"query $queryType", queryMethods))

    val mutation = if (mutationMethods.nonEmpty) {
      Some(DbSegment(s"mutation $mutationType", mutationMethods))
    } else None

    val subscription = if (subscriptionMethods.nonEmpty) {
      Some(DbSegment(s"subscription $subscriptionType", subscriptionMethods))
    } else None

    val pagination = if (paginations.nonEmpty) Some(DbSegment("pagination", paginations)) else None
    val payload    = if (payloads.nonEmpty) Some(DbSegment("payload", payloads)) else None

    val segments = List(input, output, enum, scalar, query, mutation, subscription, pagination, payload).flatten
    val md       = DbModel(pkg, domain, maxArity, segments)
    println(md)
    md
  }

  private def withBackRefs(entities: List[DbEntity]): List[DbEntity] = {
    // Add BackRefs once all entities are known
    entities.map { entity =>
      entity.copy(backRefs = backRefs.getOrElse(entity.ent, Nil).distinct.sorted)
    }
  }

  private def handleTypeDefinitions(doc: Document): Unit = {
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
  ): DbEntity = {
    val attributes     = fields.map(f =>
      mkAttr(name, f.description, f.name, f.args, f.ofType, f.directives)
    )
    val mandatoryAttrs = fields.map(_.ofType).collect {
      case NamedType(name, true)             => name
      case ListType(NamedType(ref, _), true) => name
    }
    DbEntity(name, attributes, Nil, mandatoryAttrs, Nil, description)
  }

  private def mkRootMethods(
    description: Option[String],
    //    queryType: String,
    directives: List[Directive],
    fields: List[FieldDefinition]
  ): List[DbEntity] = {
    fields.map {
      case FieldDefinition(description, name, args, ofType, directives) =>
        val attrs = args.map {
          case InputValueDefinition(description, attr, ofType, defaultValue, directives) =>
            ofType match {
              case NamedType(tpe, mandatory) =>
                val ref  = Some(tpe).filter(typeNames.contains)
                val opts = if (mandatory) List("mandatory") else Nil
                DbAttribute(attr, CardOne, getTpe(tpe), ref, opts)

              case ListType(NamedType(tpe, _), mandatory) =>
                val ref  = Some(tpe).filter(typeNames.contains)
                val opts = if (mandatory) List("mandatory") else Nil
                val card = if (isRef(tpe)) CardSet else CardSeq
                DbAttribute(attr, card, getTpe(tpe), ref, opts)

              case _ => throw new Exception(s"Unsupported type: $ofType")
            }


          //            DbAttribute(name, CardOne, ofType.toString, description = description)
        }
        DbEntity(name, attrs, List(), Nil, Nil, description)
    }
  }


  private def mkInput(
    description: Option[String],
    name: String,
    directives: List[Directive],
    fields: List[InputValueDefinition]
  ): DbEntity = {
    val args = fields.map {
      case InputValueDefinition(description, name, ofType, defaultValue, directives) =>
        DbAttribute(name, CardOne, ofType.toString, description = description)
    }
    DbEntity(name, args, Nil, Nil, Nil, description)
  }

  private def mkEnum(
    description: Option[String],
    name: String,
    directives: List[Directive],
    enumValuesDefinition: List[EnumValueDefinition]
  ): DbEntity = {
    val enumValues = enumValuesDefinition.map(e => DbAttribute(e.enumValue, CardOne, "String"))
    DbEntity(name, enumValues, Nil, Nil, Nil, description)
  }

  private def mkScalar(
    description: Option[String],
    tpe: String,
    directives: List[Directive]
  ): DbEntity = {
    val guessedType = tpe match {
      case "Date"                              => "java.util.Date"
      case "UUID"                              => "java.util.UUID"
      case "DateTime"                          => "java.time.LocalDateTime"
      case "LocalDateTime"                     => "java.time.LocalDateTime"
      case _ if tpe.toLowerCase.endsWith("id") => "String"
      case unresolved                          => unresolved
    }
    val attr        = DbAttribute("", CardOne, guessedType)
    DbEntity(tpe, List(attr), description = description)
  }

  private def mkAttr(
    entity: String,
    description: Option[String],
    attr: String,
    args: List[InputValueDefinition],
    ofType: Type,
    directives: List[Directive]
  ): DbAttribute = {
    // todo: args...?

    ofType match {
      case NamedType(tpe, mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val ref  = Some(tpe).filter(typeNames.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        DbAttribute(attr, CardOne, getTpe(tpe), ref, opts)

      case ListType(NamedType(tpe, _), mandatory) =>
        if (isRef(tpe)) addBackRef(entity, tpe)
        val ref  = Some(tpe).filter(typeNames.contains)
        val opts = if (mandatory) List("mandatory") else Nil
        val card = if (isRef(tpe)) CardSet else CardSeq
        DbAttribute(attr, card, getTpe(tpe), ref, opts)

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
