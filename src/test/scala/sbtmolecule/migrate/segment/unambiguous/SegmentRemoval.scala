package sbtmolecule.migrate.segment.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait SegmentRemoval_1 extends DomainStructure {
  object sales extends Remove {
    trait Customer {
      val name = oneString
    }

    trait Order {
      val orderNumber = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

trait SegmentRemoval_2 extends DomainStructure {
  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object SegmentRemoval extends BaseTest {
  val (before, after) = structures("segment/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRemoval_1", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), Some(SegmentMigration.Remove)),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.HasChanges)
    }

    "After (clean)" - {
      after ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRemoval_2", List(
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL - drops all tables in segment" - {
      sql(before) ==> List(
        "DROP TABLE sales_Customer;",
        "DROP TABLE sales_Order;"
      )
    }

    "Marker cleanup" - {
      import sbt.IO
      import sbtmolecule.migration.AttributeRemover
      import sbtmolecule.ParseAndGenerate
      import java.io.File

      val domainStructureContent =
        """package sbtmolecule.migrate.segment.unambiguous
          |
          |import molecule.DomainStructure
          |
          |trait SegmentRemoval_1 extends DomainStructure {
          |  object sales extends Remove {
          |    trait Customer {
          |      val name = oneString
          |    }
          |
          |    trait Order {
          |      val orderNumber = oneString
          |    }
          |  }
          |
          |  object inventory {
          |    trait Product {
          |      val title = oneString
          |    }
          |  }
          |}
          |""".stripMargin

      val tempFile = File.createTempFile("test", ".scala")
      try {
        IO.write(tempFile, domainStructureContent)

        // Parse to get positions
        val parsedDomain = ParseAndGenerate(tempFile.getAbsolutePath).generators.head.metaDomain

        // Extract segment migration marker
        val markedSegments = for {
          segment <- parsedDomain.segments
          if segment.migration.isDefined
        } yield (AttributeRemover.SegmentLevel, segment.segment, "", "", AttributeRemover.RemoveMarker, None)

        markedSegments.length ==> 1

        // Find locations
        val locations = AttributeRemover.findAttributeLocations(domainStructureContent, markedSegments, tempFile)
        locations.length ==> 1
        locations.head.segment ==> "sales"

        // Remove markers
        AttributeRemover.cleanupMigrationMarkers(tempFile, domainStructureContent, locations)

        IO.read(tempFile) ==>
          """package sbtmolecule.migrate.segment.unambiguous
            |
            |import molecule.DomainStructure
            |
            |trait SegmentRemoval_1 extends DomainStructure {
            |  object inventory {
            |    trait Product {
            |      val title = oneString
            |    }
            |  }
            |}
            |""".stripMargin
      } finally {
        tempFile.delete()
      }
    }
  }
}
