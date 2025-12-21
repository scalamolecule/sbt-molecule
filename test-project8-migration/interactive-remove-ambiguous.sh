#!/bin/bash
# Test removing attributes with ambiguous removal (requires interactive resolution)
# When multiple attributes are removed and it's ambiguous which went where,
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
echo "Interactive migration workflow: Removing Attributes (Ambiguous)"
echo "=========================================================================="
echo "Working directory: $(pwd)"
echo ""
echo "This script demonstrates handling ambiguous attribute removals."
echo "When multiple attributes of the same type disappear, moleculeGen cannot"
echo "automatically determine which database column maps to which removed attribute."
echo ""
echo "Next: We'll create an initial domain structure with multiple String attributes."

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 1 of 4: Initial domain structure and database setup ====${NC}"
echo ""
echo "Starting with a domain structure with multiple String attributes:"
echo ""
cat << 'DISPLAY'
trait Person {
  val name     = oneString
  val nickname = oneString
  val age      = oneInt
}
DISPLAY

cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {
  trait Person {
    val name     = oneString
    val nickname = oneString
    val age      = oneInt
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

    "V1 - Initial schema with 3 attributes" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V1 migration for Foo domain (clean = true wipes any existing data to start fresh)
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)

      // Insert test data with all 3 attributes
      Person.name.nickname.age.insert(
        ("John Doe", "Johnny", 30),
        ("Jane Smith", "Janie", 25)
      ).transact

      // Verify data
      Person.name.nickname.age.d1.query.get ==> List(
        ("John Doe", "Johnny", 30),
        ("Jane Smith", "Janie", 25)
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
echo "  • Test data inserted with 3 attributes (name, nickname, age)"
echo ""
echo "Next: We'll remove 'nickname' without explicit marker, creating ambiguity"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 2 of 4: Remove attribute - creating ambiguity ====${NC}"
echo ""
echo "Now we'll remove 'nickname' from Foo.scala without using an explicit .remove marker."
echo "This creates an AMBIGUOUS situation since moleculeGen cannot determine if 'nickname' was:"
echo "  • Removed, or"
echo "  • Renamed to 'name' (the existing name column could have been the old nickname)"
echo ""
cat << 'DISPLAY'
trait Person {
  val name = oneString
  val age  = oneInt
  // nickname removed without .remove marker - AMBIGUOUS!
  // Was it removed, or did it become 'name'?
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
echo "Attempting to generate molecule code..."
echo ""
echo "> sbt moleculeGen"
sbt moleculeGen

echo ""
echo -e "${GREEN}✓ This error is EXPECTED - it's part of the migration workflow${NC}"
echo ""
echo "moleculeGen automatically created Foo_migration.scala with two migration options for 'nickname':"
echo ""
cat src/main/scala/app/domain/Foo_migration.scala
echo ""
echo "Both options are present for 'nickname', preventing compilation until you choose one."
echo ""
echo "Next: We'll resolve the ambiguity by keeping only the .remove option"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 3 of 4: Resolve ambiguity in Foo_migration.scala ====${NC}"
echo ""
echo "For this test, we want to REMOVE nickname, so we'll keep only the .remove line:"
echo ""
cat << 'DISPLAY'
trait Foo_migration extends Foo with DomainStructure {

  // Please choose intended migration commands:
  // (comment-out or delete unwanted option lines)

  trait PersonMigrations extends Person {
    val nickname = oneString.remove       // if removed
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
    val nickname = oneString.remove       // if removed
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
echo "  • Resolved ambiguity in Foo_migration.scala by keeping .remove option"
echo "  • Generated migration SQL with DROP COLUMN nickname"
echo "  • Foo_migration.scala was automatically deleted after successful generation"
echo ""
echo "Next: We'll apply the migration to the database and verify data integrity"

wait_for_user

echo ""
echo -e "${YELLOW}==== Step 4 of 4: Apply migration and verify ====${NC}"
echo ""
echo "Updating test to apply V2 migration and verify removed column..."

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

    "V2 - Remove nickname attribute from existing data" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn

      // Run V2 migration on existing database (removes nickname column)
      // Note: clean = false (default) - Flyway applies only new migrations while preserving existing data
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")

      // Verify existing name/age data is still there from V1
      Person.name.age.d1.query.get ==> List(("John Doe", 30), ("Jane Smith", 25))

      // Since 'nickname' was removed, querying for it is type-safely no longer possible!
      // Person.nickname // wouldn't compile anymore!

      println("✅ V2: Nickname attribute removed, remaining data preserved successfully")
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
echo "  • Nickname column dropped from Person table"
echo "  • Verified that persisted data from V1 (name, age) is intact"
echo "  • Successfully queried remaining attributes without nickname"
echo ""
echo -e "${GREEN}✓ Migration workflow complete!${NC}"
echo ""
echo "Summary of the ambiguous removal workflow:"
echo "  1. Initial schema with 3 attributes (name, nickname, age)"
echo "  2. Removed nickname without .remove marker - created ambiguity"
echo "  3. moleculeGen detected ambiguity: was nickname removed or renamed to name?"
echo "  4. moleculeGen auto-generated Foo_migration.scala with BOTH options (won't compile)"
echo "  5. We resolved ambiguity by keeping only the .remove line"
echo "  6. Re-ran moleculeGen, which generated migration SQL and cleaned up"
echo "  7. Applied migration to database, verified data integrity"
echo ""
echo "Key takeaway: When attributes disappear ambiguously, moleculeGen auto-generates"
echo "a migration file with all options. You must resolve by choosing one option per attribute."
