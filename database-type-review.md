# Database Column Type Review

## Summary

Overall, your type mappings are quite reasonable. Here are the key issues and recommendations across all databases:

## Critical Issues

### 1. **Time/Date Types** - Most databases store them as VARCHAR/TEXT
**Problem**: All temporal types (Duration, Instant, LocalDate, LocalTime, LocalDateTime, OffsetTime, OffsetDateTime, ZonedDateTime) are stored as VARCHAR/TEXT strings across most databases.

**Impact**:
- Cannot use database-native time functions
- No automatic timezone handling
- Cannot index efficiently
- Sorting requires string conversion
- Range queries are inefficient

**Recommendations by type**:
- `Duration` → Keep as VARCHAR (no native equivalent)
- `Instant` → Use TIMESTAMP/DATETIME with timezone
- `LocalDate` → Use DATE
- `LocalTime` → Use TIME
- `LocalDateTime` → Use TIMESTAMP/DATETIME without timezone
- `OffsetTime` → Use TIME WITH TIME ZONE (PostgreSQL) or VARCHAR (others)
- `OffsetDateTime` → Use TIMESTAMP WITH TIME ZONE
- `ZonedDateTime` → VARCHAR (most portable, no native support)

### 2. **PostgreSQL Float** - Uses DECIMAL instead of REAL
**Current**: `Float → DECIMAL`
**Problem**: DECIMAL is for exact decimal arithmetic, not IEEE 754 floating point
**Fix**: `Float → REAL` (4 bytes, IEEE 754 single precision)

### 3. **PostgreSQL BigInt/BigDecimal** - Too generic
**Current**: `BigInt → DECIMAL`, `BigDecimal → DECIMAL`
**Problem**: No precision/scale specified
**Fix**:
- `BigInt → NUMERIC(1000)` or keep `DECIMAL(100, 0)` like H2
- `BigDecimal → NUMERIC(precision, scale)` with explicit values

### 4. **SQLite UUID** - VARCHAR(16) is too small
**Current**: `UUID → VARCHAR(16)`
**Problem**: UUID string representation needs 36 characters (32 hex + 4 hyphens)
**Fix**: `UUID → VARCHAR(36)` or `TEXT`

### 5. **H2 Char** - Missing length
**Current**: `Char → CHAR`
**Problem**: Should specify CHAR(1) for single character
**Fix**: `Char → CHAR(1)`

### 6. **MSSQLServer Double** - Uses REAL instead of FLOAT
**Current**: `Double → REAL`
**Problem**: REAL in SQL Server is only 4 bytes (Float), not 8 bytes (Double)
**Fix**: `Double → FLOAT(53)` (8 bytes, IEEE 754 double precision)

### 7. **MSSQLServer ID** - Wrong PRIMARY KEY syntax
**Current**: `"BIGINT AUTO_INCREMENT PRIMARY KEY"`
**Problem**: SQL Server uses IDENTITY, not AUTO_INCREMENT
**Fix**: `"BIGINT IDENTITY(1,1) PRIMARY KEY"`

### 8. **MSSQLServer TINYTEXT** - Doesn't exist
**Current**: Duration/Instant/etc → `TINYTEXT`
**Problem**: SQL Server doesn't have TINYTEXT
**Fix**: Use `VARCHAR(255)` or `NVARCHAR(255)`

### 9. **MSSQLServer TEXT** - Deprecated
**Current**: `String → TEXT`
**Problem**: TEXT is deprecated in SQL Server
**Fix**: `VARCHAR(MAX)` or `NVARCHAR(MAX)`

### 10. **MSSQLServer TINYINT(1)** - Invalid syntax
**Current**: `Boolean → TINYINT(1)`
**Problem**: SQL Server TINYINT doesn't accept length parameter
**Fix**: `BIT` (native boolean type in SQL Server)

### 11. **Oracle Missing Types** - Many temporal types missing
**Problem**: Duration, Instant, LocalTime, LocalDateTime, OffsetTime, OffsetDateTime, ZonedDateTime not defined
**Fix**: Add mappings (probably VARCHAR for most)

## Moderate Issues

### 12. **H2 String** - LONGVARCHAR vs VARCHAR
**Current**: `String → LONGVARCHAR`
**Consideration**: LONGVARCHAR is fine but consider `VARCHAR` for smaller defaults
**Recommendation**: Keep as LONGVARCHAR (max 2GB), or use `TEXT` for clarity

### 13. **H2 BigDecimal** - Very large precision
**Current**: `BigDecimal → DECIMAL(65535, 38)`
**Problem**: 65535 precision is excessive (max is actually 100000 in H2)
**Recommendation**: Use more reasonable default like `DECIMAL(38, 19)` (half scale)

### 14. **MySQL/MariaDB Boolean** - TINYINT(1) convention
**Current**: `Boolean → TINYINT(1)`
**Status**: This is correct for MySQL/MariaDB (no native BOOLEAN)
**Keep as is**: ✓

### 15. **MySQL Char** - Missing length
**Current**: `Char → CHAR`
**Fix**: `Char → CHAR(1)`

### 16. **MariaDB Char** - Missing length
**Current**: `Char → CHAR`
**Fix**: `Char → CHAR(1)`

## Database-Specific Recommendations

### H2
```scala
case "Char" => "CHAR(1)"  // Add length
case "BigDecimal" => "DECIMAL(38, 19)"  // More reasonable precision
// Consider native date/time types instead of VARCHAR
```

### PostgreSQL
```scala
case "Float" => "REAL"  // Not DECIMAL
case "BigInt" => "NUMERIC(100, 0)"  // Add precision
case "BigDecimal" => "NUMERIC(38, 19)"  // Add precision/scale
// Consider native date/time types
case "Instant" => "TIMESTAMP WITH TIME ZONE"
case "LocalDate" => "DATE"
case "LocalTime" => "TIME"
case "LocalDateTime" => "TIMESTAMP"
case "OffsetDateTime" => "TIMESTAMP WITH TIME ZONE"
```

### SQLite
```scala
case "UUID" => "TEXT"  // Not VARCHAR(16) - needs 36 chars
// SQLite has limited type system, current mappings mostly OK
// Note: SQLite has DATE/DATETIME/TIME types but they're stored as TEXT/INTEGER internally
```

### MySQL
```scala
case "Char" => "CHAR(1)"  // Add length
// Consider native temporal types instead of TINYTEXT
case "LocalDate" => "DATE"
case "LocalTime" => "TIME"
case "LocalDateTime" => "DATETIME"
case "Instant" => "DATETIME"  // or TIMESTAMP
```

### MariaDB
```scala
case "Char" => "CHAR(1)"  // Add length
// Same temporal recommendations as MySQL
```

### MSSQLServer
```scala
// Primary key syntax
if (metaAttribute.attribute == "id")
  "BIGINT IDENTITY(1,1) PRIMARY KEY"  // Not AUTO_INCREMENT

// Core types
case "String" => "NVARCHAR(MAX)"  // Not TEXT (deprecated)
case "Double" => "FLOAT(53)"  // Not REAL (too small)
case "Boolean" => "BIT"  // Not TINYINT(1)
case "Char" => "CHAR(1)"

// Temporal types (instead of TINYTEXT which doesn't exist)
case "Duration" => "NVARCHAR(100)"
case "Instant" => "DATETIME2"  // or DATETIMEOFFSET
case "LocalDate" => "DATE"
case "LocalTime" => "TIME"
case "LocalDateTime" => "DATETIME2"
case "OffsetTime" => "NVARCHAR(50)"
case "OffsetDateTime" => "DATETIMEOFFSET"
case "ZonedDateTime" => "NVARCHAR(100)"
case "UUID" => "UNIQUEIDENTIFIER"  // Native UUID type!

// Collections/Maps
case SeqValue => metaAttribute.baseTpe match {
  case "Byte" => "VARBINARY(MAX)"
  case _ => "NVARCHAR(MAX)"  // SQL Server supports JSON natively since 2016
}
case _ => "NVARCHAR(MAX)"  // Or use FOR JSON if treating as JSON
```

### Oracle
```scala
case "String" => "VARCHAR2(4000)"  // LONG VARCHAR is deprecated, has restrictions
case "BigInt" => "NUMBER(38, 0)"  // More specific
case "BigDecimal" => "NUMBER(38, 19)"  // Add scale

// Add missing temporal types
case "Duration" => "VARCHAR2(100)"
case "Instant" => "TIMESTAMP WITH TIME ZONE"
case "LocalDate" => "DATE"
case "LocalTime" => "TIMESTAMP"  // Oracle doesn't have TIME type
case "LocalDateTime" => "TIMESTAMP"
case "OffsetTime" => "VARCHAR2(50)"
case "OffsetDateTime" => "TIMESTAMP WITH TIME ZONE"
case "ZonedDateTime" => "VARCHAR2(100)"

// Collections: Oracle has native VARRAY but JSON is more portable
case _ => "CLOB"  // Or use JSON type (21c+)
```

## Priority Order

1. **Critical - Fix immediately**:
   - MSSQLServer (multiple syntax errors will break)
   - PostgreSQL Float (wrong type category)
   - SQLite UUID (too small)
   - Oracle missing types (incomplete)

2. **High - Fix soon**:
   - All temporal types across databases (consistency & functionality)
   - Char length specifications

3. **Medium - Consider for next version**:
   - BigDecimal precision specifications
   - String type choices (VARCHAR vs TEXT variants)

## Testing Recommendation

After making changes, test schema generation for each database to ensure:
1. Schemas compile without errors
2. Round-trip data correctly (insert/retrieve)
3. Native date/time functions work where applicable
4. Performance is acceptable for large datasets

## Notes

- Some choices like VARCHAR vs TEXT are subjective and depend on expected data sizes
- Native temporal types offer better functionality but complicate serialization
- Current VARCHAR approach for temporal types is conservative and portable
- Consider making temporal type mappings configurable via the new custom column types feature
