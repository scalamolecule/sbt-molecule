package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Public entity (no roles) - should pass, automatically has all 5 core actions */

trait Rule0_PublicEntity extends DomainStructure {
  // No roles defined - public entity
  trait Article { // ✅ Public entity, automatically has all 5 core actions
    val title = oneString
    val content = oneString
  }
}

object Rule0_PublicEntityTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 0: Public entity (no roles) - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      // Should not throw - public entities are exempt from all validation
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
