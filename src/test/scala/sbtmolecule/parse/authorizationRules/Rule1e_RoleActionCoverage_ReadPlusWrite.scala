package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with query + write actions via multiple roles - should pass */

object Rule1e_EntityActionCoverage_ReadPlusWrite extends DomainStructure {
  trait Reader extends Role with query
  trait Writer extends Role with save with insert with update with delete

  trait Post extends Reader with Writer { // ✅ query (1) + write actions (4) = all 5 core actions
    val content = oneString
  }
}

object Rule1e_EntityActionCoverage_ReadPlusWriteTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1e: Entity with query + write actions via multiple roles - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      // Should not throw
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
