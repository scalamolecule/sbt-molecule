package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Role with only write actions - missing query action */

trait Rule1b_RoleActionCoverage_OnlyWrite extends DomainStructure {
  trait Member extends Role with save with insert with update with delete

  trait Post extends Member { // ❌ Only has save, insert, update, delete - missing query
    val content = oneString
  }
}

object Rule1b_RoleActionCoverage_OnlyWriteTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1b: Role with only write actions - missing query action") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Missing actions: query"))
      assert(error.getMessage.contains("Available actions from roles: delete, insert, save, update"))
    }
  }
}
