# Test Project 8 - Migration Integration Tests

This test project provides real-world integration testing for Molecule's database migration workflow using Flyway.

## Purpose

While `MigrationWorkflowTest` provides unit-level testing of the migration detection and generation logic, this project tests the complete end-to-end workflow as a user would experience it:

1. Running `sbt moleculeGen` with schema changes
2. Receiving appropriate error messages
3. Auto-generation of migration files
4. Resolving ambiguities
5. Successful migration completion

## Test Structure

### Automated Testing: `test-all-workflows.sh`

Runs all 5 migration workflows unattended for CI/regression testing:

```bash
# From this directory
./test-all-workflows.sh

# Or from sbt-molecule root
cd test-project8-migration && ./test-all-workflows.sh
```

Validates all workflows complete successfully and exits with appropriate exit codes for CI integration.

### Interactive Demonstrations

Five interactive scripts that walk through each migration workflow step-by-step:

- **`interactive-add.sh`** - Adding a new attribute (unambiguous)
- **`interactive-remove.sh`** - Removing an attribute with migration marker (unambiguous)
- **`interactive-remove-ambiguous.sh`** - Removing an attribute without marker (ambiguous, requires resolution)
- **`interactive-rename.sh`** - Renaming an attribute with migration marker (unambiguous)
- **`interactive-rename-ambiguous.sh`** - Renaming an attribute without marker (ambiguous, requires resolution)

Each interactive script:
- Pauses at each step and waits for you to press ENTER
- Shows file contents after each operation
- Lets you inspect the project directory between steps
- Explains what should happen before running each command
- Perfect for learning or debugging the migration workflow

```bash
# Run any interactive workflow
./interactive-add.sh
./interactive-remove.sh
./interactive-remove-ambiguous.sh
./interactive-rename.sh
./interactive-rename-ambiguous.sh
```

### Domain Files

- **`Foo.scala`** - Main domain structure (modified during tests)
- **`Bar.scala`** - Secondary domain structure
- **`Foo_migration.scala`** - Auto-generated migration file (created/deleted during tests)

### Generated Files (during test execution)

- **`src/main/resources/db/migration/Foo/Foo_previous.scala`** - Saved previous domain structure
- **`target/.../db/migration/Foo/h2/V1__initial_schema.sql`** - Flyway V1 migration
- Various generated DSL and schema files

## Test Assertions

The scripts validate:

- ✓ Files exist/don't exist at appropriate times
- ✓ File contents match expected patterns
- ✓ Commands succeed/fail as expected
- ✓ Error messages contain correct guidance
- ✓ Migration files are auto-generated with correct structure
- ✓ Previous structure is saved and loaded correctly

## Comparison with MigrationWorkflowTest

| Aspect | MigrationWorkflowTest | test-project8-migration |
|--------|----------------------|------------------------|
| **Scope** | Unit/Integration | Full End-to-End |
| **Approach** | Direct API calls | Real `sbt moleculeGen` commands |
| **Speed** | Fast | Slower (full SBT runs) |
| **Realism** | Simulated | Actual user workflow |
| **Best For** | Testing logic, edge cases | Testing user experience |

## Benefits

1. **Real-world validation** - Tests the actual commands users will run
2. **User experience** - Validates error messages and generated files from user perspective
3. **Integration confidence** - Catches issues that unit tests might miss
4. **Documentation** - Serves as executable documentation of the migration workflow
5. **Regression protection** - Easy to add new test scenarios as edge cases are discovered

## Continuous Integration

The `test-all-workflows.sh` script can be integrated into CI/CD pipelines to ensure migration workflow remains stable across releases.

## Notes

- Tests run in isolation and clean up after themselves
- Each test step is independent and well-documented
- Failed assertions show detailed output for debugging
- The script exits with appropriate exit codes for CI integration
