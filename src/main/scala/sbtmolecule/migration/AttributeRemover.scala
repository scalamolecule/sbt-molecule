package sbtmolecule.migration

import java.io.{BufferedReader, File, InputStreamReader}
import molecule.base.metaModel.*
import sbt.IO

/**
 * Automatically removes migration markers from domain structure files after successful migrations.
 *
 * This includes attribute, entity, and segment migration markers. Domain structure files are
 * Molecule-controlled declarative metadata, so markers are automatically removed without prompting.
 * User is only prompted for renaming attribute usages in application code.
 */
object AttributeRemover {

  sealed trait MigrationMarker
  case object RemoveMarker extends MigrationMarker
  case class RenameMarker(newName: String) extends MigrationMarker

  sealed trait MigrationLevel
  case object AttributeLevel extends MigrationLevel
  case object EntityLevel extends MigrationLevel
  case object SegmentLevel extends MigrationLevel

  case class AttributeLocation(
    segment: String,
    entity: String,
    attribute: String,
    startPos: Int,
    endPos: Int,
    lineNumber: Int,
    marker: MigrationMarker
  )

  /**
   * Automatically removes migration markers from domain structure file after successful migration.
   * This includes:
   * - Removing entire attribute definitions marked with .remove
   * - Removing .rename("newName") markers from attributes (keeping the attribute)
   * - Removing entire entity trait definitions marked with extends Remove
   * - Removing extends Rename("...") from entity trait definitions (keeping the trait)
   * - Removing entire segment object definitions marked with extends Remove
   * - Removing extends Rename("...") from segment object definitions (keeping the object)
   *
   * Domain structure cleanup happens automatically (no prompt).
   * User is prompted only for renaming attribute usages in application code.
   *
   * @param domainStructureFile The domain structure file
   * @param namespacePath The namespace path (e.g., "app/domain" from package app.domain)
   * @param metaBefore The MetaDomain before migration (contains migration markers)
   * @param logger Function to log messages (e.g., sbt logger)
   * @return true if markers were cleaned up, false if no markers to clean
   */
  def promptAndRemoveAttributes(
    domainStructureFile: File,
    namespacePath: String,
    metaBefore: MetaDomain,
    logger: String => Unit
  ): Boolean = {
    // Find all attributes with migration markers
    val markedAttributes = collectMarkedAttributes(metaBefore)

    if (markedAttributes.isEmpty) {
      return false
    }

    // Read domain structure file to find positions
    val domainStructureContent = IO.read(domainStructureFile)
    val locations = findAttributeLocations(domainStructureContent, markedAttributes, domainStructureFile)

    if (locations.isEmpty) {
      return false
    }

    // Separate removes and renames for display
    val (removes, renames) = locations.partition(_.marker == RemoveMarker)

    // Display what will be cleaned up
    logger("")
    logger("=" * 70)
    logger("Migration completed successfully!")
    logger("")

    if (removes.nonEmpty) {
      logger("The following attributes marked with .remove can now be deleted:")
      removes.foreach { loc =>
        val prefix = if (loc.segment.nonEmpty) s"${loc.segment}.${loc.entity}" else loc.entity
        logger(s"  - $prefix.${loc.attribute} (line ${loc.lineNumber})")
      }
      logger("")
    }

    if (renames.nonEmpty) {
      logger("The following .rename(...) markers can now be removed:")
      renames.foreach { loc =>
        val prefix = if (loc.segment.nonEmpty) s"${loc.segment}.${loc.entity}" else loc.entity
        val newName = loc.marker match {
          case RenameMarker(name) => name
          case _ => ""
        }
        logger(s"  - $prefix.${loc.attribute} -> $newName (line ${loc.lineNumber})")
      }
      logger("")
    }

    // Auto-cleanup domain structure (no prompt)
    cleanupMigrationMarkers(domainStructureFile, domainStructureContent, locations)
    logger("")
    if (removes.nonEmpty && renames.nonEmpty) {
      logger(s"✓ Removed ${removes.length} attribute(s) and cleaned up ${renames.length} rename marker(s) from ${domainStructureFile.getName}")
    } else if (removes.nonEmpty) {
      logger(s"✓ Removed ${removes.length} attribute(s) from ${domainStructureFile.getName}")
    } else {
      logger(s"✓ Cleaned up ${renames.length} rename marker(s) from ${domainStructureFile.getName}")
    }

    // Offer to rename attribute usages across the codebase for renamed attributes (prompt for user code changes)
    if (renames.nonEmpty) {
        logger("")
        logger("The old attribute names will cause compile errors in your code.")
        logger("Would you like to automatically rename attribute usages across your codebase?")
        logger("(This will update .scala files in src/ directories)")
        logger("")

        // Check for auto-accept via environment variable (useful for scripts/testing)
        val autoRename = sys.env.get("MOLECULE_AUTO_RENAME_IN_CODE").contains("true")

        val renameResponse = if (autoRename) {
          logger("Rename attribute usages? [y/n]: y (auto-accepted via MOLECULE_AUTO_RENAME_IN_CODE)")
          logger("")
          "y"
        } else {
          print("Rename attribute usages? [y/n]: ")
          System.out.flush()

          val reader = new BufferedReader(new InputStreamReader(System.in))
          val renameInput = reader.readLine()
          if (renameInput == null) "n" else renameInput.trim.toLowerCase
        }

        if (renameResponse == "y" || renameResponse == "yes") {
          // Calculate project root from domain file using namespace path
          // e.g., src/main/scala/app/domain/Foo.scala with namespacePath="app/domain"
          // Go up: Foo.scala -> domain/ -> app/ -> scala/ -> main/ -> src/ -> project root
          val namespaceDepth = namespacePath.count(_ == '/') + 1 // Number of package segments
          val scalaDepth = 3 // scala/ -> main/ -> src/
          val totalLevels = namespaceDepth + scalaDepth

          var projectRoot = domainStructureFile.getParentFile // Start from Foo.scala's parent
          for (_ <- 0 until totalLevels) {
            projectRoot = projectRoot.getParentFile
          }

          val renamedCount = renameAttributeUsagesInCodebase(projectRoot, renames, logger)
          logger("")
          logger(s"✓ Renamed $renamedCount attribute usage(s) across the codebase")
        } else {
          logger("")
          logger("Skipped automatic renaming. You'll need to manually rename attribute usages.")
        }
    }

    logger("")
    true
  }

  /**
   * Collects all migration markers from the MetaDomain (attributes, entities, segments).
   * Returns (level, segment, entity, attribute, marker, sourcePosition)
   */
  private def collectMarkedAttributes(metaDomain: MetaDomain): List[(MigrationLevel, String, String, String, MigrationMarker, Option[(Int, Int)])] = {
    val attributeMarkers = for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attr <- entity.attributes
      migration <- attr.migration
      marker = migration match {
        case AttrMigration.Remove => Some(RemoveMarker)
        case AttrMigration.Rename(newName) => Some(RenameMarker(newName))
        case _ => None
      }
      if marker.isDefined
    } yield {
      val entityName = if (segment.segment.nonEmpty) {
        entity.entity.stripPrefix(segment.segment + "_")
      } else {
        entity.entity
      }
      (AttributeLevel, segment.segment, entityName, attr.attribute, marker.get, attr.sourcePosition)
    }

    val entityMarkers = for {
      segment <- metaDomain.segments
      entity <- segment.entities
      migration <- entity.migration
      marker = migration match {
        case EntityMigration.Remove => Some(RemoveMarker)
        case EntityMigration.Rename(newName) => Some(RenameMarker(newName))
        case _ => None
      }
      if marker.isDefined
    } yield {
      val entityName = if (segment.segment.nonEmpty) {
        entity.entity.stripPrefix(segment.segment + "_")
      } else {
        entity.entity
      }
      (EntityLevel, segment.segment, entityName, "", marker.get, None)
    }

    val segmentMarkers = for {
      segment <- metaDomain.segments
      migration <- segment.migration
      marker = migration match {
        case SegmentMigration.Remove => Some(RemoveMarker)
        case SegmentMigration.Rename(newName) => Some(RenameMarker(newName))
      }
      if marker.isDefined
    } yield {
      (SegmentLevel, segment.segment, "", "", marker.get, None)
    }

    attributeMarkers ++ entityMarkers ++ segmentMarkers
  }

  /**
   * Converts marked items (attributes, entities, segments) into AttributeLocations.
   * For attributes: Uses positions captured during Scala Meta parsing.
   * For entities and segments: Finds positions via text search patterns.
   */
  def findAttributeLocations(
    domainStructureContent: String,
    markedAttributes: List[(MigrationLevel, String, String, String, MigrationMarker, Option[(Int, Int)])],
    domainStructureFile: File
  ): List[AttributeLocation] = {
    markedAttributes.flatMap { case (level, segment, entity, attribute, marker, maybePosition) =>
      level match {
        case AttributeLevel =>
          // Attributes have positions captured during parsing
          maybePosition.map { case (startPos, endPos) =>
            val lineNumber = domainStructureContent.substring(0, startPos).count(_ == '\n') + 1
            AttributeLocation(
              segment = segment,
              entity = entity,
              attribute = attribute,
              startPos = startPos,
              endPos = endPos,
              lineNumber = lineNumber,
              marker = marker
            )
          }

        case EntityLevel =>
          // Find entity trait definition via text search
          findEntityPosition(domainStructureContent, segment, entity, marker)

        case SegmentLevel =>
          // Find segment object definition via text search
          findSegmentPosition(domainStructureContent, segment, marker)
      }
    }
  }

  /**
   * Finds position of entity trait definition with migration marker.
   */
  private def findEntityPosition(
    content: String,
    segment: String,
    entity: String,
    marker: MigrationMarker
  ): Option[AttributeLocation] = {
    // Pattern: trait EntityName extends Remove (or Rename("NewName") or RenameToDummyEntity)
    val markerPattern = marker match {
      case RemoveMarker => "Remove"
      case RenameMarker(newName) => s"""Rename\\s*\\(\\s*["']$newName["']\\s*\\)|RenameToDummyEntity"""
    }

    val pattern = s"""trait\\s+$entity\\s+extends\\s+[^\\{]*?($markerPattern)""".r

    pattern.findFirstMatchIn(content).map { m =>
      val startPos = m.start
      val endPos = m.end
      val lineNumber = content.substring(0, startPos).count(_ == '\n') + 1

      AttributeLocation(
        segment = segment,
        entity = entity,
        attribute = "",
        startPos = startPos,
        endPos = endPos,
        lineNumber = lineNumber,
        marker = marker
      )
    }
  }

  /**
   * Finds position of segment object definition with migration marker.
   */
  private def findSegmentPosition(
    content: String,
    segment: String,
    marker: MigrationMarker
  ): Option[AttributeLocation] = {
    // Pattern: object segmentName extends Remove (or Rename("NewName") or RenameToDummySegment)
    val markerPattern = marker match {
      case RemoveMarker => "Remove"
      case RenameMarker(newName) => s"""Rename\\s*\\(\\s*["']$newName["']\\s*\\)|RenameToDummySegment"""
    }

    val pattern = s"""object\\s+$segment\\s+extends\\s+($markerPattern)""".r

    pattern.findFirstMatchIn(content).map { m =>
      val startPos = m.start
      val endPos = m.end
      val lineNumber = content.substring(0, startPos).count(_ == '\n') + 1

      AttributeLocation(
        segment = segment,
        entity = "",
        attribute = "",
        startPos = startPos,
        endPos = endPos,
        lineNumber = lineNumber,
        marker = marker
      )
    }
  }

  /**
   * Cleans up migration markers from domain structure file.
   * - Attribute .remove: Removes entire attribute line
   * - Attribute .rename(...): Removes just the .rename(...) marker
   * - Entity/Segment extends Remove: Removes entire trait/object including body
   * - Entity/Segment extends Rename(...): Removes just the extends Rename(...) clause
   */
  def cleanupMigrationMarkers(
    domainStructureFile: File,
    domainStructureContent: String,
    locations: List[AttributeLocation]
  ): Unit = {
    // Sort by position in reverse order to keep positions valid
    val sortedLocations = locations.sortBy(_.startPos).reverse

    var modifiedContent = domainStructureContent

    sortedLocations.foreach { loc =>
      // Determine if this is attribute, entity, or segment based on which fields are populated
      val isAttribute = loc.attribute.nonEmpty
      val isEntity = loc.entity.nonEmpty && loc.attribute.isEmpty
      val isSegment = loc.entity.isEmpty && loc.attribute.isEmpty

      (isAttribute, isEntity, isSegment, loc.marker) match {
        case (true, false, false, RemoveMarker) =>
          // Attribute remove: Remove entire line
          var lineStart = loc.startPos
          while (lineStart > 0 && modifiedContent.charAt(lineStart - 1) != '\n') {
            lineStart -= 1
          }

          var lineEnd = loc.endPos
          while (lineEnd < modifiedContent.length && modifiedContent.charAt(lineEnd) != '\n') {
            lineEnd += 1
          }
          if (lineEnd < modifiedContent.length) {
            lineEnd += 1
          }

          val before = modifiedContent.substring(0, lineStart)
          val after = modifiedContent.substring(lineEnd)
          modifiedContent = before + after

        case (true, false, false, RenameMarker(_)) =>
          // Attribute rename: Remove just the .rename(...) marker
          val attrText = modifiedContent.substring(loc.startPos, loc.endPos)
          val renamePattern = """\.rename\s*\(\s*["'].*?["']\s*\)""".r
          var cleanedAttr = renamePattern.replaceFirstIn(attrText, "")

          // Clean up any trailing whitespace on lines after removal
          cleanedAttr = cleanedAttr.split("\n").map(_.replaceAll("\\s+$", "")).mkString("\n")

          val before = modifiedContent.substring(0, loc.startPos)
          val after = modifiedContent.substring(loc.endPos)
          modifiedContent = before + cleanedAttr + after

        case (false, true, false, RemoveMarker) =>
          // Entity remove: Remove entire trait definition including body
          val traitStart = loc.startPos
          val bodyEnd = findMatchingBrace(modifiedContent, loc.endPos)

          var lineStart = traitStart
          while (lineStart > 0 && modifiedContent.charAt(lineStart - 1) != '\n') {
            lineStart -= 1
          }

          var lineEnd = bodyEnd + 1
          if (lineEnd < modifiedContent.length && modifiedContent.charAt(lineEnd) == '\n') {
            lineEnd += 1
          }

          val before = modifiedContent.substring(0, lineStart)
          val after = modifiedContent.substring(lineEnd)
          modifiedContent = before + after

        case (false, true, false, RenameMarker(_)) =>
          // Entity rename: Remove just the "extends Rename(...)" or "extends RenameToDummyEntity" clause
          val entityText = modifiedContent.substring(loc.startPos, loc.endPos)
          val renamePattern = """\s*extends\s+Rename\s*\(\s*["'].*?["']\s*\)|\s*extends\s+RenameToDummyEntity""".r
          var cleanedEntity = renamePattern.replaceFirstIn(entityText, "")

          // Clean up trailing whitespace
          cleanedEntity = cleanedEntity.replaceAll("\\s+$", "")

          val before = modifiedContent.substring(0, loc.startPos)
          val after = modifiedContent.substring(loc.endPos)
          modifiedContent = before + cleanedEntity + after

        case (false, false, true, RemoveMarker) =>
          // Segment remove: Remove entire object definition including body
          val objectStart = loc.startPos
          val bodyEnd = findMatchingBrace(modifiedContent, loc.endPos)

          var lineStart = objectStart
          while (lineStart > 0 && modifiedContent.charAt(lineStart - 1) != '\n') {
            lineStart -= 1
          }

          var lineEnd = bodyEnd + 1
          if (lineEnd < modifiedContent.length && modifiedContent.charAt(lineEnd) == '\n') {
            lineEnd += 1
          }

          val before = modifiedContent.substring(0, lineStart)
          val after = modifiedContent.substring(lineEnd)
          modifiedContent = before + after

        case (false, false, true, RenameMarker(_)) =>
          // Segment rename: Remove just the "extends Rename(...)" or "extends RenameToDummySegment" clause
          val segmentText = modifiedContent.substring(loc.startPos, loc.endPos)
          val renamePattern = """\s*extends\s+Rename\s*\(\s*["'].*?["']\s*\)|\s*extends\s+RenameToDummySegment""".r
          var cleanedSegment = renamePattern.replaceFirstIn(segmentText, "")

          // Clean up trailing whitespace
          cleanedSegment = cleanedSegment.replaceAll("\\s+$", "")

          val before = modifiedContent.substring(0, loc.startPos)
          val after = modifiedContent.substring(loc.endPos)
          modifiedContent = before + cleanedSegment + after

        case _ =>
          // Unknown case - skip
          ()
      }
    }

    // Clean up any resulting triple+ blank lines (more than 2 consecutive newlines)
    var cleaned = modifiedContent.replaceAll("\n\n\n+", "\n\n")

    // Clean up trailing whitespace before braces
    cleaned = cleaned.replaceAll(" +\\{", " {")

    // Clean up blank lines before closing braces
    cleaned = cleaned.replaceAll("\n\n+(\\s*\\})", "\n$1")

    // Clean up blank lines after opening braces followed by closers
    cleaned = cleaned.replaceAll("\\{\\n\\n+(\\s*(?:trait|object|val))", "{\n$1")

    IO.write(domainStructureFile, cleaned)
  }

  /**
   * Finds the position of the matching closing brace for a trait/object body.
   */
  private def findMatchingBrace(content: String, startPos: Int): Int = {
    var pos = startPos
    var braceCount = 0
    var foundOpenBrace = false

    while (pos < content.length) {
      content.charAt(pos) match {
        case '{' =>
          braceCount += 1
          foundOpenBrace = true
        case '}' if foundOpenBrace =>
          braceCount -= 1
          if (braceCount == 0) {
            return pos
          }
        case _ =>
      }
      pos += 1
    }

    // If no matching brace found, return end of content
    content.length - 1
  }

  /**
   * Removes attributes from domain structure file by position, preserving all other content.
   * Removes in reverse order to keep positions valid.
   *
   * @deprecated Use cleanupMigrationMarkers instead
   */
  def removeAttributesFromDomainStructure(
    domainStructureFile: File,
    domainStructureContent: String,
    locations: List[AttributeLocation]
  ): Unit = {
    cleanupMigrationMarkers(domainStructureFile, domainStructureContent, locations)
  }

  /**
   * Renames attribute usages across the codebase using Scala Meta AST parsing.
   * Only renames actual Scala identifiers (Term.Name nodes in the AST).
   * Strings, comments, and documentation are never modified.
   *
   * @param projectRoot Root directory of the project
   * @param renames List of AttributeLocations with RenameMarker
   * @param logger Logging function
   * @return Number of replacements made
   */
  private def renameAttributeUsagesInCodebase(
    projectRoot: File,
    renames: List[AttributeLocation],
    logger: String => Unit
  ): Int = {
    import scala.meta._

    // Collect old -> new name mappings
    val renameMap = renames.collect {
      case loc if loc.marker.isInstanceOf[RenameMarker] =>
        val newName = loc.marker.asInstanceOf[RenameMarker].newName
        (loc.attribute, newName)
    }.toMap

    if (renameMap.isEmpty) {
      return 0
    }

    var totalReplacements = 0

    // Find all .scala files in src/ directories (excluding target/)
    def findScalaFiles(dir: File): List[File] = {
      if (!dir.exists() || !dir.isDirectory) {
        return List.empty
      }

      val (dirs, files) = dir.listFiles().partition(_.isDirectory)

      // Skip target, .git, and generated directories
      val validDirs = dirs.filterNot { d =>
        val name = d.getName
        name == "target" || name == ".git" || name == "moleculeGen"
      }

      val scalaFiles = files.filter(_.getName.endsWith(".scala")).toList
      val nestedFiles = validDirs.flatMap(findScalaFiles).toList

      scalaFiles ++ nestedFiles
    }

    val scalaFiles = findScalaFiles(projectRoot)
    logger(s"Parsing ${scalaFiles.length} Scala files with Scala Meta...")

    scalaFiles.foreach { file =>
      try {
        val content = IO.read(file)
        val input = Input.VirtualFile(file.getPath, content)
        val dialect = dialects.Scala3(input)

        // Try to parse the file
        dialect.parse[Source] match {
          case Parsed.Success(tree) =>
            // Find all Term.Name nodes that match our rename map
            val replacements = scala.collection.mutable.ListBuffer[(Int, Int, String, String)]()

            tree.traverse {
              case name @ Term.Name(oldName) if renameMap.contains(oldName) =>
                // Found an identifier that needs renaming
                val newName = renameMap(oldName)
                replacements += ((name.pos.start, name.pos.end, oldName, newName))
            }

            if (replacements.nonEmpty) {
              // Apply replacements in reverse order to maintain position validity
              var modified = content
              replacements.sortBy(_._1).reverse.foreach { case (start, end, oldName, newName) =>
                val before = modified.substring(0, start)
                val after = modified.substring(end)
                modified = before + newName + after
              }

              IO.write(file, modified)
              totalReplacements += replacements.length
              logger(s"  Updated: ${file.getPath.substring(projectRoot.getPath.length)} (${replacements.length} replacements)")
            }

          case Parsed.Error(pos, message, details) =>
            // Skip files that don't parse (maybe they have syntax errors)
            logger(s"  Skipped (parse error): ${file.getPath.substring(projectRoot.getPath.length)}")
        }
      } catch {
        case e: Exception =>
          // Skip files that cause exceptions
          logger(s"  Skipped (error): ${file.getPath.substring(projectRoot.getPath.length)}")
      }
    }

    totalReplacements
  }
}
