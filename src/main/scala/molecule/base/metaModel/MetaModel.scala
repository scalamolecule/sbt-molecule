package molecule.base.metaModel

import molecule.core.dataModel.*
import molecule.base.util.BaseHelpers._

case class MetaDomain(
  pkg: String,
  domain: String,
  segments: List[MetaSegment],
  roles: List[MetaRole] = Nil  // Role definitions
) {
  def render(tabs: Int = 0): String = {
    val p           = indent(tabs)
    val pad         = s"\n$p  "
    val segmentsStr = if (segments.isEmpty) "" else
      segments.map(_.render(tabs + 1)).mkString(pad, s",\n\n$pad", s"\n$p")
    val rolesStr    = if (roles.isEmpty) "" else
      roles.map(_.toString).mkString(pad, s",$pad", s"\n$p")
    s"""MetaDomain("$pkg", "$domain", List($segmentsStr), List($rolesStr))"""
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
  entityRoles: List[String] = Nil,          // Roles this entity extends (empty = public)
  entityActions: List[String] = Nil,        // Actions from role definitions
  isAuthenticated: Boolean = false          // Whether entity extends Authenticated
) {
  def render(tabs: Int): String = {
    val attrsStr          = if (attributes.isEmpty) "" else {
      val maxAttr = attributes.map(_.attribute.length).max
      val maxTpe  = attributes.map(_.baseTpe.length).max
      val p       = indent(tabs)
      val pad     = s"\n$p  "
      attributes.map { attr =>
        val attr1         = "\"" + attr.attribute + "\"" + padS(maxAttr, attr.attribute)
        val value         = attr.value
        val tpe           = "\"" + attr.baseTpe + "\"" + padS(maxTpe, attr.baseTpe)
        val args          = list(attr.arguments)
        val ref           = o(attr.ref)
        val reverseRef    = o(attr.reverseRef)
        val relationship  = o(attr.relationship)
        val enumTpe       = o(attr.enumTpe)
        val options       = list(attr.options)
        val descr         = o(attr.description)
        val alias         = o(attr.alias)
        val requiredAttrs = list(attr.requiredAttrs)
        val valueAttrs    = list(attr.valueAttrs)
        val validations1  = renderValidations(attr.validations)
        val allowRoles1   = list(attr.allowRoles)
        val allowActions1 = list(attr.allowActions)
        val allowRoleActions1 = if (attr.allowRoleActions.isEmpty) "Nil" else {
          attr.allowRoleActions.map { case (roles, actions) =>
            s"(${list(roles)}, ${list(actions)})"
          }.mkString("List(", ", ", ")")
        }
        val isAuth        = attr.isAuthenticated
        s"""MetaAttribute($attr1, $value, $tpe, $args, $ref, $reverseRef, $relationship, $enumTpe, $options, $alias, $requiredAttrs, $valueAttrs, $validations1, $descr, $allowRoles1, $allowActions1, $allowRoleActions1, $isAuth)"""
      }.mkString(pad, s",$pad", s"\n$p")
    }
    val backRefs1         = if (backRefs.isEmpty) "" else backRefs.mkString("\"", "\", \"", "\"")
    val mandatoryAttrsStr = if (mandatoryAttrs.isEmpty) "" else mandatoryAttrs.mkString("\"", "\", \"", "\"")
    val mandatoryRefsStr  = if (mandatoryRefs.isEmpty) "" else mandatoryRefs.map {
      case (attr, ref) => s"""\"$attr\" -> \"$ref\""""
    }.mkString(", ")
    val entityRolesStr    = if (entityRoles.isEmpty) "" else entityRoles.mkString("\"", "\", \"", "\"")
    val entityActionsStr  = if (entityActions.isEmpty) "" else entityActions.mkString("\"", "\", \"", "\"")
    s"""MetaEntity("$entity", List($attrsStr), List($backRefs1), List($mandatoryAttrsStr), List($mandatoryRefsStr), $isJoinTable, ${o(description)}, List($entityRolesStr), List($entityActionsStr), $isAuthenticated)"""
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
  // Access control
  allowRoles: List[String] = Nil,                          // Roles specified via .allowRoles
  allowActions: List[String] = Nil,                        // Actions specified via .allowActions
  allowRoleActions: List[(List[String], List[String])] = Nil,  // (roles, actions) specified via .allowRoleActions
  isAuthenticated: Boolean = false                         // Whether .authenticated was called
) {
  override def toString: String = {
    val validations1 = renderValidations(validations)
    val allowRolesStr = if (allowRoles.isEmpty) "" else allowRoles.mkString("\"", "\", \"", "\"")
    val allowActionsStr = if (allowActions.isEmpty) "" else allowActions.mkString("\"", "\", \"", "\"")
    val allowRoleActionsStr = if (allowRoleActions.isEmpty) "" else {
      allowRoleActions.map { case (roles, actions) =>
        val rolesStr = roles.mkString("List(\"", "\", \"", "\")")
        val actionsStr = actions.mkString("List(\"", "\", \"", "\")")
        s"($rolesStr, $actionsStr)"
      }.mkString("List(", ", ", ")")
    }
    val allowRoleActionsOutput = if (allowRoleActions.isEmpty) "Nil" else allowRoleActionsStr
    s"""MetaAttribute("$attribute", $value, "$baseTpe", ${list(arguments)}, ${o(ref)}, ${o(reverseRef)}, ${o(relationship)}, ${o(enumTpe)}, ${list(options)}, ${o(alias)}, ${list(requiredAttrs)}, ${list(valueAttrs)}, $validations1, ${o(description)}, List($allowRolesStr), List($allowActionsStr), $allowRoleActionsOutput, $isAuthenticated)"""
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
  actions: List[String] = Nil  // Actions this role has (query, subscribe, save, etc.)
) {
  override def toString: String = {
    val actionsStr = if (actions.isEmpty) "" else actions.mkString("\"", "\", \"", "\"")
    s"""MetaRole("$role", List($actionsStr))"""
  }
}


