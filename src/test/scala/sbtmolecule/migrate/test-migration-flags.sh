#!/bin/bash

# Test script for migration workflow
# Tests initMigrations and deleteMigrations subcommands
#
# Note: This script is slow because each sbt invocation has startup time.
# For faster testing during development, run commands manually.
# This script is mainly for comprehensive CI testing.

set -e  # Exit on error

PROJECT_DIR="test-project1-basic"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "Testing Molecule Migration Workflow"
echo "=========================================="
echo ""

# Helper function to check if a file exists
check_file_exists() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} File exists: $1"
    else
        echo -e "${RED}✗${NC} File missing: $1"
        exit 1
    fi
}

# Helper function to check if a file does NOT exist
check_file_not_exists() {
    if [ ! -f "$1" ]; then
        echo -e "${GREEN}✓${NC} File does not exist: $1"
    else
        echo -e "${RED}✗${NC} File should not exist: $1"
        exit 1
    fi
}

# Helper function to count migrations
count_migrations() {
    local dir="$1"
    if [ -d "$dir" ]; then
        find "$dir" -name "V*.sql" | wc -l | tr -d ' '
    else
        echo "0"
    fi
}

# Helper function to run sbt command
run_sbt() {
    echo -e "> sbt $1"
    (cd "$PROJECT_DIR" && sbt "$1")
    local exit_code=$?
    if [ $exit_code -ne 0 ]; then
        echo -e "${RED}✗${NC} sbt command failed with exit code $exit_code"
        exit $exit_code
    fi
}

# Helper function to reset domain files and clean up migrations
reset_project() {
    # Reset Foo.scala to clean state
    cat > "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala" << 'EOF'
package app.domain

import molecule.DomainStructure

trait Foo extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
EOF
    # Reset Bar.scala to clean state
    cat > "$PROJECT_DIR/src/main/scala/app/domain/Bar.scala" << 'EOF'
package app.domain

import molecule.DomainStructure

trait Bar extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
EOF
    # Remove any migration files and generated resources
    rm -f "$PROJECT_DIR/src/main/scala/app/domain/Foo_migration.scala"
    rm -f "$PROJECT_DIR/src/main/scala/app/domain/Bar_migration.scala"
    rm -rf "$PROJECT_DIR/src/main/resources/db/migration"
    rm -rf "$PROJECT_DIR/src/main/resources/db/schema"
}

echo "Step 1: Clean slate - reset domain files and remove migrations"
echo "----------------------------------------------"
reset_project
echo -e "${GREEN}✓${NC} Reset domain files and cleaned migration/schema directories"
echo ""

echo "Step 2: Initial generation (no migrations)"
echo "----------------------------------------------"
run_sbt "moleculeGen"
check_file_exists "$PROJECT_DIR/src/main/resources/db/schema/app/domain/Foo/Foo_postgresql.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/schema/app/domain/Bar/Bar_postgresql.sql"
check_file_not_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql/V1__initial_schema.sql"
echo -e "${GREEN}✓${NC} Schema files generated, no migrations created"
echo ""

echo "Step 3: Initialize migrations for Foo"
echo "----------------------------------------------"
run_sbt "moleculeGen initMigrations Foo"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql/V1__initial_schema.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/h2/V1__initial_schema.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/Foo_previous.scala"
check_file_not_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Bar/postgresql/V1__initial_schema.sql"
echo -e "${GREEN}✓${NC} Foo migrations initialized, Bar untouched"
echo ""
echo "Migration status after Step 3:"
echo "> sbt moleculeMigrationStatus"
(cd "$PROJECT_DIR" && sbt moleculeMigrationStatus)
echo ""

echo "Step 4: Check migration status"
echo "----------------------------------------------"
echo "> sbt moleculeMigrationStatus"
cd "$PROJECT_DIR" && sbt moleculeMigrationStatus | grep "app.domain.Foo - latest: V1__initial_schema.sql" > /dev/null
cd ..
echo -e "${GREEN}✓${NC} Migration status shows Foo with V1"
echo ""

echo "Step 5: Initialize migrations for all domains"
echo "----------------------------------------------"
run_sbt "moleculeGen initMigrations"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Bar/postgresql/V1__initial_schema.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Bar/Bar_previous.scala"
echo -e "${GREEN}✓${NC} All domains now have migrations"
echo ""
echo "Migration status after Step 5:"
echo "> sbt moleculeMigrationStatus"
(cd "$PROJECT_DIR" && sbt moleculeMigrationStatus)
echo ""

echo "Step 6: Add attribute to Foo and verify auto-migration"
echo "----------------------------------------------"
echo "Counting current migrations..."
BEFORE_COUNT=$(count_migrations "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql")
echo "Adding city attribute to Foo/Person..."
cp "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala" "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala.bak"
sed -i '' 's/val age  = oneInt/val age  = oneInt\
    val city = oneString/' "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala"
run_sbt "moleculeGen"
AFTER_COUNT=$(count_migrations "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql")
EXPECTED_COUNT=$((BEFORE_COUNT + 1))
if [ "$AFTER_COUNT" -eq "$EXPECTED_COUNT" ]; then
    echo -e "${GREEN}✓${NC} Migration auto-generated for Foo (was: $BEFORE_COUNT, now: $AFTER_COUNT)"
else
    echo -e "${RED}✗${NC} Expected $EXPECTED_COUNT migrations, found: $AFTER_COUNT"
    exit 1
fi
echo ""

echo "Step 7: Remove test attribute and clean up"
echo "----------------------------------------------"
echo "Restoring original Foo.scala..."
mv "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala.bak" "$PROJECT_DIR/src/main/scala/app/domain/Foo.scala"
echo "Deleting test migration files..."
rm -f "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql/V2__*.sql"
rm -f "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/h2/V2__*.sql"
rm -f "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/Foo_previous.scala"
rm -f "$PROJECT_DIR/src/main/scala/app/domain/Foo_migration.scala"
echo "Regenerating with original schema..."
run_sbt "moleculeGen"
echo "Restoring Foo_previous.scala..."
run_sbt "moleculeGen initMigrations Foo"
echo -e "${GREEN}✓${NC} Removed test attribute and cleaned up migration files"
echo ""
echo "Migration status after Step 7:"
echo "> sbt moleculeMigrationStatus"
(cd "$PROJECT_DIR" && sbt moleculeMigrationStatus)
echo ""

echo "Step 8: Delete migrations for Bar"
echo "----------------------------------------------"
run_sbt "moleculeGen deleteMigrations Bar"
check_file_not_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Bar/postgresql/V1__initial_schema.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql/V1__initial_schema.sql"
echo -e "${GREEN}✓${NC} Bar migrations deleted, Foo remains"
echo ""
echo "Migration status after Step 8:"
echo "> sbt moleculeMigrationStatus"
(cd "$PROJECT_DIR" && sbt moleculeMigrationStatus)
echo ""

echo "Step 9: Delete all migrations"
echo "----------------------------------------------"
run_sbt "moleculeGen deleteMigrations"
check_file_not_exists "$PROJECT_DIR/src/main/resources/db/migration/app/domain/Foo/postgresql/V1__initial_schema.sql"
if [ -d "$PROJECT_DIR/src/main/resources/db/migration" ]; then
    REMAINING=$(find "$PROJECT_DIR/src/main/resources/db/migration" -name "V*.sql" | wc -l | tr -d ' ')
    if [ "$REMAINING" -eq "0" ]; then
        echo -e "${GREEN}✓${NC} All migrations deleted"
    else
        echo -e "${RED}✗${NC} Still found $REMAINING migration files"
        exit 1
    fi
else
    echo -e "${GREEN}✓${NC} Migration directory removed"
fi
echo ""

echo "Step 10: Verify status shows no active migrations"
echo "----------------------------------------------"
echo "> sbt moleculeMigrationStatus"
cd "$PROJECT_DIR" && sbt moleculeMigrationStatus | grep "No active migrations" > /dev/null
cd ..
echo -e "${GREEN}✓${NC} Status correctly shows no active migrations"
echo ""

echo "Step 11: Verify clean generation still works"
echo "----------------------------------------------"
run_sbt "moleculeGen"
check_file_exists "$PROJECT_DIR/src/main/resources/db/schema/app/domain/Foo/Foo_postgresql.sql"
check_file_exists "$PROJECT_DIR/src/main/resources/db/schema/app/domain/Bar/Bar_postgresql.sql"
echo -e "${GREEN}✓${NC} Clean generation successful, schema files created"
echo ""

echo "Step 12: Reset project to clean state"
echo "----------------------------------------------"
reset_project
echo -e "${GREEN}✓${NC} Project reset to initial state"
echo ""

echo "=========================================="
echo -e "${GREEN}All tests passed!${NC}"
echo "=========================================="
