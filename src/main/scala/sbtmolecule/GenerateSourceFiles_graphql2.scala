package sbtmolecule

import java.io.File
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.*
import caliban.parsing.adt.Document
import sbt.*
import sbtmolecule.graphql.dsl.{GraphqlOutput, GraphqlQuery}

case class GenerateSourceFiles_graphql2(doc: Document, pkg: String, domain: String, maxArity: Int) {

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


//  private def withBackRefs(entities: List[MetaEntity]): List[MetaEntity] = {
//    doc.typeDefinitions.collect {
//      case ObjectTypeDefinition(_, entity, _, _, fields) if !reservedTypes.contains(entity) =>
//        entity
//        fields.foreach {
//          case FieldDefinition(_, _, _, ofType, _) =>
//
//        }
//
//
//      case InterfaceTypeDefinition(_, name, _, _, fields) if !reservedTypes.contains(name) => name
//      //    case InputObjectTypeDefinition(_, name, _, _) if !reservedTypes.contains(name)  => name
//      //    case ScalarTypeDefinition(_, name, _) if !reservedTypes.contains(name)          => name
//    }
//
//
//    // Add BackRefs once all entities are known
//    entities.map { entity =>
//      entity.copy(backRefs = backRefs.getOrElse(entity.ent, Nil).distinct.sorted)
//    }
//  }


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
          case `queryType`                   => GraphqlQuery(pkg, domain, typeNames, enumNames, descr, fields).get
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
        GraphqlOutput(pkg, domain, maxArity, typeNames, enumNames, name, descr, fields).get
    }
  }

  def printCode(name: String): Unit = {
    println(getCode(name).filterNot(_.isEmpty).mkString("\n===================\n\n"))
  }

}
