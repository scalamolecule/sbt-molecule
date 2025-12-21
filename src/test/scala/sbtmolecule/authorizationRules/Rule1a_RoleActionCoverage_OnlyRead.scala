package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Role with only 'query' action - missing other actions */

trait Rule1a_RoleActionCoverage_OnlyRead extends DomainStructure {
  trait Member extends Role with query

  trait Post extends Member { // ❌ Only has query - missing save, insert, update, delete
    val content = oneString
  }
}

object Rule1a_RoleActionCoverage_OnlyReadTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1a: Role with only 'query' action - missing other actions") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Missing actions: save, insert, update, delete"))
      assert(error.getMessage.contains("Available actions from roles: query"))
    }
  }
}
