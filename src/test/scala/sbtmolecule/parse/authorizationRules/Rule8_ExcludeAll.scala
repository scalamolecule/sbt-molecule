package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 8: .exclude[R] must not exclude all entity roles
object Rule8_ExcludeAll extends DomainStructure {
  trait Member extends Role with query with save with insert with update with delete

  trait Settings extends Member {
    val data = oneString.exclude[Member]  // ❌ Excludes all entity roles
  }
}

object Rule8_ExcludeAllTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 8: .exclude[R] must not exclude all entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Settings.data'"))
      assert(error.getMessage.contains("'.exclude[Member]' would result in no roles having access"))
    }
  }
}
