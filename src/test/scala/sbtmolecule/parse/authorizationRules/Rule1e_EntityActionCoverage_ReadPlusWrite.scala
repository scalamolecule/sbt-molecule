package sbtmolecule.parse.authorizationRules
import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

/** Test: Entity with 'read' + 'write' roles - should pass */

object Rule1e_EntityActionCoverage_ReadPlusWrite extends DomainStructure {
  trait Reader extends Role with read
  trait Writer extends Role with write

  trait Post extends Reader with Writer { // ✅ read (2) + write (4) = all 6 actions
    val content = oneString
  }
}

object Rule1e_EntityActionCoverage_ReadPlusWriteTest extends TestSuite {
  override def tests: Tests = Tests {
    test("Rule 1e: Entity with 'read' + 'write' roles - should pass") {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/authorizationRules/"
      // Should not throw
      ParseAndGenerate(path + getClass.getSimpleName.dropRight(5) + ".scala").generator
    }
  }
}
