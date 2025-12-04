package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with all 5 core CRUD actions - should pass */

object Rule1d_EntityActionCoverage_AllActions extends DomainStructure {
  trait Admin extends Role with query with save with insert with update with delete

  trait Post extends Admin { // ✅ Has all 5 core actions
    val content = oneString
  }
}

object Rule1d_EntityActionCoverage_AllActionsTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1d: Entity with all 5 core CRUD actions - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      // Should not throw
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
