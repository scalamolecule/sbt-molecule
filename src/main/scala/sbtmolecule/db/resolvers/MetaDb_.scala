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

  def roleIndex: String = {
    val roles = metaDomain.roles
    if (roles.isEmpty) {
      "Map.empty[String, Int]"
    } else {
      if (roles.length > 32) {
        throw new Exception(s"Maximum 32 roles supported. Found ${roles.length} roles: ${roles.map(_.role).mkString(", ")}")
      }
      val p = indent(1)
      val pad = s"\n$p  "
      val maxRole = roles.map(_.role.length).max
      val roleEntries = roles.sortBy(_.role).zipWithIndex.map { case (role, index) =>
        s""""${role.role}"${padS(maxRole, role.role)} -> $index"""
      }
      val entriesStr = if (roleEntries.isEmpty) "" else roleEntries.mkString(pad, s",$pad", s"\n$p")
      s"Map($entriesStr)"
    }
  }

  def queryAccessEntities: String = {
    val roles = metaDomain.roles
    if (roles.isEmpty) {
      "IArray.empty[Int]"
    } else {
      val roleMap = roles.sortBy(_.role).zipWithIndex.map { case (role, index) => role.role -> index }.toMap
      val entities = for {
        segment <- metaDomain.segments
        entity <- segment.entities
      } yield entity

      val entityBitmasks = entities.map { entity =>
        (entity.entity, calculateEntityBitmask(entity, roleMap, roles))
      }

      val p = indent(1)
      val pad = s"\n$p  "
      val maxEntity = if (entityBitmasks.isEmpty) 0 else entityBitmasks.map(_._1.length).max
      val bitmaskStr = entityBitmasks.map { case (entityName, bitmask) =>
        s"/* ${entityName.padTo(maxEntity, ' ')} */ $bitmask"
      }
      val masksStr = if (bitmaskStr.isEmpty) "" else bitmaskStr.mkString(pad, s",$pad", s"\n$p")
      s"IArray($masksStr)"
    }
  }

  def queryAccessAttributes: String = {
    val roles = metaDomain.roles
    if (roles.isEmpty) {
      "IArray.empty[Int]"
    } else {
      val roleMap = roles.sortBy(_.role).zipWithIndex.map { case (role, index) => role.role -> index }.toMap

      // Group attributes by entity
      val attributesByEntity = (for {
        segment <- metaDomain.segments
        entity <- segment.entities
      } yield {
        val attrs = entity.attributes.map { attr =>
          calculateAttributeBitmask(entity, attr, roleMap, roles)
        }
        (entity.entity, attrs)
      }).toList

      val p = indent(1)
      val pad = s"\n$p  "

      // Find max entity name length for alignment
      val maxEntity = if (attributesByEntity.isEmpty) 0 else attributesByEntity.map(_._1.length).max

      val lines = attributesByEntity.flatMap { case (entityName, attrBitmasks) =>
        if (attrBitmasks.isEmpty) {
          Nil
        } else {
          List(s"/* ${entityName.padTo(maxEntity, ' ')} */ ${attrBitmasks.mkString(", ")}")
        }
      }

      val masksStr = if (lines.isEmpty) "" else lines.mkString(pad, s",$pad", s"\n$p")
      s"IArray($masksStr)"
    }
  }

  // Helper methods for bitmask calculation ................................................

  private def calculateEntityBitmask(entity: MetaEntity, roleMap: Map[String, Int], roles: List[MetaRole]): Int = {
    // Public entity (no roles, not authenticated) = all bits set
    if (entity.entityRoles.isEmpty && !entity.isAuthenticated) {
      0xFFFFFFFF // All bits set = public
    }
    // Authenticated entity = all defined role bits set
    else if (entity.isAuthenticated) {
      allRoleBits(roleMap)
    }
    // Entity with specific roles
    else {
      entity.entityRoles.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          case Some(index) => bitmask | (1 << index)
          case None =>
            throw new Exception(s"Role '$roleName' used in entity '${entity.entity}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
        }
      }
    }
  }

  private def calculateAttributeBitmask(
    entity: MetaEntity,
    attr: MetaAttribute,
    roleMap: Map[String, Int],
    roles: List[MetaRole]
  ): Int = {
    // Priority order (first match wins):
    // 1. .allowRoles[R] - explicit role override
    // 2. .allowRoleActions[R, A] - role + action combination (can be chained, uses OR)
    // 3. .allowActions[A] - action-based filtering of entity roles
    // 4. .authenticated - any authenticated user
    // 5. Inherit from entity default

    // 1. Explicit role override
    if (attr.allowRoles.nonEmpty) {
      attr.allowRoles.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          case Some(index) => bitmask | (1 << index)
          case None =>
            throw new Exception(s"Role '$roleName' used in attribute '${entity.entity}.${attr.attribute}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
        }
      }
    }
    // 2. Chained allowRoleActions (bitwise OR of all chains)
    else if (attr.allowRoleActions.nonEmpty) {
      attr.allowRoleActions.foldLeft(0) { (bitmask, roleActionPair) =>
        val (roleNames, actionNames) = roleActionPair
        val chainBitmask = filterRolesByActions(roleNames, actionNames, roleMap, roles, entity, attr)
        bitmask | chainBitmask
      }
    }
    // 3. Action-based filtering
    else if (attr.allowActions.nonEmpty) {
      val entityRoleNames = if (entity.isAuthenticated) {
        roleMap.keys.toList
      } else if (entity.entityRoles.isEmpty) {
        roleMap.keys.toList // Public entity
      } else {
        entity.entityRoles
      }
      filterRolesByActions(entityRoleNames, attr.allowActions, roleMap, roles, entity, attr)
    }
    // 4. Authenticated attribute
    else if (attr.isAuthenticated) {
      allRoleBits(roleMap)
    }
    // 5. Inherit from entity
    else {
      calculateEntityBitmask(entity, roleMap, roles)
    }
  }

  private def filterRolesByActions(
    roleNames: List[String],
    actionNames: List[String],
    roleMap: Map[String, Int],
    roles: List[MetaRole],
    entity: MetaEntity,
    attr: MetaAttribute
  ): Int = {
    roleNames.foldLeft(0) { (bitmask, roleName) =>
      roleMap.get(roleName) match {
        case Some(index) =>
          // Check if this role has ALL the specified actions
          val role = roles.find(_.role == roleName).getOrElse(
            throw new Exception(s"Role '$roleName' not found in role definitions")
          )
          val hasAllActions = actionNames.forall(action => role.actions.contains(action))
          if (hasAllActions) {
            bitmask | (1 << index)
          } else {
            bitmask
          }
        case None =>
          throw new Exception(s"Role '$roleName' used in attribute '${entity.entity}.${attr.attribute}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
      }
    }
  }

  private def allRoleBits(roleMap: Map[String, Int]): Int = {
    roleMap.values.foldLeft(0) { (bitmask, index) =>
      bitmask | (1 << index)
    }
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
        |
        |
        |  // Access control -------------------------------------------------------
        |
        |  /** Role name to bit index (0-31) */
        |  override val roleIndex: Map[String, Int] = $roleIndex
        |
        |  /** Bitwise role access for entities on query action */
        |  override val queryAccessEntities: IArray[Int] = $queryAccessEntities
        |
        |  /** Bitwise role access for attributes on query action */
        |  override val queryAccessAttributes: IArray[Int] = $queryAccessAttributes
        |}""".stripMargin
}
