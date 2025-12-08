package sbtmolecule.parse.customColumnProps

import molecule.DomainStructure
import sbtmolecule.ParseAndGenerate
import utest._

object CustomDbColumnProperties extends DomainStructure {

  // Custom database column properties for this domain
  generalDbColumnProperties(
    Db.H2 -> Set(
      oneString -> "VARCHAR(100)",
      (oneDouble, "DECIMAL(10,2)")
    ),
    (Db.SQLite, Set(
      oneString -> "NVARCHAR(150)",
      (oneDouble, "DECIMAL(10,2)")
    ))
  )

  trait SpecialEntity {

    /** Normal default database column properties for Scala type String:
     *
     * H2         LONGVARCHAR
     * MariaDB    LONGTEXT
     * MySQL      LONGTEXT COLLATE utf8mb4_0900_as_cs
     * PostgreSQL TEXT COLLATE ucs_basic
     * SQLite     TEXT
     *
     * New defaults from general custom db types defined above:
     *
     * H2         VARCHAR(100)
     * SQLite     NVARCHAR(150)
     */
    val defaultString = oneString

    // Special properties for this attribute only in H2
    val shortString = oneString.dbColumnProperties(Db.H2 -> "VARCHAR(20)")

    // Special properties for this attribute only in H2 and SQLite
    val multiple = oneString.dbColumnProperties(
      Db.H2 -> "VARCHAR(20)",
      Db.SQLite -> "NVARCHAR(20)"
    )

    /** Normal default database column properties for Scala type Double:
     *
     * H2         DOUBLE PRECISION
     * MariaDB    DOUBLE
     * MySQL      DOUBLE
     * PostgreSQL DOUBLE PRECISION
     * SQLite     REAL
     *
     * New defaults from general custom db types defined above:
     *
     * H2         DECIMAL(10,2)
     * SQLite     DECIMAL(10,2)
     */
    val price = oneDouble
  }
}

object CustomDbColumnPropertiesTest extends TestSuite {
  val path       = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/customColumnProps/"
  val generator  = ParseAndGenerate(path + "CustomDbColumnProps.scala").generator
  val withSchema = (schema: String) => (check: String, errorMsg: String) => {
    // Normalize whitespace (replace multiple spaces with single space) for comparison
    val normalizedSql   = schema.replaceAll("\\s+", " ")
    val normalizedCheck = check.replaceAll("\\s+", " ")
    if (!normalizedSql.contains(normalizedCheck)) {
      println(schema)
      throw new Exception(errorMsg)
    }
  }

  override def tests: Tests = Tests {

    test("H2 custom column properties with precedence") {
      val h2Sql = sbtmolecule.db.resolvers.Db_H2(generator.metaDomain).getSQL
      val check = withSchema(h2Sql)

      // Test general custom property for oneString -> VARCHAR(100)
      check("defaultString VARCHAR(100)",
        "Expected 'defaultString VARCHAR(100)' in H2 schema (general custom property)")

      // Test attribute-specific override (highest priority) -> VARCHAR(20)
      check("shortString VARCHAR(20)",
        "Expected 'shortString VARCHAR(20)' - attribute-specific should override general")

      // Test another attribute-specific override
      check("multiple VARCHAR(20)",
        "Expected 'multiple VARCHAR(20)' - attribute-specific should override general")

      // Test general custom property for oneDouble -> DECIMAL(10,2)
      check("price DECIMAL(10,2)",
        "Expected 'price DECIMAL(10,2)' from general custom properties")
    }

    test("SQLite custom column properties with precedence") {
      val sqliteSql = sbtmolecule.db.resolvers.Db_SQlite(generator.metaDomain).getSQL
      val check     = withSchema(sqliteSql)

      // Test general custom property for oneString -> NVARCHAR(150)
      check("defaultString NVARCHAR(150)",
        "Expected 'defaultString NVARCHAR(150)' in SQLite schema (general custom property)")

      // Test attribute-specific override (highest priority)
      check("multiple NVARCHAR(20)",
        "Expected 'multiple NVARCHAR(20)' - attribute-specific should override general")

      // Test fallback to general when no attribute-specific property exists
      // shortString only has H2 custom property, so should use general NVARCHAR(150) for SQLite
      check("shortString NVARCHAR(150)",
        "Expected 'shortString NVARCHAR(150)' - should fall back to general custom property")

      // Test general custom property for oneDouble -> DECIMAL(10,2)
      check("price DECIMAL(10,2)",
        "Expected 'price DECIMAL(10,2)' from general custom properties")
    }

    test("PostgreSQL uses default types when no custom properties defined") {
      val postgresqlSql = sbtmolecule.db.resolvers.Db_PostgreSQL(generator.metaDomain).getSQL
      val check         = withSchema(postgresqlSql)

      // Verify all attributes use default Molecule types (no custom properties for PostgreSQL)
      check("defaultString TEXT COLLATE ucs_basic",
        "Expected 'defaultString TEXT COLLATE ucs_basic' - default Molecule type")

      check("shortString TEXT COLLATE ucs_basic",
        "Expected 'shortString TEXT COLLATE ucs_basic' - default Molecule type")

      check("multiple TEXT COLLATE ucs_basic",
        "Expected 'multiple TEXT COLLATE ucs_basic' - default Molecule type")

      check("price DOUBLE PRECISION",
        "Expected 'price DOUBLE PRECISION' - default Molecule type")
    }
  }
}
