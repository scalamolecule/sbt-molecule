package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 4: Attribute .only[R] - role must be in entity roles
object Rule4_AttributeOnly extends DomainStructure {
  trait Member extends Role with all
  trait Admin extends Role with all

  trait Settings extends Member {
    val apiKey = oneString.only[Admin]  // ❌ Admin not in entity roles
  }
}

object Rule4_AttributeOnlyTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 4: Attribute .only[R] - role must be in entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Settings.apiKey'"))
      assert(error.getMessage.contains("Role 'Admin' in '.only[Admin]' is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Member"))
    }
  }
}
