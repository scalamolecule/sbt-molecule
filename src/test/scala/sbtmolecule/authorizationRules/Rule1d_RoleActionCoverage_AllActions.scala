package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Role with all 5 core CRUD actions - should pass */

trait Rule1d_RoleActionCoverage_AllActions extends DomainStructure {
  trait Admin extends Role with query with save with insert with update with delete

  trait Post extends Admin { // ✅ Has all 5 core actions
    val content = oneString
  }
}

object Rule1d_RoleActionCoverage_AllActionsTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1d: Role with all 5 core CRUD actions - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      // Should not throw
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
