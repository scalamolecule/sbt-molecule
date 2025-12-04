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
        // Entity bitmask = permissions from entity trait inheritance, filtered by query action
        val entityBitmask = filterEntityRolesByAction(entity, "query", roleMap, roles)
        (entity.entity, entityBitmask)
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

  def queryAccessAttributes: String = generateAccessAttributes("query")
  def saveAccessAttributes: String = generateAccessAttributes("save")
  def insertAccessAttributes: String = generateAccessAttributes("insert")
  def updateAccessAttributes: String = generateAccessAttributes("update")
  def deleteAccessAttributes: String = generateDeleteAccessAttributes()
  def rawQueryAccessAttributes: String = generateAccessAttributes("rawQuery")
  def rawTransactAccessAttributes: String = generateAccessAttributes("rawTransact")

  def saveAccessEntities: String = generateAccessEntities("save")
  def insertAccessEntities: String = generateAccessEntities("insert")
  def updateAccessEntities: String = generateAccessEntities("update")
  def deleteAccessEntities: String = generateAccessEntities("delete")
  def rawQueryAccessEntities: String = generateAccessEntities("rawQuery")
  def rawTransactAccessEntities: String = generateAccessEntities("rawTransact")

  // Role action bitmasks - which roles have which actions
  // Used to check authenticated user's permissions on public entities (-1)
  def roleQueryAction: String = generateRoleActionBitmask("query")
  def roleSaveAction: String = generateRoleActionBitmask("save")
  def roleInsertAction: String = generateRoleActionBitmask("insert")
  def roleUpdateAction: String = generateRoleActionBitmask("update")
  def roleDeleteAction: String = generateRoleActionBitmask("delete")
  def roleRawQueryAction: String = generateRoleActionBitmask("rawQuery")
  def roleRawTransactAction: String = generateRoleActionBitmask("rawTransact")

  private def generateAccessEntities(action: String): String = {
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
        // Calculate attribute bitmasks based on role and action tuning
        val attrBitmasks = entity.attributes.map { attr =>
          calculateAttributeBitmask(entity, attr, action, roleMap, roles)
        }

        // Entity-level bitmask should be the union of all attribute bitmasks
        // This allows access if ANY attribute is accessible for this action
        val combinedAttrBitmask = if (attrBitmasks.isEmpty) 0 else attrBitmasks.reduce(_ | _)

        val entityBitmask = if (action == "delete") {
          // For delete action: roles with delete action + entity deleting grants
          if (entity.entityRoles.nonEmpty) {
            calculateEntityBitmask(entity, action, roleMap, roles)
          } else {
            // Public entity
            -1
          }
        } else {
          // For other actions: use combined attribute bitmask
          // This allows entity access if ANY attribute is accessible for this action
          combinedAttrBitmask
        }
        (entity.entity, entityBitmask)
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

  private def generateAccessAttributes(action: String): String = {
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
          calculateAttributeBitmask(entity, attr, action, roleMap, roles)
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

  private def generateDeleteAccessAttributes(): String = {
    val roles = metaDomain.roles
    if (roles.isEmpty) {
      "IArray.empty[Int]"
    } else {
      val roleMap = roles.sortBy(_.role).zipWithIndex.map { case (role, index) => role.role -> index }.toMap

      // For delete, all attributes of an entity should have the same permissions as the entity
      val attributesByEntity = (for {
        segment <- metaDomain.segments
        entity <- segment.entities
      } yield {
        // Entity delete permission = roles with delete action + entity deleting grants
        val entityDeleteBitmask = if (entity.entityRoles.nonEmpty) {
          calculateEntityBitmask(entity, "delete", roleMap, roles)
        } else {
          // Public entity
          -1
        }

        // All attributes get the same entity-level permission
        val attrs = entity.attributes.map(_ => entityDeleteBitmask)
        (entity.entity, attrs)
      }).toList

      val p = indent(1)
      val pad = s"\n$p  "

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

  private def generateRoleActionBitmask(action: String): String = {
    val roles = metaDomain.roles
    if (roles.isEmpty) {
      "0"
    } else {
      // Roles are sorted alphabetically (same order as role indices)
      val sortedRoles = roles.sortBy(_.role)
      val bitmask = sortedRoles.zipWithIndex.foldLeft(0) { case (mask, (role, index)) =>
        if (role.actions.contains(action)) {
          mask | (1 << index)
        } else {
          mask
        }
      }
      bitmask.toString
    }
  }

  private def calculateEntityBitmask(entity: MetaEntity, action: String, roleMap: Map[String, Int], roles: List[MetaRole]): Int = {
    // Public entity (no roles defined) = -1 (special marker for unauthenticated-only access)
    // Authenticated users must still follow their role's action permissions
    if (entity.entityRoles.isEmpty) {
      -1 // -1 = public (unauthenticated access allowed, authenticated users follow role permissions)
    }
    // Entity with specific roles - filter by action + grants
    else {
      // Calculate base bitmask from roles that have this action
      val baseBitmask = entity.entityRoles.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          case Some(index) =>
            val role = roles.find(_.role == roleName).getOrElse(
              throw new Exception(s"Role '$roleName' not found in role definitions")
            )
            if (role.actions.contains(action)) {
              bitmask | (1 << index)
            } else {
              bitmask
            }
          case None =>
            throw new Exception(s"Role '$roleName' used in entity '${entity.entity}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
        }
      }

      // Add grants for this action
      val grantsBitmask = if (action == "update") {
        entity.entityUpdatingGrants.foldLeft(0) { (bitmask, roleName) =>
          roleMap.get(roleName).map(index => bitmask | (1 << index)).getOrElse(bitmask)
        }
      } else if (action == "delete") {
        entity.entityDeletingGrants.foldLeft(0) { (bitmask, roleName) =>
          roleMap.get(roleName).map(index => bitmask | (1 << index)).getOrElse(bitmask)
        }
      } else {
        0
      }

      baseBitmask | grantsBitmask
    }
  }

  private def calculateAttributeBitmask(
    entity: MetaEntity,
    attr: MetaAttribute,
    action: String,
    roleMap: Map[String, Int],
    roles: List[MetaRole]
  ): Int = {

    // Authorization Model: 4 Layers
    // Layer 1: Entity roles (who can access entity)
    // Layer 2: Action grants at entity level (updating[R], deleting[R])
    // Layer 3: Attribute restrictions (.only[R], .exclude[R])
    // Layer 4: Attribute update grants (.updating[R] at attribute level)

    // Step 1: Determine effective roles for this attribute after restrictions (Layer 3)
    val effectiveRoles = if (attr.onlyRoles.nonEmpty) {
      // .only[R] - restrict to only these roles
      attr.onlyRoles
    } else if (attr.excludedRoles.nonEmpty) {
      // .exclude[R] - all entity roles except these
      entity.entityRoles.filterNot(attr.excludedRoles.contains)
    } else {
      // No restrictions - use entity roles
      entity.entityRoles
    }

    // Step 2: Calculate base permissions from role actions
    val baseBitmask = if (effectiveRoles.isEmpty) {
      // Public entity/attribute (no roles defined) = -1 (unauthenticated access only)
      -1
    } else {
      // Filter effective roles by those that have this action
      effectiveRoles.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          case Some(index) =>
            val role = roles.find(_.role == roleName).getOrElse(
              throw new Exception(s"Role '$roleName' not found in role definitions")
            )
            if (role.actions.contains(action)) {
              bitmask | (1 << index)
            } else {
              bitmask
            }
          case None =>
            throw new Exception(s"Role '$roleName' used in entity/attribute '${entity.entity}.${attr.attribute}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
        }
      }
    }

    // Step 3: Apply grants (Layer 2 & 4)
    // IMPORTANT: Grants are ADDITIVE and apply to roles in effectiveRoles
    // (after attribute restrictions are applied)
    val grantsBitmask = if (action == "update") {
      // For update action: combine entity and attribute update grants
      // Action grants apply to all attributes, attribute update grants are attribute-specific
      val entityGrantBitmask = entity.entityUpdatingGrants.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          // Grant applies if role passes attribute restrictions (is in effectiveRoles)
          case Some(index) if effectiveRoles.contains(roleName) => bitmask | (1 << index)
          case _ => bitmask
        }
      }
      val attrGrantBitmask = attr.attrUpdatingGrants.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          // Grant applies if role passes attribute restrictions (is in effectiveRoles)
          case Some(index) if effectiveRoles.contains(roleName) => bitmask | (1 << index)
          case _ => bitmask
        }
      }
      entityGrantBitmask | attrGrantBitmask
    } else if (action == "delete") {
      // For delete action: action grants apply to ALL attributes
      // (deleting an entity deletes all its attributes, so attribute restrictions don't apply)
      entity.entityDeletingGrants.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          // Grant applies to all attributes regardless of attribute restrictions
          case Some(index) => bitmask | (1 << index)
          case _ => bitmask
        }
      }
    } else {
      0 // No grants for other actions (query, save, insert, rawQuery, rawTransact)
    }

    // Step 4: Combine base permissions with grants
    baseBitmask | grantsBitmask
  }


  private def filterEntityRolesByAction(
    entity: MetaEntity,
    action: String,
    roleMap: Map[String, Int],
    roles: List[MetaRole]
  ): Int = {
    // If entity is public (no roles defined), allow public access for all actions
    if (entity.entityRoles.isEmpty) {
      // Public entity - all actions are public (-1 = unauthenticated access allowed)
      -1
    }
    // If entity has specific roles, only those roles with this action can access
    else {
      entity.entityRoles.foldLeft(0) { (bitmask, roleName) =>
        roleMap.get(roleName) match {
          case Some(index) =>
            // Check if this role has the action
            val role = roles.find(_.role == roleName).getOrElse(
              throw new Exception(s"Role '$roleName' not found in role definitions")
            )
            if (role.actions.contains(action)) {
              bitmask | (1 << index)
            } else {
              bitmask
            }
          case None =>
            throw new Exception(s"Role '$roleName' used in entity '${entity.entity}' is not defined. Available roles: ${roleMap.keys.mkString(", ")}")
        }
      }
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
        |  /** Bitmask of roles that have query action */
        |  override val roleQueryAction: Int = $roleQueryAction
        |
        |  /** Bitmask of roles that have save action */
        |  override val roleSaveAction: Int = $roleSaveAction
        |
        |  /** Bitmask of roles that have insert action */
        |  override val roleInsertAction: Int = $roleInsertAction
        |
        |  /** Bitmask of roles that have update action */
        |  override val roleUpdateAction: Int = $roleUpdateAction
        |
        |  /** Bitmask of roles that have delete action */
        |  override val roleDeleteAction: Int = $roleDeleteAction
        |
        |  /** Bitmask of roles that have rawQuery action */
        |  override val roleRawQueryAction: Int = $roleRawQueryAction
        |
        |  /** Bitmask of roles that have rawTransact action */
        |  override val roleRawTransactAction: Int = $roleRawTransactAction
        |
        |  /** Bitwise role access for entities on query action */
        |  override val queryAccessEntities: IArray[Int] = $queryAccessEntities
        |
        |  /** Bitwise role access for attributes on query action */
        |  override val queryAccessAttributes: IArray[Int] = $queryAccessAttributes
        |
        |  /** Bitwise role access for entities on save action */
        |  override val saveAccessEntities: IArray[Int] = $saveAccessEntities
        |
        |  /** Bitwise role access for attributes on save action */
        |  override val saveAccessAttributes: IArray[Int] = $saveAccessAttributes
        |
        |  /** Bitwise role access for entities on insert action */
        |  override val insertAccessEntities: IArray[Int] = $insertAccessEntities
        |
        |  /** Bitwise role access for attributes on insert action */
        |  override val insertAccessAttributes: IArray[Int] = $insertAccessAttributes
        |
        |  /** Bitwise role access for entities on update action */
        |  override val updateAccessEntities: IArray[Int] = $updateAccessEntities
        |
        |  /** Bitwise role access for attributes on update action */
        |  override val updateAccessAttributes: IArray[Int] = $updateAccessAttributes
        |
        |  /** Bitwise role access for entities on delete action */
        |  override val deleteAccessEntities: IArray[Int] = $deleteAccessEntities
        |
        |  /** Bitwise role access for attributes on delete action */
        |  override val deleteAccessAttributes: IArray[Int] = $deleteAccessAttributes
        |
        |  /** Bitwise role access for entities on rawQuery action */
        |  override val rawQueryAccessEntities: IArray[Int] = $rawQueryAccessEntities
        |
        |  /** Bitwise role access for attributes on rawQuery action */
        |  override val rawQueryAccessAttributes: IArray[Int] = $rawQueryAccessAttributes
        |
        |  /** Bitwise role access for entities on rawTransact action */
        |  override val rawTransactAccessEntities: IArray[Int] = $rawTransactAccessEntities
        |
        |  /** Bitwise role access for attributes on rawTransact action */
        |  override val rawTransactAccessAttributes: IArray[Int] = $rawTransactAccessAttributes
        |}""".stripMargin
}
