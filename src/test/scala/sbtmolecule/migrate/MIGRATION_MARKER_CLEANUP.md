# Automatic Migration Marker Cleanup After Migration

When you mark attributes, entities, or segments with migration markers and successfully run `sbt moleculeGen` to generate the migration files, Molecule **automatically** cleans up these markers from your domain structure file without prompting.

**Domain structure files are Molecule-controlled declarative metadata**, so markers are removed automatically. User is only prompted when renaming attribute usages in application code.

## Important: Attribute vs Entity/Segment Resolution

**Two different workflows for different levels of structural changes:**

### Attributes/Relationships (Lower Impact)
- Disappeared attributes/relationships **generate a helper migration file** (`Foo_migration.scala`)
- You choose `.remove` or `.rename("newName")` in the helper file
- Helper file is automatically cleaned up after migration generation

### Entities/Segments (Higher Impact)
- Disappeared entities/segments **require explicit markers** in your domain file
- You must add `extends Remove` or `extends RenameTo_NewName` directly
- No helper migration file is generated
- **Why?** Entities/segments affect entire tables or table groups - requiring explicit markers ensures intentional, well-considered changes

This document focuses on the automatic cleanup process that happens **after** you've resolved ambiguities using either approach.

## How It Works

### Example 1: Removing Attributes

1. **Mark attribute for removal:**
   ```scala
   trait Person {
     val name     = oneString
     val email    = oneString.remove  // Mark for removal
     val age      = oneInt
     val customer = manyToOne[Customer].remove  // Relationships too!
   }
   ```

2. **Run migration:**
   ```bash
   sbt moleculeGen
   ```

3. **Automatic cleanup** (no prompt):
   ```
   ======================================================================
   Migration completed successfully!

   Cleaned up migration markers from domain structure:
     Attributes removed:
       - Person.email (line 8)
       - Person.customer (line 10)

   ✓ Domain structure updated: Person.scala
   ```

4. **Result** - the attribute definitions are automatically removed:
   ```scala
   trait Person {
     val name = oneString
     val age  = oneInt
   }
   ```

### Example 2: Renaming Attributes

1. **Mark attribute for rename:**
   ```scala
   trait Order {
     val number   = oneInt
     val customer = manyToOne[Customer].rename("buyer")  // Mark for rename
   }

   trait Person {
     val name     = oneString
     val email    = oneString.rename("emailAddress")  // Mark for rename
     val age      = oneInt
   }
   ```

2. **Run migration:**
   ```bash
   sbt moleculeGen
   ```

3. **Automatic cleanup** (no prompt):
   ```
   ======================================================================
   Migration completed successfully!

   Rename markers removed (you must manually rename the definitions):
     - val Order.customer -> buyer (line 3)
     - val Person.email -> emailAddress (line 9)

   ✓ Domain structure updated: Person.scala
   ```

4. **Result** - the `.rename(...)` marker is automatically removed (attribute stays):
   ```scala
   trait Person {
     val name  = oneString
     val email = oneString  // Marker removed, attribute kept
     val age   = oneInt
   }
   ```

5. **Option to automatically rename usages across codebase**:
   ```
   The old attribute names will cause compile errors in your code.
   Would you like to automatically rename attribute usages across your codebase?
   (This will update .scala files in src/ directories)

   Rename attribute usages? [y/n]:
   ```

6. **If you answer 'y'**, all usages are renamed automatically:
   ```
   Searching 47 Scala files...
     Updated: /src/main/scala/app/queries/UserQueries.scala
     Updated: /src/test/scala/app/PersonTests.scala

   ✓ Renamed 23 attribute usage(s) across the codebase
   ```

7. **Then manually rename the attribute definition** to complete the migration:
   ```scala
   trait Person {
     val name         = oneString
     val emailAddress = oneString  // Manually renamed!
     val age          = oneInt
   }
   ```

### Example 3: Removing Entities

1. **Mark entity for removal:**
   ```scala
   trait Person extends Remove {
     val name = oneString
     val age  = oneInt
   }

   trait Company {
     val name = oneString
   }
   ```

2. **Run migration:**
   ```bash
   sbt moleculeGen
   ```

3. **Automatic cleanup:**
   ```
   ======================================================================
   Migration completed successfully!

   Cleaned up migration markers from domain structure:
     Entities removed:
       - Person (line 5)

   ✓ Domain structure updated: MyDomain.scala
   ```

4. **Result** - the entire entity trait is automatically removed:
   ```scala
   trait Company {
     val name = oneString
   }
   ```

### Example 4: Removing Segments

1. **Mark segment for removal:**
   ```scala
   object sales extends Remove {
     trait Customer {
       val name = oneString
     }
   }

   object inventory {
     trait Product {
       val title = oneString
     }
   }
   ```

2. **Run migration:**
   ```bash
   sbt moleculeGen
   ```

3. **Automatic cleanup:**
   ```
   ======================================================================
   Migration completed successfully!

   Cleaned up migration markers from domain structure:
     Segments removed:
       - sales (line 1)

   ✓ Domain structure updated: MyDomain.scala
   ```

4. **Result** - the entire segment object is automatically removed:
   ```scala
   object inventory {
     trait Product {
       val title = oneString
     }
   }
   ```

### Example 5: Multiple Marker Types

The tool handles all types of markers in a single migration:

```
======================================================================
Migration completed successfully!

Cleaned up migration markers from domain structure:
  Segments removed:
    - oldSegment (line 1)
  Entities removed:
    - Person (line 15)
  Attributes removed:
    - Order.phone (line 25)

  Rename markers removed (you must manually rename the definitions):
    - object sales -> Sales (line 10)
    - trait Customer -> Client (line 20)
    - val Order.email -> emailAddress (line 23)

✓ Domain structure updated: MyDomain.scala
```

## Features

### Preserves Formatting and Comments

The removal is position-based and preserves all other code:
- Comments before/after attributes
- Blank lines and indentation
- All other attributes unchanged

### Handles Multi-line Attributes

Works correctly with attributes spanning multiple lines:
```scala
val emailWithValidation = oneString
  .email("Please provide a valid email")
  .mandatory
  .remove
```

All lines of the attribute definition are removed cleanly.

### Multiple Markers

Multiple migration markers are handled together:
```scala
trait Person {
  val name         = oneString
  val email        = oneString.rename("emailAddress")  // rename this
  val phone        = oneString.remove  // remove this
  val legacyField  = oneString.remove  // remove this too
  val address      = oneString
}
```

After choosing 'y':
- The `.rename(...)` marker is removed from `email` (you then rename it manually)
- Both `phone` and `legacyField` are completely removed
- `name` and `address` remain unchanged

### Automatic Codebase Renaming

When you rename attributes, the tool can automatically update **all usages** across your codebase using **Scala Meta AST parsing**:

**How it works:**
- Parses all `.scala` files in `src/` directories using Scala Meta
- Traverses the AST to find `Term.Name` nodes (actual Scala identifiers)
- Uses exact AST positions for replacements
- Skips `target/`, `.git/`, and `moleculeGen/` directories
- Shows which files were updated and how many replacements per file
- Gracefully skips files with parse errors

**Example:**
```scala
// Before:
Person.name.email.age.query.get  // ← "email" will be renamed

val message = "Please update your email"  // ← NOT renamed (it's a string)

// After automatic renaming:
Person.name.emailAddress.age.query.get  // ✓ Renamed

val message = "Please update your email"  // ✓ Unchanged (correct!)
```

**This is type-safe:**
- ✅ Only renames actual Scala identifiers (`Term.Name` nodes in the AST)
- ✅ Strings, comments, and documentation are never modified
- ✅ Uses the same Scala Meta parser that validates your domain structure
- ✅ Parse errors are handled gracefully (files are skipped, not corrupted)
- ✅ All changes are reviewable in git before committing

## Benefits

1. **Automatic Cleanup**: Domain structure markers are removed automatically (no manual cleanup needed)
2. **User Control Where It Matters**: You still decide whether to rename usages in application code
3. **Type-Safe**: AST-based renaming only touches actual Scala identifiers
4. **Prevents Data Corruption**: Detects and rejects dangerous name swaps
5. **Precise Positions**: Uses exact AST positions from Scala Meta parsing for attributes
6. **Git Friendly**: All changes are reviewable before committing
7. **Comprehensive**: Handles attributes, entities, and segments
8. **Reversible**: All operations are recoverable via git history

## Protection Against Name Swaps

The system **prevents dangerous name swaps** that could cause data corruption:

### What is a Name Swap?

```scala
// ❌ DANGEROUS - NOT ALLOWED
trait Person {
  val email = oneString.rename("phone")  // email -> phone
  val phone = oneString.rename("email")  // phone -> email
}
```

### Why is it Dangerous?

1. During migration, SQL renames columns: `email → phone` and `phone → email`
2. But the actual data values stay in their original columns during the rename
3. If your code runs between migration steps, it could write email data to the phone column
4. Even if no code runs, this creates confusion about which data is in which column

### Error Message

```
-- ERROR: Dangerous attribute name swap detected.

The following attributes are being renamed to each other's names:
  - Person.email <-> Person.phone

This is not allowed because it can cause data corruption. If you need to swap
attribute names, you must do it over two separate migrations:

Migration 1: Rename to temporary names
  val email = oneString.rename("email_temp")
  val phone = oneString.rename("phone_temp")

Migration 2: Rename to final names
  val email_temp = oneString.rename("phone")
  val phone_temp = oneString.rename("email")
```

### The Safe Way to Swap Names

Use a two-step migration with temporary names:

**Step 1**: Rename both to temporary names
```scala
trait Person {
  val email = oneString.rename("email_temp")
  val phone = oneString.rename("phone_temp")
}
```

Run `sbt moleculeGen`, clean up markers, manually rename to:
```scala
trait Person {
  val email_temp = oneString
  val phone_temp = oneString
}
```

**Step 2**: Rename from temporary to swapped names
```scala
trait Person {
  val email_temp = oneString.rename("phone")
  val phone_temp = oneString.rename("email")
}
```

Run `sbt moleculeGen`, clean up markers, manually rename to:
```scala
trait Person {
  val phone = oneString  // Originally was email!
  val email = oneString  // Originally was phone!
}
```

## When to Say 'No' (Application Code Renaming Only)

Domain structure cleanup happens automatically, but you might want to decline the automatic codebase renaming (application code) if:
- You want to manually review each usage
- You have complex code patterns that need manual attention
- You want to update related documentation at the same time

Note: Domain structure markers are always removed automatically since they're Molecule-controlled metadata.

## Technical Details

### Migration Marker Cleanup (Domain Structure Files)

The marker cleanup uses **Scala Meta positions captured during safe parsing**:

1. **Parsing phase** (`ParseDomainStructure.scala`):
    - Scala Meta parses domain structure: `trait Foo extends DomainStructure { ... }`
    - Detects `.remove` and `.rename(...)` markers via AST pattern matching
    - Captures exact source positions: `Some((startPos, endPos))`
    - Stores in `MetaAttribute.sourcePosition`

2. **Cleanup phase** (`AttributeRemover.scala`):
    - Retrieves stored positions from `MetaDomain`
    - Uses exact positions to remove/modify attribute definitions
    - Processes in reverse order to maintain position validity
    - Cleans up excessive blank lines

**Why this is trustworthy:**
- Positions come from validated Scala Meta parsing
- No text search or pattern matching
- Can't accidentally match unrelated code
- 100% accurate identification

### Attribute Usage Renaming (All Scala Files)

The codebase renaming uses **Scala Meta AST traversal**:

1. **Discovery phase**:
    - Recursively finds all `.scala` files in `src/`
    - Excludes `target/`, `.git/`, `moleculeGen/`

2. **Parsing phase** (per file):
    - Parses file with Scala Meta: `dialect.parse[Source]`
    - Traverses AST looking for `Term.Name` nodes
    - Matches against rename map: `case Term.Name(oldName) if renameMap.contains(oldName)`
    - Collects positions: `(name.pos.start, name.pos.end, oldName, newName)`

3. **Replacement phase**:
    - Applies replacements in reverse order (maintains position validity)
    - Writes updated file
    - Gracefully skips files with parse errors

**Type safety:**
- Only touches actual Scala identifiers (`Term.Name` AST nodes)
- Strings, comments, and documentation are never modified
- Uses Scala Meta AST parsing (same technology that validates your domain structure)
- Parse errors are handled gracefully (files are skipped, not corrupted)
- All changes are reversible via git

**Example of what gets renamed vs. what doesn't:**
```scala
// Renamed (Term.Name nodes):
Person.email.query              // ← identifier
val x = email                   // ← identifier
case class Foo(email: String)   // ← parameter name

// NOT renamed (not Term.Name):
val msg = "Send email to..."    // ← string literal
// TODO: update email field      // ← comment
```

No external dependencies beyond Scala Meta (which is already used for domain structure parsing).
