package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with only 'write' role - missing read actions */

object Rule1b_EntityActionCoverage_OnlyWrite extends DomainStructure {
  trait Member extends Role with write

  trait Post extends Member { // ❌ Only has save, insert, update, delete - missing query, subscribe
    val content = oneString
  }
}

object Rule1b_EntityActionCoverage_OnlyWriteTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1b: Entity with only 'write' role - missing read actions") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Missing actions: query, subscribe"))
      assert(error.getMessage.contains("Available actions from roles: delete, insert, save, update"))
    }
  }
}
