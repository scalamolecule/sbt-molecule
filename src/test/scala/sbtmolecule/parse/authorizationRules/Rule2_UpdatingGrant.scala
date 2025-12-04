package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 2: Role updating[R] grant - role must be in entity roles
object Rule2_UpdatingGrant extends DomainStructure {
  trait Member extends Role with query with save with insert with update with delete
  trait Admin extends Role with query with save with insert with update with delete

  trait Post extends Member
    with updating[Admin] {  // ❌ Admin not in entity roles
    val content = oneString
  }
}

object Rule2_UpdatingGrantTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 2: Role updating[R] grant - role must be in entity roles") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Post'"))
      assert(error.getMessage.contains("Role 'Admin' in 'updating[Admin]' grant is not in the entity roles"))
      assert(error.getMessage.contains("Entity roles: Member"))
    }
  }
}
