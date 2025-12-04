package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 5: Attribute .exclude[R] - role must be in entity roles
object Rule5_AttributeExclude extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with query with save with insert with update with delete

  trait Profile extends Member {
    val email = oneString.exclude[Guest]  // ❌ Guest not in entity roles
  }
}

object Rule5_AttributeExcludeTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 5: Attribute .exclude[R] - role must be in entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Profile.email'"))
      assert(error.getMessage.contains("Role 'Guest' in '.exclude[Guest]' is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Member"))
    }
  }
}
