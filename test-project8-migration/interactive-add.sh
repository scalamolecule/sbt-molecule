#!/bin/bash
# Test adding new attributes (unambiguous - no Foo_migration.scala file needed)
# Adding attributes is unambiguous: Molecule auto-detects new attributes and generates appropriate SQL

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Source shared functions
source "$SCRIPT_DIR/shared.sh"

cd "$SCRIPT_DIR"

# Clean slate
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

echo "=========================================================================="
echo "Interactive migration workflow: Adding Attributes"
echo "=========================================================================="
echo "Working directory: $(pwd)"
echo ""
echo "This script will walk you through the migration workflow of adding an attribute step by step."
echo "In your editor you can inspect files and directories between each step to see what happens."
echo ""
echo "Next: We'll create an initial domain structure and set up the database."

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 1 of 2: Initial domain structure and database setup ====${NC}"
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
  val dbUrl = "jdbc:h2:file:./target/testdb"

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

    "V1 - Initial schema with name attribute" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V1 migration for Foo domain (clean = true wipes any existing data to start fresh)
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)

      // Insert test data with initial schema
      Person.name.age.insert(("John Doe", 30), ("Jane Smith", 25)).transact

      // Verify data
      Person.name.age.d1.query.get ==> List(("John Doe", 30), ("Jane Smith", 25))

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
echo "  • Test data inserted and verified"
echo ""
echo "Next: We'll add a new 'email' attribute to the domain structure"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 2 of 2: Add 'email' attribute ====${NC}"
echo ""
cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {
  trait Person {
    val name  = oneString
    val age   = oneInt
    val email = oneString
  }
}
EOF

echo "Updated domain structure:"
echo ""
cat << 'DISPLAY'
trait Person {
  val name  = oneString
  val age   = oneInt
  val email = oneString  // <- New attribute
}
DISPLAY

echo ""
echo "Re-generating molecule boilerplate code and creating a new Flyway version file:"
echo ""
echo "> sbt moleculeGen"
sbt moleculeGen

echo ""
echo -e "${GREEN}✓ Created Flyway migration file:${NC}"
echo "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql"
echo -e "${BLUE}"
cat src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql
echo -e "${NC}"

echo ""
echo "Now migrate with V2 and run test to use the new email attribute..."
echo ""

# Update the test file to use email attribute
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
  val dbUrl = "jdbc:h2:file:./target/testdb"

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

    "V2 - Add email attribute to existing data" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V2 migration on existing database (adds email column)
      // Note: clean = false (default) - Flyway applies only new migrations while preserving existing data
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")

      // Verify existing data is still there from V1
      Person.name.age.d1.query.get ==> List(("John Doe", 30), ("Jane Smith", 25))

      // Upsert existing John Doe row with email
      Person.name_("John Doe").email("doe@gmail.com").upsert.transact

      // Verify email was added to existing data
      Person.name_("John Doe").email.query.get ==> List("doe@gmail.com")

      println("✅ V2: Email attribute added to persisted data successfully")
    }
  }
}
EOF

echo "> sbt test"
sbt test
echo ""
echo "Results:"
echo "  • Detected that 'email' was added to domain structure"
echo "  • Created Flyway migration file V2__molecule_1_change.sql"
echo "  • Migrated database schema with Flyway migration file"
echo "  • Verified that persisted data from V1 was intact"
echo "  • Successfully upserted existing John Doe with email"
echo "  • Successfully queried email from persisted data"
echo ""
echo -e "${GREEN}✓ Test complete${NC}"
