#!/bin/bash
# Test renaming attributes with ambiguous rename (requires interactive resolution)
# When an attribute is renamed without explicit marker and it's ambiguous which went where,
# we need to use Foo_migration.scala to resolve the ambiguity.

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
echo "Interactive migration workflow: Renaming Attributes (Ambiguous)"
echo "=========================================================================="
echo "Working directory: $(pwd)"
echo ""
echo "This script demonstrates handling ambiguous attribute renames."
echo "When 'name' is renamed to 'fullName' without using .becomes(fullName) marker,"
echo "moleculeGen cannot automatically determine if this is a rename or remove+add operation."
echo ""
echo "Next: We'll create an initial domain structure with a String attribute."

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 1 of 4: Initial domain structure and database setup ====${NC}"
echo ""
echo "Starting with a domain structure with name attribute:"
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
echo "  • Test data inserted with 2 attributes (name, age)"
echo ""
echo "Next: We'll rename 'name' to 'fullName' without explicit marker, creating ambiguity"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 2 of 4: Rename attribute - creating ambiguity ====${NC}"
echo ""
echo "Now we'll rename 'name' to 'fullName' without using an explicit .becomes marker."
echo "This creates an AMBIGUOUS situation since moleculeGen cannot determine if:"
echo "  • 'name' was renamed to 'fullName', or"
echo "  • 'name' was removed AND 'fullName' was added as a new attribute"
echo ""
cat << 'DISPLAY'
trait Person {
  val fullName = oneString  // <- 'name' renamed without .becomes marker - AMBIGUOUS!
  val age      = oneInt
}
DISPLAY

cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {
  trait Person {
    val fullName = oneString
    val age      = oneInt
  }
}
EOF

echo ""
echo "Attempting to generate molecule code..."
echo ""
echo "> sbt moleculeGen"
sbt moleculeGen

echo ""
echo -e "${GREEN}✓ This error is EXPECTED - it's part of the migration workflow${NC}"
echo ""
echo "moleculeGen automatically created Foo_migration.scala with migration options for both attributes:"
echo ""
cat src/main/scala/app/domain/Foo_migration.scala
echo ""
echo "Multiple options are present for each attribute, preventing compilation until you choose."
echo ""
echo "Next: We'll resolve the ambiguity by specifying the correct rename mappings"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 3 of 4: Resolve ambiguity in Foo_migration.scala ====${NC}"
echo ""
echo "For this test, we want to RENAME 'name' to 'fullName', so we'll keep only the .becomes line:"
echo ""
cat << 'DISPLAY'
trait Foo_migration extends Foo with DomainStructure {

  // Please choose intended migration commands:
  // (comment-out or delete unwanted option lines)

  trait PersonMigrations extends Person {
    val name = oneString.becomes(fullName) // if renamed: add new attribute like .becomes(otherAttr)
  }
}
DISPLAY

cat > src/main/scala/app/domain/Foo_migration.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo_migration extends Foo with DomainStructure {

  // Please choose intended migration commands:
  // (comment-out or delete unwanted option lines)

  trait PersonMigrations extends Person {
    val name = oneString.becomes(fullName) // if renamed: add new attribute like .becomes(otherAttr)
  }
}
EOF

echo ""
echo "After our choice, we can now call 'sbt moleculeGen' again to do an unambiguous migration:"
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
echo "Results:"
echo "  • Resolved ambiguity in Foo_migration.scala by keeping .becomes option"
echo "  • Generated migration SQL with ALTER TABLE ... RENAME COLUMN statement"
echo "  • Foo_migration.scala was automatically deleted after successful generation"
echo ""
echo "Next: We'll apply the migration to the database and verify data integrity"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 4 of 4: Apply migration and verify ====${NC}"
echo ""
echo "Updating test to apply V2 migration and verify renamed column..."

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
      // Person.name  // wouldn't compile anymore!

      println("✅ V2: Name attribute renamed to fullName, data preserved successfully")
    }
  }
}
EOF

echo ""
echo "> sbt test"
sbt test

echo ""
echo "Results:"
echo "  • Applied Flyway migration V2__molecule_1_change.sql"
echo "  • Name column renamed to fullName in Person table"
echo "  • Verified that persisted data from V1 is intact"
echo "  • Successfully queried data using new attribute name"
echo ""
echo -e "${GREEN}✓ Migration workflow complete!${NC}"
echo ""
echo "Summary of the ambiguous rename workflow:"
echo "  1. Initial schema with 2 attributes (name, age)"
echo "  2. Renamed 'name' to 'fullName' without .rename(\"fullName\") marker - created ambiguity"
echo "  3. moleculeGen detected ambiguity: was 'name' renamed or removed+added?"
echo "  4. moleculeGen auto-generated Foo_migration.scala with BOTH options (won't compile)"
echo "  5. We resolved ambiguity by keeping only the .becomes(fullName) line"
echo "  6. Re-ran moleculeGen, which generated migration SQL and cleaned up"
echo "  7. Applied migration to database, verified data integrity"
echo ""
echo "Key takeaway: When an attribute disappears without an explicit migration marker,"
echo "moleculeGen auto-generates a migration file with the two explicit options. You must resolve by"
echo "choosing either .remove (if removed) or .becomes(otherAttr) (if renamed)."
