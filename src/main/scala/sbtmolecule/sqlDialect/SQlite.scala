package sbtmolecule.sqlDialect

import molecule.db.base.ast.*

object SQlite extends Dialect {

  override def tpe(metaAttribute: MetaAttribute): String = {
    if (metaAttribute.attr == "id")
      "INTEGER PRIMARY KEY AUTOINCREMENT"
    else metaAttribute.card match {
      case _: CardOne => metaAttribute.baseTpe match {
        case "ID"             => "INTEGER"
        case "String"         => "TEXT"
        case "Int"            => "INTEGER"
        case "Long"           => "BIGINT"
        case "Float"          => "DOUBLE PRECISION"
        case "Double"         => "DOUBLE PRECISION"
        case "Boolean"        => "BOOLEAN"
        case "BigInt"         => "TEXT"
        case "BigDecimal"     => "TEXT"
        case "Date"           => "DATE"
        case "Duration"       => "NVARCHAR(100)"
        case "Instant"        => "NVARCHAR(100)"
        case "LocalDate"      => "NVARCHAR(100)"
        case "LocalTime"      => "NVARCHAR(100)"
        case "LocalDateTime"  => "NVARCHAR(100)"
        case "OffsetTime"     => "NVARCHAR(100)"
        case "OffsetDateTime" => "NVARCHAR(100)"
        case "ZonedDateTime"  => "NVARCHAR(100)"
        case "UUID"           => "VARCHAR(16)"
        case "URI"            => "TEXT"
        case "Byte"           => "TINYINT"
        case "Short"          => "SMALLINT"
        case "Char"           => "CHARACTER(1)"
      }

      case _: CardSeq => metaAttribute.baseTpe match {
        case "Byte" => "VARBINARY" // special for byte arrays
        case _      => "JSON"
      }

      case _ => "JSON"
    }
  }

  // https://sqlite.org/lang_keywords.html
  override def reservedKeyWords: List[String] = List(
    "abort",
    "action",
    "add",
    "after",
    "all",
    "alter",
    "always",
    "analyze",
    "and",
    "as",
    "asc",
    "attach",
    "autoincrement",
    "before",
    "begin",
    "between",
    "by",
    "cascade",
    "case",
    "cast",
    "check",
    "collate",
    "column",
    "commit",
    "conflict",
    "constraint",
    "create",
    "cross",
    "current",
    "current_date",
    "current_time",
    "current_timestamp",
    "database",
    "default",
    "deferrable",
    "deferred",
    "delete",
    "desc",
    "detach",
    "distinct",
    "do",
    "drop",
    "each",
    "else",
    "end",
    "escape",
    "except",
    "exclude",
    "exclusive",
    "exists",
    "explain",
    "fail",
    "filter",
    "first",
    "following",
    "for",
    "foreign",
    "from",
    "full",
    "generated",
    "glob",
    "group",
    "groups",
    "having",
    "if",
    "ignore",
    "immediate",
    "in",
    "index",
    "indexed",
    "initially",
    "inner",
    "insert",
    "instead",
    "intersect",
    "into",
    "is",
    "isnull",
    "join",
    "key",
    "last",
    "left",
    "like",
    "limit",
    "match",
    "materialized",
    "natural",
    "no",
    "not",
    "nothing",
    "notnull",
    "null",
    "nulls",
    "of",
    "offset",
    "on",
    "or",
    "order",
    "others",
    "outer",
    "over",
    "partition",
    "plan",
    "pragma",
    "preceding",
    "primary",
    "query",
    "raise",
    "range",
    "recursive",
    "references",
    "regexp",
    "reindex",
    "release",
    "rename",
    "replace",
    "restrict",
    "returning",
    "right",
    "rollback",
    "row",
    "rows",
    "savepoint",
    "select",
    "set",
    "table",
    "temp",
    "temporary",
    "then",
    "ties",
    "to",
    "transaction",
    "trigger",
    "unbounded",
    "union",
    "unique",
    "update",
    "using",
    "vacuum",
    "values",
    "view",
    "virtual",
    "when",
    "where",
    "window",
    "with",
    "without",
  )
}
