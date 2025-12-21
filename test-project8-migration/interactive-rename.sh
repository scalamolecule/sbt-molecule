#!/bin/bash
# Test renaming attributes with explicit migration marking

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Source shared functions
source "$SCRIPT_DIR/shared.sh"

cd "$SCRIPT_DIR"

# Clean slate - start fresh
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

echo "=========================================================================="
echo "Interactive migration workflow: Renaming Attributes"
echo "=========================================================================="
echo "Working directory: $(pwd)"
echo ""
echo "This script will walk you through the migration workflow of renaming an attribute step by step."
echo "In your editor you can inspect files and directories between each step to see what happens."
echo ""
echo "Next: We'll create an initial domain structure and set up the database."

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 1 of 3: Initial domain structure and database setup ====${NC}"
echo ""
echo "Starting with a clean domain structure:"
echo ""
cat << 'DISPLAY'
trait Person {
  val name = oneString
  val age  = oneInt
}
DISPLAY

cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
EOF

echo ""
echo "Creating initial test file..."

cat > src/test/scala/app/Test.scala << 'EOF'
package app

import java.sql.{Connection, DriverManager}
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {

  // Using persistent H2 database for migration testing
  val dbDir = "target/testdb"
  val dbUrl = s"jdbc:h2:file:./$dbDir"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")

    // Use persistent connection
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()

    if (clean) {
      flyway.clean()  // Clean for testing purposes
    }
    flyway.migrate()
  }

  override def tests: Tests = Tests {

    "V1 - Initial schema with 2 attributes" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V1 migration for Foo domain (clean = true wipes any existing data to start fresh)
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)

      // Insert test data with original attribute names
      Person.name.age.insert(
        ("John Doe", 30),
        ("Jane Smith", 25)
      ).transact

      // Verify data
      Person.name.age.d1.query.get ==> List(
        ("John Doe", 30),
        ("Jane Smith", 25)
      )

      println("✅ V1: Initial data inserted successfully")
    }
  }
}
EOF

echo ""
echo "Generating molecule boilerplate code and initial V1 Flyway SQL schema:"
echo ""
echo "> sbt moleculeGen"
sbt moleculeGen

echo ""
echo "Now running tests to create H2 database and apply V1 migration..."
echo ""
echo "> sbt test"
sbt test

echo ""
echo "Results:"
echo "  • H2 database created with V1 schema"
echo "  • Flyway applied V1__initial_schema.sql"
echo "  • Test data inserted with 2 attributes"
echo ""
echo "Next: We'll mark 'name' for renaming to 'fullName'"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 2 of 3: Mark 'name' for renaming to 'fullName' and generate migration ====${NC}"
echo ""
echo "Mark 'name' for renaming by adding .rename(\"fullName\") to the attribute:"
echo ""
cat << 'DISPLAY'
trait Person {
  val name = oneString.rename("fullName")  // <- Mark for rename
  val age  = oneInt
}
DISPLAY

cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {
  trait Person {
    val name = oneString.rename("fullName")
    val age  = oneInt
  }
}
EOF

echo ""
echo "Re-generating molecule boilerplate code and creating a new Flyway version file."
echo ""
echo "Note: moleculeGen will:"
echo "  1. Automatically clean up the .rename marker from domain structure (no prompt)"
echo "  2. Prompt to rename attribute usages across the codebase"
echo "We'll automatically accept the rename prompt using MOLECULE_AUTO_RENAME_IN_CODE=true."
echo ""
echo "> MOLECULE_AUTO_RENAME_IN_CODE=true sbt moleculeGen"
MOLECULE_AUTO_RENAME_IN_CODE=true sbt moleculeGen

echo ""
echo -e "${GREEN}✓ Created Flyway migration file:${NC}"
echo "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql"
echo -e "${BLUE}"
cat src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql
echo -e "${NC}"
echo ""
echo "Results:"
echo "  • Detected .rename(\"fullName\") marker on 'name' attribute"
echo "  • Generated migration SQL V2__molecule_1_change.sql"
echo "  • SQL will rename the column when applied"
echo "  • Automatically cleaned up the .rename marker from domain structure"
echo "  • Automatically renamed attribute usages in codebase (name -> fullName)"
echo ""
echo "Current domain structure after cleanup:"
echo ""
cat src/main/scala/app/domain/Foo.scala
echo ""
echo "Next: We'll apply the migration to the database and verify data integrity"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 3 of 3: Apply migration to database ====${NC}"
echo ""
echo "Now we'll apply the migration to the database and verify data integrity."
echo ""
echo "The test file was automatically updated by moleculeGen in Step 2."
echo "All references to 'Person.name' were renamed to 'Person.fullName'."
echo ""
echo "Creating V2 test to verify migration..."
echo ""

cat > src/test/scala/app/Test.scala << 'EOF'
package app

import java.sql.{Connection, DriverManager}
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {

  // Using persistent H2 database for migration testing
  val dbDir = "target/testdb"
  val dbUrl = s"jdbc:h2:file:./$dbDir"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")

    // Use persistent connection
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()

    if (clean) {
      flyway.clean()  // Clean for testing purposes
    }
    flyway.migrate()
  }

  override def tests: Tests = Tests {

    "V2 - Rename name attribute to fullName" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V2 migration on existing database (renames name column to fullName)
      // Note: clean = false (default) - Flyway applies only new migrations while preserving existing data
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")

      // Verify existing data is still there from V1, now accessible via 'fullName'
      Person.fullName.age.d1.query.get ==> List(("John Doe", 30), ("Jane Smith", 25))

      // Since 'name' was renamed, querying for it is type-safely no longer possible!
      // Person.name // wouldn't compile anymore!

      println("✅ V2: Name attribute renamed to fullName, data preserved successfully")
    }
  }
}
EOF

echo "> sbt test"
sbt test

echo ""
echo "Results:"
echo "  • Applied Flyway migration V2__molecule_1_change.sql"
echo "  • Name column renamed to fullName in Person table"
echo "  • Verified that persisted data from V1 is intact"
echo "  • Successfully queried data using new attribute name"
echo "  • Domain structure was already cleaned up in Step 2"
echo ""
echo -e "${GREEN}✓ Migration workflow complete!${NC}"
echo ""
echo "Summary of the complete workflow:"
echo "  1. Initial schema with 2 attributes (name, age)"
echo "  2. Marked name with .rename(\"fullName\"), generated V2 SQL, cleaned up marker, renamed in code"
echo "  3. Applied migration to database, verified data integrity"
echo ""
echo "The domain structure now matches the migrated database schema."
