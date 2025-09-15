package sbtmolecule.db.resolvers

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import molecule.base.util.BaseHelpers.{indent, padS}


case class MetaDb_(metaDomain: MetaDomain) {
  val pkg    = metaDomain.pkg + ".dsl"
  val domain = metaDomain.domain


  def mandatoryAttrs: String = {
    val p        = indent(1)
    val pad      = s"\n$p  "
    val pairs0   = for {
      segment <- metaDomain.segments
      entity <- segment.entities
    } yield {
      (entity.entity, entity.mandatoryAttrs)
    }
    val entities = pairs0.filter(_._2.nonEmpty)
    val pairs    = if (entities.isEmpty) "" else {
      val maxEntity = entities.map(_._1.length).max
      entities.map { case (entity, mandatoryAttrs) =>
        val fullAttrs = mandatoryAttrs.map(attr => s"$entity.$attr").mkString("\", \"")
        s""""$entity"${padS(maxEntity, entity)} -> List("$fullAttrs")"""
      }.mkString(pad, s",$pad", s"\n$p")
    }
    s"Map($pairs)"
  }

  def mandatoryRefs: String = {
    val p        = indent(1)
    val pad      = s"\n$p  "
    val pairs0   = for {
      segment <- metaDomain.segments
      entity <- segment.entities
    } yield {
      (entity.entity, entity.mandatoryRefs)
    }
    val entities = pairs0.filter(_._2.nonEmpty)
    val pairs    = if (entities.isEmpty) "" else {
      val maxEntity = entities.map(_._1.length).max
      entities.map { case (entity, mandatoryRefs) =>
        val data = mandatoryRefs.map { case (attr, ref) =>
          s""""$entity.$attr" -> "$ref""""
        }
        s""""$entity"${padS(maxEntity, entity)} -> List(${data.mkString(", ")})"""
      }.mkString(pad, s",$pad", s"\n$p")
    }
    s"Map($pairs)"
  }

  def attrData: String = {
    val p           = indent(1)
    val pad         = s"\n$p  "
    val attrData    = for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attr <- entity.attributes
    } yield {
      (s"${entity.entity}.${attr.attribute}", attr.value, attr.baseTpe, attr.requiredAttrs)
    }
    val maxEntity   = attrData.map(_._1.length).max
    val maxBaseType = attrData.map(_._3.length).max
    val attrs       = attrData.map {
      case (a, value, tpe, reqAttrs) =>
        val reqAttrsStr = reqAttrs.map(a => s""""$a"""").mkString(", ")
        s""""$a"${padS(maxEntity, a)} -> ($value, "$tpe"${padS(maxBaseType, tpe)}, List($reqAttrsStr))"""
    }
    val attrsStr    = if (attrs.isEmpty) "" else attrs.mkString(pad, s",$pad", s"\n$p")
    s"Map($attrsStr)"
  }

  def uniqueAttrs: String = {
    val attrs    = for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attr <- entity.attributes if attr.options.contains("unique")
    } yield {
      s""""${entity.entity}.${attr.attribute}""""
    }
    val attrsStr = if (attrs.isEmpty) "" else attrs.mkString("\n    ", s",\n    ", s"\n  ")
    s"List($attrsStr)"
  }


  def getMeta: String =
    s"""|// AUTO-GENERATED Molecule boilerplate code
        |package $pkg.$domain.metadb
        |
        |import molecule.core.dataModel.*
        |import molecule.db.common.api.MetaDb
        |
        |trait ${domain}_ extends MetaDb {
        |
        |  /** entity -> List[mandatory-attribute] */
        |  val mandatoryAttrs: Map[String, List[String]] = $mandatoryAttrs
        |
        |  /** entity -> List[(entity.attr, mandatory refEntity)] */
        |  val mandatoryRefs: Map[String, List[(String, String)]] = $mandatoryRefs
        |
        |  /** attr -> (Value, Scala type, required attributes) */
        |  val attrData: Map[String, (Value, String, List[String])] = $attrData
        |
        |  /** Attributes requiring unique values */
        |  val uniqueAttrs: List[String] = $uniqueAttrs
        |}""".stripMargin
}
