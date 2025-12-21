#!/bin/bash
# Non-interactive comprehensive test of all migration workflows
# Tests all 5 migration workflows: add, rename (unambiguous), rename (ambiguous), remove (unambiguous), remove (ambiguous)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Cleanup function for Ctrl-C
cleanup() {
    echo ""
    echo -e "${YELLOW}Interrupted by user. Cleaning up...${NC}"

    # Restore original state
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

    cat > src/test/scala/app/Test.scala << 'EOF'
package app
import utest.*
object Test extends TestSuite {
  override def tests: Tests = Tests {}
}
EOF

    rm -f src/main/scala/app/domain/Foo_migration.scala
    rm -rf target/
    rm -rf src/main/resources/db/

    echo -e "${GREEN}✓ Project restored${NC}"
    echo ""
    echo "========================================="
    echo "Test Results (interrupted)"
    echo "========================================="
    echo "Total tests:  $TOTAL_TESTS"
    echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
    if [ $FAILED_TESTS -gt 0 ]; then
        echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
    else
        echo "Failed:       0"
    fi
    echo -e "Interrupted:  ${YELLOW}$((TOTAL_TESTS - PASSED_TESTS - FAILED_TESTS))${NC}"
    echo ""
    exit 130
}

# Trap Ctrl-C (SIGINT) and call cleanup
trap cleanup INT

cd "$(dirname "$0")"

echo "========================================="
echo "Comprehensive Migration Workflow Test"
echo "========================================="
echo ""

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Helper to run sbt command
run_sbt() {
    echo ""
    sbt moleculeGen
    return $?
}

# Helper to run sbt test command
run_sbt_test() {
    echo ""
    sbt test
    return $?
}

# Test function
run_test() {
    local test_name=$1
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "Testing: $test_name ... "
}

pass_test() {
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${GREEN}✓${NC}"
}

fail_test() {
    local reason=$1
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${RED}✗${NC} ($reason)"
}

# Clean slate
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

echo "=== Workflow 1: ADD Attribute (unambiguous) ==="
echo ""

# 1.1: Initial generation
run_test "1.1 Initial schema generation"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V1 - Initial schema" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)
      Person.name.age.insert(("John", 30), ("Jane", 25)).transact
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -eq 0 ]; then
    pass_test
else
    fail_test "Initial generation failed"
fi

# 1.2: Add attribute
run_test "1.2 Add email attribute"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V2 - Add email" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
      Person.name_("John").email("john@example.com").upsert.transact
      Person.name_("John").email.query.get ==> List("john@example.com")
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -eq 0 ] && [ -f "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql" ]; then
    pass_test
else
    fail_test "V2 not created or test failed"
fi

echo ""
echo "=== Workflow 2: RENAME Attribute (unambiguous with .rename marker) ==="
echo ""

# Clean for new workflow
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

# 2.1: Initial generation
run_test "2.1 Initial schema for rename workflow"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V1 - Initial schema" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)
      Person.name.age.insert(("John", 30), ("Jane", 25)).transact
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -ne 0 ]; then
    fail_test "Initial generation failed"
    TOTAL_TESTS=$((TOTAL_TESTS - 1))
else
    pass_test
fi

# 2.2: Rename with marker
run_test "2.2 Rename name to fullName with .rename marker"
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

MOLECULE_AUTO_RENAME_IN_CODE=true run_sbt

if [ $? -eq 0 ] && [ -f "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql" ]; then
    # Verify marker was cleaned up
    if grep -q '.rename(' src/main/scala/app/domain/Foo.scala; then
        fail_test "Marker not cleaned up"
    else
        pass_test
    fi
else
    fail_test "V2 not created"
fi

# 2.3: Apply rename migration
run_test "2.3 Apply rename migration to database"
cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V2 - Rename name to fullName" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")
      Person.fullName.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt_test
if [ $? -eq 0 ]; then
    pass_test
else
    fail_test "Migration test failed"
fi

echo ""
echo "=== Workflow 3: REMOVE Attribute (unambiguous with .remove marker) ==="
echo ""

# Clean for new workflow
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

# 3.1: Initial generation with email
run_test "3.1 Initial schema with email for remove workflow"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V1 - Initial schema with email" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)
      Person.name.age.email.insert(("John", 30, "john@example.com"), ("Jane", 25, "jane@example.com")).transact
      Person.name.age.email.d1.query.get ==> List(("John", 30, "john@example.com"), ("Jane", 25, "jane@example.com"))
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -ne 0 ]; then
    fail_test "Initial generation failed"
    TOTAL_TESTS=$((TOTAL_TESTS - 1))
else
    pass_test
fi

# 3.2: Remove with marker
run_test "3.2 Remove email with .remove marker"
cat > src/main/scala/app/domain/Foo.scala << 'EOF'
package app.domain
import molecule.DomainStructure
trait Foo extends DomainStructure {
  trait Person {
    val name  = oneString
    val age   = oneInt
    val email = oneString.remove
  }
}
EOF

run_sbt

if [ $? -eq 0 ] && [ -f "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql" ]; then
    # Verify marker was cleaned up and email attribute removed
    if grep -q 'email' src/main/scala/app/domain/Foo.scala; then
        fail_test "Attribute not removed"
    else
        pass_test
    fi
else
    fail_test "V2 not created"
fi

# 3.3: Apply remove migration
run_test "3.3 Apply remove migration to database"
cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V2 - Remove email" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt_test
if [ $? -eq 0 ]; then
    pass_test
else
    fail_test "Migration test failed"
fi

echo ""
echo "=== Workflow 4: RENAME Attribute (ambiguous - requires Foo_migration.scala) ==="
echo ""

# Clean for new workflow
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

# 4.1: Initial generation
run_test "4.1 Initial schema for ambiguous rename"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V1 - Initial schema" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)
      Person.name.age.insert(("John", 30), ("Jane", 25)).transact
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -ne 0 ]; then
    fail_test "Initial generation failed"
    TOTAL_TESTS=$((TOTAL_TESTS - 1))
else
    pass_test
fi

# 4.2: Ambiguous rename detection
run_test "4.2 Detect ambiguous rename (name -> fullName)"
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

run_sbt
if [ $? -ne 0 ] && [ -f "src/main/scala/app/domain/Foo_migration.scala" ]; then
    pass_test
else
    fail_test "Ambiguity not detected or migration file not created"
fi

# 4.3: Resolve ambiguous rename
run_test "4.3 Resolve ambiguous rename with Foo_migration.scala"
cat > src/main/scala/app/domain/Foo_migration.scala << 'EOF'
package app.domain
import molecule.DomainStructure
trait Foo_migration extends Foo with DomainStructure {
  trait PersonMigrations extends Person {
    val name = oneString.becomes(fullName)
  }
}
EOF

MOLECULE_AUTO_RENAME_IN_CODE=true run_sbt

if [ $? -eq 0 ] && [ -f "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql" ]; then
    # Verify migration file was removed
    if [ -f "src/main/scala/app/domain/Foo_migration.scala" ]; then
        fail_test "Migration file not removed"
    else
        pass_test
    fi
else
    fail_test "V2 not created"
fi

# 4.4: Apply ambiguous rename migration
run_test "4.4 Apply ambiguous rename migration to database"
cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V2 - Rename name to fullName" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")
      Person.fullName.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt_test
if [ $? -eq 0 ]; then
    pass_test
else
    fail_test "Migration test failed"
fi

echo ""
echo "=== Workflow 5: REMOVE Attribute (ambiguous - requires Foo_migration.scala) ==="
echo ""

# Clean for new workflow
rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/

# 5.1: Initial generation with email
run_test "5.1 Initial schema for ambiguous remove"
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V1 - Initial schema with email" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2", clean = true)
      Person.name.age.email.insert(("John", 30, "john@example.com"), ("Jane", 25, "jane@example.com")).transact
      Person.name.age.email.d1.query.get ==> List(("John", 30, "john@example.com"), ("Jane", 25, "jane@example.com"))
    }
  }
}
EOF

run_sbt && run_sbt_test
if [ $? -ne 0 ]; then
    fail_test "Initial generation failed"
    TOTAL_TESTS=$((TOTAL_TESTS - 1))
else
    pass_test
fi

# 5.2: Ambiguous remove detection
run_test "5.2 Detect ambiguous remove (email missing)"
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

run_sbt
if [ $? -ne 0 ] && [ -f "src/main/scala/app/domain/Foo_migration.scala" ]; then
    pass_test
else
    fail_test "Ambiguity not detected or migration file not created"
fi

# 5.3: Resolve ambiguous remove
run_test "5.3 Resolve ambiguous remove with Foo_migration.scala"
cat > src/main/scala/app/domain/Foo_migration.scala << 'EOF'
package app.domain
import molecule.DomainStructure
trait Foo_migration extends Foo with DomainStructure {
  trait PersonMigrations extends Person {
    val email = oneString.remove
  }
}
EOF

run_sbt

if [ $? -eq 0 ] && [ -f "src/main/resources/db/migration/app/domain/Foo/h2/V2__molecule_1_change.sql" ]; then
    # Verify migration file was removed
    if [ -f "src/main/scala/app/domain/Foo_migration.scala" ]; then
        fail_test "Migration file not removed"
    else
        pass_test
    fi
else
    fail_test "V2 not created"
fi

# 5.4: Apply ambiguous remove migration
run_test "5.4 Apply ambiguous remove migration to database"
cat > src/test/scala/app/Test.scala << 'EOF'
package app
import java.sql.DriverManager
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.facade.JdbcConn_JVM
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import org.flywaydb.core.Flyway
import utest.*

object Test extends TestSuite {
  val dbUrl = "jdbc:h2:file:./target/testdb"

  def getConn: JdbcConn_JVM = {
    Class.forName("org.h2.Driver")
    val metaDb = Foo_h2()
    val proxy = JdbcProxy(dbUrl, metaDb)
    val sqlConn = DriverManager.getConnection(dbUrl, "sa", "")
    JdbcConn_JVM(proxy, sqlConn)
  }

  def runMigrations(migrationsPath: String, clean: Boolean = false): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, "sa", "")
      .locations(s"filesystem:$migrationsPath")
      .cleanDisabled(false)
      .load()
    if (clean) flyway.clean()
    flyway.migrate()
  }

  override def tests: Tests = Tests {
    "V2 - Remove email" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn
      runMigrations("src/main/resources/db/migration/app/domain/Foo/h2")
      Person.name.age.d1.query.get ==> List(("John", 30), ("Jane", 25))
    }
  }
}
EOF

run_sbt_test
if [ $? -eq 0 ]; then
    pass_test
else
    fail_test "Migration test failed"
fi

echo ""
echo "=== Cleanup ==="
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

cat > src/test/scala/app/Test.scala << 'EOF'
package app
import utest.*
object Test extends TestSuite {
  override def tests: Tests = Tests {}
}
EOF

rm -f src/main/scala/app/domain/Foo_migration.scala
rm -rf target/
rm -rf src/main/resources/db/
sbt moleculeGen > /dev/null 2>&1
echo -e "${GREEN}✓ Project restored${NC}"

echo ""
echo "========================================="
echo "Test Results"
echo "========================================="
echo "Total tests:  $TOTAL_TESTS"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
else
    echo "Failed:       0"
fi
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
