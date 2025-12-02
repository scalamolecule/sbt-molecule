package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 10: .updating[R] grant on excluded role is ineffective
object Rule10_UpdatingOnExcluded extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with all

  trait Post extends Guest with Member {
    val content = oneString.exclude[Guest].updating[Guest]  // ❌ Guest is excluded
  }
}

object Rule10_UpdatingOnExcludedTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 10: .updating[R] grant on excluded role is ineffective") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Post.content'"))
      assert(error.getMessage.contains("'.updating[Guest]' grant is ineffective"))
      assert(error.getMessage.contains("excluded by '.exclude[Guest]'"))
    }
  }
}
