package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with 'all' actions - should pass */

object Rule1d_EntityActionCoverage_AllActions extends DomainStructure {
  trait Admin extends Role with all

  trait Post extends Admin { // ✅ Has all 6 actions
    val content = oneString
  }
}

object Rule1d_EntityActionCoverage_AllActionsTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1d: Entity with 'all' actions - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      // Should not throw
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
