package sbtmolecule.migrate.segment.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait SegmentDisappears_1 extends DomainStructure {
  object sales {
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

trait SegmentDisappears_2 extends DomainStructure {
  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object SegmentDisappears extends BaseTest {
  override def tests: Tests = Tests {
    "Entire segment disappears without migration marker" - {
      // When a segment disappears without extends Remove or Rename, it's an error
      intercept[Exception](
        structures("segment/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following segments have been removed without extending `Remove` or `Rename("newName")`:
          |  sales""".stripMargin
    }
  }
}
