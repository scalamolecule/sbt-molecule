package sbtmolecule.parse.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest.*

// Rule 14: Entity deleting[R] grant - role must have access to all attributes (exclude case)
object Rule14_DeletingRequiresAttributeAccessExclude extends DomainStructure {
  trait Member extends Role with query
  trait Admin extends Role with query with save with insert with update with delete

  trait Document extends Member with Admin
    with deleting[(Member, Admin)] {  // ❌ Member has delete grant but is excluded from secretNotes
    val title = oneString
    val secretNotes = oneString.exclude[Member]
  }
}

object Rule14_DeletingRequiresAttributeAccessExcludeTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 14: Entity deleting[R] grant - role must have access to all attributes (exclude case)") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      val error = intercept[Exception] {
        ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
      }
      assert(error.getMessage.contains("Authorization error in entity 'Document'"))
      assert(error.getMessage.contains("Entity grants deleting to role 'Member' but attribute 'secretNotes' is restricted"))
      assert(error.getMessage.contains(".exclude[Member]"))
      assert(error.getMessage.contains("A role cannot delete an entity if it cannot access all attributes"))
    }
  }
}
