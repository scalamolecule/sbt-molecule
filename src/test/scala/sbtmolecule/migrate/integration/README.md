# Migration Integration Tests

This directory contains end-to-end integration tests for the migration workflow.

## MigrationWorkflowTest

Simulates the complete real-world migration workflow that a user would experience:

1. **Initial setup** - Create `Foo.scala` with `Person { val name = oneString }`, generate Flyway V1 migration, create H2 database, insert test data
2. **User modifies schema** - Change `name` to `fullName` in the same file (ambiguous change)
3. **Plugin detects ambiguity** - Throws error about missing migration commands
4. **Plugin generates migration helper** - In production, would create `Foo_migration.scala` with both `.remove` and `.becomes()` uncommented (intentionally won't compile - forces user to make explicit choice). In this test, we use `.scalaTemp` extension to avoid breaking the test project's compilation.
5. **User resolves ambiguity** - Edits to `Foo_migration.scala` choosing `.becomes(fullName)` (now compiles)
6. **Plugin generates SQL** - Creates `ALTER TABLE Person RENAME COLUMN name TO fullName;`
7. **Flyway executes migration** - Adds V2 migration file alongside V1, executes it on real H2 database
8. **Verification** - Confirms data persisted with new column name
9. **Cleanup** - Closes database connection and removes temporary test files

## Temporary Test Files

During test execution, files are generated in `workflowTempFiles/` for realistic simulation:

- **step1_initial/** - Initial `Foo.scala` with `name` attribute
- **step2_modified/** - Modified `Foo.scala` with `fullName` attribute
- **step4_migration_generated/** - `Foo.scala` + `Foo_migration.scalaTemp` (uses `.scalaTemp` extension for testing only; in production would be `.scala`)
- **step5_migration_resolved/** - `Foo.scala` + `Foo_migration.scala` (user-resolved version that compiles)
- **step6_sql_generated/** - Generated `migration.sql`
- **db/migration/** - Flyway migrations:
    - `V1__initial_schema.sql` - Initial table creation
    - `V2__rename_person_name_to_fullName.sql` - Column rename
- **testdb.mv.db** - H2 database file (Multi-Version storage engine)
- **testdb.trace.db** - H2 trace/debug log

Each step directory has proper package declarations (e.g., `package sbtmolecule.migrate.integration.workflowTempFiles.step1_initial`) to avoid compilation conflicts.

**Note:** All temporary files are automatically cleaned up after the test completes.

## Key Features

- **Real file operations** - Uses os-lib to create actual files in the source tree
- **Real database** - H2 database with actual data that persists through migration
- **Real Flyway execution** - Both V1 and V2 migrations executed properly
- **Type-safe workflow** - `.becomes()` method ensures compile-time safety
- **Compilation-driven UX** - Generated file won't compile until user makes explicit choice
