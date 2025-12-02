package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with only 'read' role - missing write actions */

object Rule1a_EntityActionCoverage_OnlyRead extends DomainStructure {
  trait Member extends Role with read

  trait Post extends Member { // ❌ Only has query, subscribe - missing save, insert, update, delete
    val content = oneString
  }
}

object Rule1a_EntityActionCoverage_OnlyReadTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1a: Entity with only 'read' role - missing write actions") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Missing actions: save, insert, update, delete"))
      assert(error.getMessage.contains("Available actions from roles: query, subscribe"))
    }
  }
}
