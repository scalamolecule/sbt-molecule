package sbtmolecule.migrate.segment.unambiguous

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

trait SegmentRename_1 extends DomainStructure {
  object sales extends RenameToDummySegment {
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

trait SegmentRename_2 extends DomainStructure {
  object seg {
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

object SegmentRename extends BaseTest {
  val (before, after) = structures("segment/unambiguous/")

  override def tests: Tests = Tests {
    "Before (annotated)" - {
      stripPositions(before) ==>
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRename_1", List(
          MetaSegment("sales", List(
            MetaEntity("sales_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("sales_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), Some(SegmentMigration.Rename("seg"))),
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
        MetaDomain("sbtmolecule.migrate.segment.unambiguous", "SegmentRename_2", List(
          MetaSegment("seg", List(
            MetaEntity("seg_Customer", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None),
            MetaEntity("seg_Order", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("orderNumber", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None),
          MetaSegment("inventory", List(
            MetaEntity("inventory_Product", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None),
              MetaAttribute("title", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None, Nil, Nil, Nil, Map.empty, None)
            ), List(), List(), List(), false, None, List(), List(), List(), List(), None)
          ), None)
        ), List(), Map.empty, None, MigrationStatus.Clean)
    }

    "SQL - renames all tables in segment" - {
      sql(before) ==> List(
        "ALTER TABLE sales_Customer RENAME TO seg_Customer;",
        "ALTER TABLE sales_Order RENAME TO seg_Order;"
      )
    }

    "Marker cleanup" - {
      import sbt.IO
      import sbtmolecule.migration.AttributeRemover
      import sbtmolecule.ParseAndGenerate
      import java.io.File

      val domainStructureContent = """package sbtmolecule.migrate.segment.unambiguous
        |
        |import molecule.DomainStructure
        |
        |trait SegmentRename_1 extends DomainStructure {
        |  object sales extends RenameToDummySegment {
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
        } yield (AttributeRemover.SegmentLevel, segment.segment, "", "", AttributeRemover.RenameMarker("seg"), None)

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
        |trait SegmentRename_1 extends DomainStructure {
        |  object sales {
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
      } finally {
        tempFile.delete()
      }
    }
  }
}
