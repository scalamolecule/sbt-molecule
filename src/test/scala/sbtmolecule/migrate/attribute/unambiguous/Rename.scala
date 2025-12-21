package sbtmolecule.migrate.attribute.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait Rename_1 extends DomainStructure {
  trait Person {
    val name =
      oneString

        .rename("fullName")
  }
}

trait Rename_2 extends DomainStructure {
  trait Person {
    val fullName = oneString
  }
}

object Rename extends BaseTest {
  val (before, after) = structures("attribute/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "Rename_1", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, Some(AttrMigration.Rename("fullName")))
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.attribute.unambiguous", "Rename_2", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("fullName", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ))
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List("ALTER TABLE Person RENAME COLUMN name TO fullName;")
    }

    "Marker cleanup" - {
      import sbt.IO
      import sbtmolecule.migration.AttributeRemover
      import sbtmolecule.ParseAndGenerate
      import java.io.File

      val domainStructureContent =
        """package sbtmolecule.migrate.attribute.unambiguous
          |
          |import molecule.DomainStructure
          |
          |trait Rename_1 extends DomainStructure {
          |  trait Person {
          |    val name = oneString.rename("fullName")
          |  }
          |}
          |""".stripMargin

      val tempFile = File.createTempFile("test", ".scala")
      try {
        IO.write(tempFile, domainStructureContent)

        // Parse to get positions
        val parsedDomain = ParseAndGenerate(tempFile.getAbsolutePath).generators.head.metaDomain

        // Extract attribute migration marker
        val markedAttrs = for {
          segment <- parsedDomain.segments
          entity <- segment.entities
          attr <- entity.attributes
          if attr.migration.isDefined
          (startPos, endPos) <- attr.sourcePosition
        } yield (AttributeRemover.AttributeLevel, segment.segment, entity.entity, attr.attribute, AttributeRemover.RenameMarker("fullName"), Some((startPos, endPos)))

        markedAttrs.length ==> 1

        // Find locations
        val locations = AttributeRemover.findAttributeLocations(domainStructureContent, markedAttrs, tempFile)
        locations.length ==> 1
        locations.head.attribute ==> "name"

        // Remove markers
        AttributeRemover.cleanupMigrationMarkers(tempFile, domainStructureContent, locations)

        IO.read(tempFile) ==>
          """package sbtmolecule.migrate.attribute.unambiguous
            |
            |import molecule.DomainStructure
            |
            |trait Rename_1 extends DomainStructure {
            |  trait Person {
            |    val name = oneString
            |  }
            |}
            |""".stripMargin
      } finally {
        tempFile.delete()
      }
    }

    "Multiline attribute definitions" - {
      import sbt.IO
      import sbtmolecule.migration.AttributeRemover
      import sbtmolecule.ParseAndGenerate
      import java.io.File

      val domainStructureContent =
        """package sbtmolecule.migrate.attribute.unambiguous
          |
          |import molecule.DomainStructure
          |
          |trait Rename_1 extends DomainStructure {
          |  trait Person {
          |    val name =
          |      oneString
          |        .index
          |        .rename("fullName")
          |  }
          |}
          |""".stripMargin

      val tempFile = File.createTempFile("test", ".scala")
      try {
        IO.write(tempFile, domainStructureContent)

        // Parse to get positions
        val parsedDomain = ParseAndGenerate(tempFile.getAbsolutePath).generators.head.metaDomain

        // Extract attribute migration marker
        val markedAttrs = for {
          segment <- parsedDomain.segments
          entity <- segment.entities
          attr <- entity.attributes
          if attr.migration.isDefined
          (startPos, endPos) <- attr.sourcePosition
        } yield (AttributeRemover.AttributeLevel, segment.segment, entity.entity, attr.attribute, AttributeRemover.RenameMarker("fullName"), Some((startPos, endPos)))

        markedAttrs.length ==> 1

        // Find locations
        val locations = AttributeRemover.findAttributeLocations(domainStructureContent, markedAttrs, tempFile)
        locations.length ==> 1
        locations.head.attribute ==> "name"

        // Remove markers
        AttributeRemover.cleanupMigrationMarkers(tempFile, domainStructureContent, locations)

        IO.read(tempFile) ==>
          """package sbtmolecule.migrate.attribute.unambiguous
            |
            |import molecule.DomainStructure
            |
            |trait Rename_1 extends DomainStructure {
            |  trait Person {
            |    val name =
            |      oneString
            |        .index
            |  }
            |}
            |""".stripMargin
      } finally {
        tempFile.delete()
      }
    }
  }
}
