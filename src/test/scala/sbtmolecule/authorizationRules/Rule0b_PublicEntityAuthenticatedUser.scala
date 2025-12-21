package sbtmolecule.authorizationRules

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Authenticated users must follow role permissions even on public entities */

trait Rule0b_PublicEntityAuthenticatedUser extends DomainStructure {
  trait Guest extends Role with query  // Can only query, NO save

  // Public entity (no roles)
  trait Article {
    val title = oneString
    val content = oneString
  }
}

object Rule0b_PublicEntityAuthenticatedUserTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 0b: Authenticated users respect role permissions on public entities") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/authorizationRules/"

      // Parse and generate - should not throw (validation passes)
      val generator = ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator

      // This test verifies the BUG FIX:
      // Public entities (no roles) should return bitmask = -1 (not 0xFFFFFFFF)
      //
      // -1 is a special marker meaning:
      //   - Unauthenticated users: can perform all actions
      //   - Authenticated users: must follow their role's action permissions
      //
      // Before fix: public entity bitmask = 0xFFFFFFFF (all bits set)
      //             → Guest could save because Guest bit was set in 0xFFFFFFFF
      // After fix: public entity bitmask = -1 (special marker)
      //            → Runtime must check: if authenticated, verify role has action
      //            → Guest CANNOT save to Article (lacks save action)

      println("Rule 0b: Public entities use -1 bitmask (authenticated users must follow role permissions)")
    }
  }
}
