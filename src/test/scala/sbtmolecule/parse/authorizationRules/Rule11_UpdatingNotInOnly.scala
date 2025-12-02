package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 11: .updating[R] grant on role not in .only[R] list is ineffective
object Rule11_UpdatingNotInOnly extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with all
  trait Admin extends Role with all

  trait Post extends Guest with Member with Admin {
    val content = oneString.only[Admin].updating[Member]  // ❌ Member not in only list
  }
}

object Rule11_UpdatingNotInOnlyTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 11: .updating[R] grant on role not in .only[R] list is ineffective") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Post.content'"))
      assert(error.getMessage.contains("'.updating[Member]' grant is ineffective"))
      assert(error.getMessage.contains("excluded by '.only[Admin]'"))
    }
  }
}
