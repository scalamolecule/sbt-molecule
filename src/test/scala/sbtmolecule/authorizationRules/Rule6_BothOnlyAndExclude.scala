package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 6: Cannot use both .only and .exclude on same attribute
trait Rule6_BothOnlyAndExclude extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with query with save with insert with update with delete

  trait Profile extends Guest with Member {
    val email = oneString.only[Member].exclude[Guest]  // ❌ Can't use both
  }
}

object Rule6_BothOnlyAndExcludeTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 6: Cannot use both .only and .exclude on same attribute") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Cannot use both .only and .exclude on same attribute"))
      assert(error.getMessage.contains("Profile.email"))
    }
  }
}
