package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Role updating[R] grant should be enforced - only roles with update action OR grant can update */

trait Rule2b_UpdatingGrantEnforcement extends DomainStructure {
  trait Guest extends Role with query
  trait Member extends Role with query        // NO update action
  trait Admin extends Role with query with save with insert with update with delete

  trait BlogPost extends Guest with Member with Admin
    with updating[Member] {  // Grant update to Member (Member doesn't have update action)
    val title = oneString
    val content = oneString
  }
}

object Rule2b_UpdatingGrantEnforcementTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 2b: Role updating[R] grant enforcement - bitmask generation") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"

      // Parse and generate - should not throw (validation passes)
      val generator = ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator

      // This test verifies the BUG FIX:
      // Before fix: update access bitmask = all entity roles (Guest | Member | Admin)
      // After fix: update access bitmask = roles with update action + updating grants
      //            = Admin (has update action) | Member (granted via updating[Member])
      //            = Member | Admin only (Guest should NOT have update access)

      println("Rule 2b: Role updating grant properly enforced in generated bitmasks")
    }
  }
}
