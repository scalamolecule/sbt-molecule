package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 3: Role deleting[R] grant - role must be in entity roles
trait Rule3_DeletingGrant extends DomainStructure {
  trait Member extends Role with query with save with insert with update with delete
  trait Admin extends Role with query with save with insert with update with delete

  trait Log extends Member
    with deleting[Admin] {  // ❌ Admin not in entity roles
    val entry = oneString
  }
}

object Rule3_DeletingGrantTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 3: Role deleting[R] grant - role must be in entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Log'"))
      assert(error.getMessage.contains("Role 'Admin' in 'deleting[Admin]' grant is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Member"))
    }
  }
}
