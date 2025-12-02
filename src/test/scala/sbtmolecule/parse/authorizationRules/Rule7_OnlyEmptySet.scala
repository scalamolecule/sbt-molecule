package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 7: .only[R] must not result in empty role set
object Rule7_OnlyEmptySet extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with all
  trait Admin extends Role with all

  trait Profile extends Guest with Member {
    val secret = oneString.only[Admin]  // ❌ Admin not in entity roles [Guest, Member]
  }
}

object Rule7_OnlyEmptySetTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 7: .only[R] must not result in empty role set") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Profile.secret'"))
      assert(error.getMessage.contains("Role 'Admin' in '.only[Admin]' is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Guest, Member"))
    }
  }
}
