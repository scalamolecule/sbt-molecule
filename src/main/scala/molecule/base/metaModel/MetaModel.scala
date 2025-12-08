package molecule.base.metaModel

import molecule.core.dataModel.*
import molecule.base.util.BaseHelpers._

case class MetaDomain(
  pkg: String,
  domain: String,
  segments: List[MetaSegment],
  roles: List[MetaRole] = Nil, // Role definitions
  generalDbColumnProps: Map[String, Map[String, String]] = Map.empty // Map(db -> Map(baseTpe -> columnTypeDefinition))
) {
  def render(tabs: Int = 0): String = {
    val p           = indent(tabs)
    val pad         = s"\n$p  "
    val segmentsStr = if (segments.isEmpty) "" else
      segments.map(_.render(tabs + 1)).mkString(pad, s",\n\n$pad", s"\n$p")
    val rolesStr    = if (roles.isEmpty) "" else
      roles.map(_.toString).mkString(pad, s",$pad", s"\n$p")
    val generalPropsStr = if (generalDbColumnProps.isEmpty) "Map.empty" else {
      generalDbColumnProps.map { case (db, typeMap) =>
        val innerMap = typeMap.map { case (tpe, colDef) => s""""$tpe" -> "$colDef"""" }.mkString("Map(", ", ", ")")
        s""""$db" -> $innerMap"""
      }.mkString("Map(", ", ", ")")
    }
    s"""MetaDomain("$pkg", "$domain", List($segmentsStr), List($rolesStr), $generalPropsStr)"""
  }

  override def toString: String = render()
}


case class MetaSegment(
  segment: String,
  entities: List[MetaEntity]
) {
  def render(tabs: Int): String = {
    val p           = indent(tabs)
    val pad         = s"\n$p  "
    val entitiesStr = if (entities.isEmpty) "" else
      entities.map(_.render(tabs + 1)).mkString(pad, s",\n$pad", s"\n$p")
    s"""MetaSegment("$segment", List($entitiesStr))"""
  }

  override def toString: String = render(0)
}


case class MetaEntity(
  entity: String,
  attributes: List[MetaAttribute],
  backRefs: List[String] = Nil,
  mandatoryAttrs: List[String] = Nil,
  mandatoryRefs: List[(String, String)] = Nil,
  isJoinTable: Boolean = false,
  description: Option[String] = None,
  // Access control
  entityRoles: List[String] = Nil, // Layer 1: Roles this entity extends (empty = public)
  entityActions: List[String] = Nil, // Actions from role definitions
  entityUpdatingGrants: List[String] = Nil, // Layer 2: Action grants via `with updating[R]`
  entityDeletingGrants: List[String] = Nil, // Layer 2: Action grants via `with deleting[R]`
) {
  def render(tabs: Int): String = {
    val attrsStr          = if (attributes.isEmpty) "" else {
      val maxAttr = attributes.map(_.attribute.length).max
      val maxTpe  = attributes.map(_.baseTpe.length).max
      val p       = indent(tabs)
      val pad     = s"\n$p  "
      attributes.map { attr =>
        val attr1             = "\"" + attr.attribute + "\"" + padS(maxAttr, attr.attribute)
        val value             = attr.value
        val tpe               = "\"" + attr.baseTpe + "\"" + padS(maxTpe, attr.baseTpe)
        val args              = list(attr.arguments)
        val ref               = o(attr.ref)
        val reverseRef        = o(attr.reverseRef)
        val relationship      = o(attr.relationship)
        val enumTpe           = o(attr.enumTpe)
        val options           = list(attr.options)
        val descr             = o(attr.description)
        val alias             = o(attr.alias)
        val requiredAttrs     = list(attr.requiredAttrs)
        val valueAttrs        = list(attr.valueAttrs)
        val validations1      = renderValidations(attr.validations)
        val onlyRoles1        = list(attr.onlyRoles)
        val excludedRoles1    = list(attr.excludedRoles)
        val attrUpdatingGrants1 = list(attr.attrUpdatingGrants)
        s"""MetaAttribute($attr1, $value, $tpe, $args, $ref, $reverseRef, $relationship, $enumTpe, $options, $alias, $requiredAttrs, $valueAttrs, $validations1, $descr, $onlyRoles1, $excludedRoles1, $attrUpdatingGrants1)"""
      }.mkString(pad, s",$pad", s"\n$p")
    }
    val backRefs1         = if (backRefs.isEmpty) "" else backRefs.mkString("\"", "\", \"", "\"")
    val mandatoryAttrsStr = if (mandatoryAttrs.isEmpty) "" else mandatoryAttrs.mkString("\"", "\", \"", "\"")
    val mandatoryRefsStr  = if (mandatoryRefs.isEmpty) "" else mandatoryRefs.map {
      case (attr, ref) => s"""\"$attr\" -> \"$ref\""""
    }.mkString(", ")
    val entityRolesStr    = if (entityRoles.isEmpty) "" else entityRoles.mkString("\"", "\", \"", "\"")
    val entityActionsStr  = if (entityActions.isEmpty) "" else entityActions.mkString("\"", "\", \"", "\"")
    val entityUpdatingGrantsStr = if (entityUpdatingGrants.isEmpty) "" else entityUpdatingGrants.mkString("\"", "\", \"", "\"")
    val entityDeletingGrantsStr = if (entityDeletingGrants.isEmpty) "" else entityDeletingGrants.mkString("\"", "\", \"", "\"")
    s"""MetaEntity("$entity", List($attrsStr), List($backRefs1), List($mandatoryAttrsStr), List($mandatoryRefsStr), $isJoinTable, ${o(description)}, List($entityRolesStr), List($entityActionsStr), List($entityUpdatingGrantsStr), List($entityDeletingGrantsStr))"""
  }

  override def toString: String = render(0)
}


case class MetaAttribute(
  attribute: String,
  value: Value,
  baseTpe: String,
  arguments: List[MetaArgument] = Nil,
  ref: Option[String] = None,
  reverseRef: Option[String] = None,
  relationship: Option[Relationship] = None,
  enumTpe: Option[String] = None,
  options: List[String] = Nil,
  alias: Option[String] = None,
  requiredAttrs: List[String] = Nil,
  valueAttrs: List[String] = Nil,
  validations: List[(String, String)] = Nil,
  description: Option[String] = None,
  // Access control - New model (Layer 3 & 4)
  onlyRoles: List[String] = Nil, // Layer 3: Attribute restrictions via .only[R]
  excludedRoles: List[String] = Nil, // Layer 3: Attribute restrictions via .exclude[R]
  attrUpdatingGrants: List[String] = Nil, // Layer 4: Attribute update grants via .updating[R]
  // Custom database column properties
  dbColumnProps: Map[String, String] = Map.empty, // Map(db -> columnTypeDefinition)
) {
  override def toString: String = {
    val validations1           = renderValidations(validations)
    val onlyRolesStr           = if (onlyRoles.isEmpty) "" else onlyRoles.mkString("\"", "\", \"", "\"")
    val excludedRolesStr       = if (excludedRoles.isEmpty) "" else excludedRoles.mkString("\"", "\", \"", "\"")
    val attrUpdatingGrantsStr  = if (attrUpdatingGrants.isEmpty) "" else attrUpdatingGrants.mkString("\"", "\", \"", "\"")
    val dbColumnPropsStr       = if (dbColumnProps.isEmpty) "Map.empty" else dbColumnProps.map { case (k, v) => s""""$k" -> "$v"""" }.mkString("Map(", ", ", ")")
    s"""MetaAttribute("$attribute", $value, "$baseTpe", ${list(arguments)}, ${o(ref)}, ${o(reverseRef)}, ${o(relationship)}, ${o(enumTpe)}, ${list(options)}, ${o(alias)}, ${list(requiredAttrs)}, ${list(valueAttrs)}, $validations1, ${o(description)}, List($onlyRolesStr), List($excludedRolesStr), List($attrUpdatingGrantsStr), $dbColumnPropsStr)"""
  }
}

case class MetaArgument(
  argument: String,
  value: Value,
  baseTpe: String,
  mandatory: Boolean = false,
  defaultValue: Option[String] = None, // All values added as String
  description: Option[String] = None,
) {
  override def toString: String = {
    s"""MetaArgument("$argument", $value, "$baseTpe", $mandatory, ${o(defaultValue)}, ${o(description)})"""
  }
}

/** Role definition with associated actions */
case class MetaRole(
  role: String,
  actions: List[String] = Nil // Actions this role has (query, subscribe, save, etc.)
) {
  override def toString: String = {
    val actionsStr = if (actions.isEmpty) "" else actions.mkString("\"", "\", \"", "\"")
    s"""MetaRole("$role", List($actionsStr))"""
  }
}


