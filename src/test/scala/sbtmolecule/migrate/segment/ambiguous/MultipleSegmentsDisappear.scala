package sbtmolecule.migrate.segment.ambiguous

import molecule.DomainStructure
import sbtmolecule.migrate.BaseTest
import utest.*

trait MultipleSegmentsDisappear_1 extends DomainStructure {
  object sales {
    trait Customer {
      val name = oneString
    }
  }

  object inventory {
    trait Product {
      val title = oneString
    }
  }

  object shipping {
    trait Carrier {
      val carrierName = oneString
    }
  }
}

trait MultipleSegmentsDisappear_2 extends DomainStructure {
  object inventory {
    trait Product {
      val title = oneString
    }
  }
}

object MultipleSegmentsDisappear extends BaseTest {
  override def tests: Tests = Tests {
    "Multiple segments disappear without migration markers" - {
      // When multiple segments disappear, all are reported
      intercept[Exception](
        structures("segment/ambiguous/")
      ).getMessage ==>
        """-- ERROR: Schema changes detected but explicit migration commands are missing.
          |
          |The following segments have been removed without extending `Remove` or `Rename("newName")`:
          |  sales
          |  shipping""".stripMargin
    }
  }
}
