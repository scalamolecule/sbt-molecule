# Molecule Migration System

## Overview

Molecule now supports optional database migrations using Flyway-compatible versioned SQL files. This system allows you to:
- Enable migrations per-domain when ready for production
- Keep other domains in development mode (schema regeneration without migrations)
- Auto-generate incremental migrations when schemas change
- Manage migrations via simple command-line flags

## Directory Structure

```
src/main/resources/
  db/
    schema/                          # Current schemas (always generated)
      {package}/{domain}/
        {Domain}_postgresql.sql
        {Domain}_h2.sql
        ...
    migration/                       # Flyway migrations (opt-in per domain)
      {package}/{domain}/
        {Domain}_previous.scala      # Baseline for migration detection
        postgresql/
          V1__initial_schema.sql
          V2__add_email_field.sql
          ...
        h2/
          V1__initial_schema.sql
          ...
```

## Workflow

### Development Mode (Default)

```bash
sbt moleculeGen
```

- Generates Scala DSL boilerplate
- Generates current schema SQL files
- **No migrations created**
- Can freely modify domain structures

### Enable Migrations

Initialize migrations for specific domain(s):
```bash
sbt "moleculeGen --init-migrations:Foo"
sbt "moleculeGen --init-migrations:Foo,Bar"
```

Initialize for all domains:
```bash
sbt "moleculeGen --init-migrations"
```

This creates:
- V1__initial_schema.sql (baseline migration)
- {Domain}_previous.scala (for change detection)

### Auto-Migration Mode

Once migrations are initialized, regular `moleculeGen` will auto-detect changes:

```bash
# Make changes to domain structure
sbt moleculeGen  # Auto-generates V2, V3, etc.
```

Molecule compares current vs previous structure and generates:
- SQL migration files (ALTER TABLE, etc.)
- Resolution files if ambiguities detected (`.rename()`, `.remove()`)

### Check Migration Status

```bash
sbt moleculeMigrationStatus
```

Output:
```
Active migrations:
  app.domain.Foo - latest: V2__molecule_1_change.sql
  app.domain.Bar - latest: V1__initial_schema.sql
```

### Disable Migrations

Remove migrations for specific domain(s):
```bash
sbt "moleculeGen --delete-migrations:Foo"
sbt "moleculeGen --delete-migrations:Foo,Bar"
```

Remove all migrations:
```bash
sbt "moleculeGen --delete-migrations"
```

## Migration Markers

### Attribute-Level Ambiguity Resolution

When **attributes** disappear without explicit migration markers, Molecule generates a resolution file with options:

```scala
// Foo_migration.scala
trait Foo_migration extends DomainStructure {
  trait Person {
    val oldName = oneString.remove          // Option 1: Remove
    // val oldName = oneString.rename("newName")  // Option 2: Rename
  }
}
```

Uncomment the correct option and run `moleculeGen` again. The markers are automatically removed after migration generation.

### Entity/Segment-Level Explicit Markers Required

For **entities and segments**, Molecule requires explicit markers directly in your domain structure file (no helper migration file is generated):

**Why this design?**
- Entities and segments affect entire tables or table groups
- Their removal/renaming has significant structural impact
- Requiring explicit markers ensures intentional, well-considered changes

**Entity disappears without marker:**
```
-- ERROR: Schema changes detected but explicit migration commands are missing.

The following entities have been removed without extending `Remove` or `Rename("newName")`:
  Person
```

**Solution:** Add the marker directly to your domain file:
```scala
trait Foo extends DomainStructure {
  trait Person extends Remove {  // or: extends RenameTo_NewPerson
    val name = oneString
    val age  = oneInt
  }
}
```

**Segment disappears without marker:**
```
-- ERROR: Schema changes detected but explicit migration commands are missing.

The following segments have been removed without extending `Remove` or `Rename("newName")`:
  oldModule
```

**Solution:** Add the marker directly to your domain file:
```scala
trait Foo extends DomainStructure {
  object oldModule extends Remove {  // or: extends RenameTo_newModule
    trait Entity {
      val name = oneString
    }
  }
}
```

**Summary of resolution approaches:**
- **Attributes/Relationships**: Helper migration file generated → choose `.remove` or `.rename("newName")`
- **Entities/Segments**: No helper file → add `extends Remove` or `extends RenameTo_NewName` directly

## Supported Migration Operations

### Attribute Migrations

**Add attributes:**
```scala
trait Person {
  val name  = oneString
  val email = oneString  // New attribute
}
```

**Remove attributes:**
```scala
trait Person {
  val name  = oneString
  val email = oneString.remove  // Mark for removal
}
```

**Rename attributes:**
```scala
trait Person {
  val name  = oneString
  val email = oneString.rename("emailAddress")  // Mark for rename
}
```

### Relationship Migrations

Foreign key relationships are fully supported with the same operations:

**Add relationships:**
```scala
trait Order {
  val number   = oneInt
  val customer = manyToOne[Customer]  // New foreign key
}
```

**Remove relationships:**
```scala
trait Order {
  val number   = oneInt
  val customer = manyToOne[Customer].remove  // Mark for removal
}
```

**Rename relationships:**
```scala
trait Order {
  val number   = oneInt
  val customer = manyToOne[Customer].rename("buyer")  // Mark for rename
}
```

Relationship migrations handle foreign key constraints and indexes correctly across all database dialects.

### Attribute Options

Changes to `.index` and `.owner` options are detected and migrated automatically:

**Index management:**
```scala
// Adding .index generates CREATE INDEX
val email = oneString.index

// Removing .index generates DROP INDEX
val email = oneString  // .index removed
```

**Owner option (for relationships):**
```scala
// Adding .owner enables ON DELETE CASCADE
val company = manyToOne[Company].owner

// Removing .owner disables CASCADE
val company = manyToOne[Company]  // .owner removed
```

Note: `.unique`, `.mandatory`, and `.alias` are runtime validations only and don't affect the database schema.

### Entity and Segment Migrations

**Remove entity:**
```scala
trait OldEntity extends Remove {
  val name = oneString
}
```

**Rename entity:**
```scala
trait OldEntity extends RenameToNewEntity {
  val name = oneString
}
```

**Remove segment:**
```scala
object oldSegment extends Remove {
  trait Entity {
    val name = oneString
  }
}
```

**Rename segment:**
```scala
object oldSegment extends RenameToNewSegment {
  trait Entity {
    val name = oneString
  }
}
```

## Type and Cardinality Changes

Molecule **prevents automatic type and cardinality changes** for data safety. These require manual migration:

**Type changes (prevented):**
```scala
// Before:
val age = oneInt

// After:
val age = oneString  // ERROR: Type change detected
```

**Cardinality changes (prevented):**
```scala
// Before:
val tags = manyString

// After:
val tags = oneString  // ERROR: Cardinality change detected
```

**Why prevented?**
- Type changes require data conversion logic (e.g., `Int → String`)
- Cardinality changes require data aggregation (e.g., `many → one` needs deduplication)
- Automating these could silently corrupt data

**Manual migration process:**

When you attempt a type/cardinality change, you'll receive an error with these steps:

1. Create new attribute with desired type: `val ageNew = oneString`
2. Generate migration to add column
3. Write custom SQL or application code to convert data
4. Verify data migration
5. Remove old attribute: `val age = oneInt.remove`
6. Generate migration to drop old column
7. Optionally rename: `val ageNew = oneString.rename("age")`

This gives you full control over data transformation while preventing accidental data loss.

## Testing

A comprehensive test script is provided:

```bash
./test-migration-flags.sh
```

This script tests all migration flag combinations:
1. **Development mode**: `moleculeGen` (no migrations)
2. **Initialize single domain**: `moleculeGen --init-migrations:Foo`
3. **Check migration status**: `moleculeMigrationStatus`
4. **Initialize all domains**: `moleculeGen --init-migrations`
5. **Auto-migration**: Schema change triggers automatic migration generation
6. **Delete single domain**: `moleculeGen --delete-migrations:Bar`
7. **Delete all domains**: `moleculeGen --delete-migrations`
8. **Verify status after deletion**: `moleculeMigrationStatus` shows no active migrations
9. **Clean regeneration**: `moleculeGen` works correctly after all migrations deleted

The script covers all flag variations and ensures proper state transitions between migration modes.

## Implementation Details

### Key Files

- `MoleculePlugin.scala`: Main plugin with flag handling
- `GenerateSourceFiles_db.scala`: Schema and migration generation
- `MigrationDetector.scala`: Change detection and ambiguity resolution
- `MigrationSqlGenerator.scala`: SQL generation for different dialects
- `AttributeRemover.scala`: Automatic cleanup of migration markers

### Flag Parsing

Arguments are parsed in `handleMoleculeGen`:
- `--init-migrations[:Domain1,Domain2]`
- `--delete-migrations[:Domain1,Domain2]`

Optional domain list filters which domains are affected.

### Auto-Detection

On each `moleculeGen` run:
1. Check if `db/migration/{package}/{domain}/` exists
2. If yes: Load previous structure, detect changes, generate migration
3. If no: Skip migration generation (development mode)

### Multiple Dialects

All operations affect all supported database dialects:
- H2
- PostgreSQL
- MySQL
- MariaDB
- SQLite

## Best Practices

1. **Start in development mode**: Don't enable migrations until ready for production
2. **Per-domain control**: Production domains can have migrations while dev domains don't
3. **Use migration markers carefully**: Always provide `.remove` or `.rename()` for ambiguous changes
4. **Review generated SQL**: Check migration files before applying to production
5. **Version control**: Commit migration files alongside domain structure changes
6. **Test on staging first**: Apply migrations to a staging environment before production
7. **Handle type changes manually**: Use the 7-step process for type/cardinality changes to ensure data integrity
8. **Use `.index` strategically**: Add indexes to frequently queried attributes for better performance
9. **Be careful with `.owner`**: Understand that `.owner` enables CASCADE DELETE before using it

## Example Workflow

```bash
# Initial development
sbt moleculeGen  # Fast iteration, no migrations

# Ready for production
sbt "moleculeGen --init-migrations:User"

# Make a change
# Add field: val email = oneString
sbt moleculeGen  # Auto-generates V2

# Check what was generated
cat src/main/resources/db/migration/app/domain/User/postgresql/V2__molecule_1_change.sql

# Apply to production database
# (Use Flyway or similar tool)

# Later: major refactoring of experimental domain
sbt "moleculeGen --delete-migrations:Experimental"
# Make breaking changes freely
sbt moleculeGen
# When stable again:
sbt "moleculeGen --init-migrations:Experimental"
```

## Philosophy

Molecule's migration system follows the "less is more" principle:
- **Zero configuration**: No build.sbt settings required
- **Opt-in per domain**: Migrations only where needed
- **Auto-detection**: No manual flag passing once enabled
- **Clear separation**: Development vs production modes

This keeps the common case (development) fast and simple, while providing production-ready migration support when needed.
