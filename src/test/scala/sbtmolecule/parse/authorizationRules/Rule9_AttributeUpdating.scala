package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 9: Attribute .updating[R] - role must be in entity roles
object Rule9_AttributeUpdating extends DomainStructure {
  trait Member extends Role with query with save with insert with update with delete
  trait Admin extends Role with query with save with insert with update with delete

  trait Draft extends Member {
    val title = oneString.updating[Admin]  // ❌ Admin not in entity roles
  }
}

object Rule9_AttributeUpdatingTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 9: Attribute .updating[R] - role must be in entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in attribute 'Draft.title'"))
      assert(error.getMessage.contains("Role 'Admin' in '.updating[Admin]' is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Member"))
    }
  }
}
