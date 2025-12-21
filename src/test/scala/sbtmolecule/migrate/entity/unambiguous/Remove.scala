package sbtmolecule.migrate.entity.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*


trait Remove_1 extends DomainStructure {
  trait Person extends Remove {
    val name = oneString
  }
}

trait Remove_2 extends DomainStructure {
  // Person entity removed - empty domain
}

object Remove extends BaseTest {
  val (before, after) = structures("entity/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.entity.unambiguous", "Remove_1", List(
          MetaSegment("", List(
            MetaEntity("Person", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), Some(EntityMigration.Remove))
          ))
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.entity.unambiguous", "Remove_2", List(
          MetaSegment("", List())
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL" - {
      sql(before) ==> List("DROP TABLE Person;")
    }

    "Marker cleanup" - {
      import sbt.IO
      import sbtmolecule.migration.AttributeRemover
      import sbtmolecule.ParseAndGenerate
      import java.io.File

      val domainStructureContent =
        """package sbtmolecule.migrate.entity.unambiguous
          |
          |import molecule.DomainStructure
          |
          |trait Remove_1 extends DomainStructure {
          |  trait Person extends Remove {
          |    val name = oneString
          |  }
          |}
          |""".stripMargin

      val tempFile = File.createTempFile("test", ".scala")
      try {
        IO.write(tempFile, domainStructureContent)

        // Parse to get positions
        val parsedDomain = ParseAndGenerate(tempFile.getAbsolutePath).generators.head.metaDomain

        // Extract entity migration marker
        val markedEntities = for {
          segment <- parsedDomain.segments
          entity <- segment.entities
          if entity.migration.isDefined
        } yield (AttributeRemover.EntityLevel, segment.segment, entity.entity, "", AttributeRemover.RemoveMarker, None)

        markedEntities.length ==> 1

        // Find locations
        val locations = AttributeRemover.findAttributeLocations(domainStructureContent, markedEntities, tempFile)
        locations.length ==> 1
        locations.head.entity ==> "Person"

        // Remove markers
        AttributeRemover.cleanupMigrationMarkers(tempFile, domainStructureContent, locations)

        IO.read(tempFile) ==>
          """package sbtmolecule.migrate.entity.unambiguous
            |
            |import molecule.DomainStructure
            |
            |trait Remove_1 extends DomainStructure {
            |}
            |""".stripMargin
      } finally {
        tempFile.delete()
      }
    }
  }
}
