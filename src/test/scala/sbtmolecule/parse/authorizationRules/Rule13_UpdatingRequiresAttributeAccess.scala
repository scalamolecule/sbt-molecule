package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 13: Entity updating[R] grant - role must have access to all attributes
object Rule13_UpdatingRequiresAttributeAccess extends DomainStructure {
  trait Member extends Role with query
  trait Admin extends Role with query with save with insert with update with delete

  trait Document extends Member with Admin
    with updating[(Member, Admin)] {  // ❌ Member has update grant but can't access secretNotes
    val title = oneString
    val secretNotes = oneString.only[Admin]
  }
}

object Rule13_UpdatingRequiresAttributeAccessTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 13: Entity updating[R] grant - role must have access to all attributes") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Document'"))
      assert(error.getMessage.contains("Entity grants updating to role 'Member' but attribute 'secretNotes' is restricted"))
      assert(error.getMessage.contains(".only[Admin]"))
      assert(error.getMessage.contains("A role cannot update an entity if it cannot access all attributes"))
    }
  }
}
