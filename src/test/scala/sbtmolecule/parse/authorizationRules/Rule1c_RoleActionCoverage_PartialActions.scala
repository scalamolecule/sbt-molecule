package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with partial actions through multiple roles */

object Rule1c_EntityActionCoverage_PartialActions extends DomainStructure {
  trait Viewer extends Role with query
  trait Editor extends Role with update

  trait Post extends Viewer with Editor { // ❌ Only has query, update - missing save, insert, delete
    val content = oneString
  }
}

object Rule1c_EntityActionCoverage_PartialActionsTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1c: Entity with partial actions through multiple roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Missing actions: save, insert, delete"))
      assert(error.getMessage.contains("Available actions from roles: query, update"))
    }
  }
}
