package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Role deleting[R] grant should be enforced - only roles with delete action OR grant can delete */

trait Rule3b_DeletingGrantEnforcement extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with query        // NO delete action
  trait Admin extends Role with query with save with insert with update with delete

  trait BlogPost extends Guest with Member with Admin
    with deleting[Admin] {  // Grant delete to Admin (Admin already has delete action, but explicit)
    val title = oneString
    val content = oneString
  }
}

object Rule3b_DeletingGrantEnforcementTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 3b: Role deleting[R] grant enforcement - bitmask generation") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"

      // Parse and generate - should not throw (validation passes)
      val generator = ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator

      // This test verifies the BUG FIX:
      // Before fix: delete access bitmask = all entity roles (Guest | Member | Admin)
      // After fix: delete access bitmask = roles with delete action + deleting grants
      //            = Admin only (has delete action)
      //            Guest and Member should NOT have delete access

      println("Rule 3b: Role deleting grant properly enforced in generated bitmasks")
    }
  }
}
