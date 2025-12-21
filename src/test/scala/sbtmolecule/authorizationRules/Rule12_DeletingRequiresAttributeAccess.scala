package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 12: Entity deleting[R] grant - role must have access to all attributes
trait Rule12_DeletingRequiresAttributeAccess extends DomainStructure {
  trait Member extends Role with query
  trait Admin extends Role with query with save with insert with update with delete

  trait Document extends Member with Admin
    with deleting[(Member, Admin)] {  // ❌ Member has delete grant but can't access secretNotes
    val title = oneString
    val secretNotes = oneString.only[Admin]
  }
}

object Rule12_DeletingRequiresAttributeAccessTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 12: Entity deleting[R] grant - role must have access to all attributes") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Document'"))
      assert(error.getMessage.contains("Entity grants deleting to role 'Member' but attribute 'secretNotes' is restricted"))
      assert(error.getMessage.contains(".only[Admin]"))
      assert(error.getMessage.contains("A role cannot delete an entity if it cannot access all attributes"))
    }
  }
}
