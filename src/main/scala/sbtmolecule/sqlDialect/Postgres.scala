package sbtmolecule.db.sqlDialect

import molecule.base.ast.*

object Postgres extends Dialect {

  override def tpe(metaAttribute: MetaAttribute): String = {
    if (metaAttribute.attr == "id")
      "BIGSERIAL PRIMARY KEY"
    else metaAttribute.card match {
      case _: CardOne => metaAttribute.baseTpe match {
        case "ID"             => "BIGINT"
        case "String"         => "TEXT COLLATE ucs_basic"
        case "Int"            => "INTEGER"
        case "Long"           => "BIGINT"
        case "Float"          => "DECIMAL"
        case "Double"         => "DOUBLE PRECISION"
        case "Boolean"        => "BOOLEAN"
        case "BigInt"         => "DECIMAL"
        case "BigDecimal"     => "DECIMAL"
        case "Date"           => "BIGINT"
        case "Duration"       => "VARCHAR"
        case "Instant"        => "VARCHAR"
        case "LocalDate"      => "VARCHAR"
        case "LocalTime"      => "VARCHAR"
        case "LocalDateTime"  => "VARCHAR"
        case "OffsetTime"     => "VARCHAR"
        case "OffsetDateTime" => "VARCHAR"
        case "ZonedDateTime"  => "VARCHAR"
        case "UUID"           => "UUID"
        case "URI"            => "VARCHAR"
        case "Byte"           => "SMALLINT"
        case "Short"          => "SMALLINT"
        case "Char"           => "CHAR(1)"
      }
      case _: CardSet => metaAttribute.baseTpe match {
        case "String"         => "TEXT ARRAY"
        case "Int"            => "INTEGER ARRAY"
        case "Long"           => "BIGINT ARRAY"
        case "Float"          => "DECIMAL ARRAY"
        case "Double"         => "DOUBLE PRECISION ARRAY"
        case "Boolean"        => "BOOLEAN ARRAY"
        case "BigInt"         => "DECIMAL ARRAY"
        case "BigDecimal"     => "DECIMAL ARRAY"
        case "Date"           => "BIGINT ARRAY"
        case "Duration"       => "VARCHAR ARRAY"
        case "Instant"        => "VARCHAR ARRAY"
        case "LocalDate"      => "VARCHAR ARRAY"
        case "LocalTime"      => "VARCHAR ARRAY"
        case "LocalDateTime"  => "VARCHAR ARRAY"
        case "OffsetTime"     => "VARCHAR ARRAY"
        case "OffsetDateTime" => "VARCHAR ARRAY"
        case "ZonedDateTime"  => "VARCHAR ARRAY"
        case "UUID"           => "UUID ARRAY"
        case "URI"            => "VARCHAR ARRAY"
        case "Byte"           => "SMALLINT ARRAY"
        case "Short"          => "SMALLINT ARRAY"
        case "Char"           => "CHAR(1) ARRAY"
      }

      case _: CardSeq => metaAttribute.baseTpe match {
        case "String"         => "TEXT ARRAY"
        case "Int"            => "INTEGER ARRAY"
        case "Long"           => "BIGINT ARRAY"
        case "Float"          => "DECIMAL ARRAY"
        case "Double"         => "DOUBLE PRECISION ARRAY"
        case "Boolean"        => "BOOLEAN ARRAY"
        case "BigInt"         => "DECIMAL ARRAY"
        case "BigDecimal"     => "DECIMAL ARRAY"
        case "Date"           => "BIGINT ARRAY"
        case "Duration"       => "VARCHAR ARRAY"
        case "Instant"        => "VARCHAR ARRAY"
        case "LocalDate"      => "VARCHAR ARRAY"
        case "LocalTime"      => "VARCHAR ARRAY"
        case "LocalDateTime"  => "VARCHAR ARRAY"
        case "OffsetTime"     => "VARCHAR ARRAY"
        case "OffsetDateTime" => "VARCHAR ARRAY"
        case "ZonedDateTime"  => "VARCHAR ARRAY"
        case "UUID"           => "UUID ARRAY"
        case "URI"            => "VARCHAR ARRAY"
        case "Byte"           => "BYTEA" // special for byte arrays
        case "Short"          => "SMALLINT ARRAY"
        case "Char"           => "CHAR(1) ARRAY"
      }

      case _: CardMap => "JSONB"
    }
  }

  // https://www.postgresql.org/docs/current/sql-keywords-appendix.html
  override def reservedKeyWords: List[String] = List(
    "all",
    "analyse",
    "analyze",
    "and",
    "any",
    "array",
    "as",
    "asc",
    "asymmetric",
    "authorization",
    "binary",
    "both",
    "case",
    "cast",
    "check",
    "collate",
    "collation",
    "column",
    "concurrently",
    "constraint",
    "create",
    "cross",
    "current_catalog",
    "current_date",
    "current_role",
    "current_schema",
    "current_time",
    "current_timestamp",
    "current_user",
    "day",
    "default",
    "deferrable",
    "desc",
    "distinct",
    "do",
    "else",
    "end",
    "except",
    "false",
    "fetch",
    "filter",
    "for",
    "foreign",
    "freeze",
    "from",
    "full",
    "grant",
    "group",
    "having",
    "hour",
    "ilike",
    "in",
    "initially",
    "inner",
    "intersect",
    "into",
    "is",
    "isnull",
    "join",
    "lateral",
    "leading",
    "left",
    "like",
    "limit",
    "localtime",
    "localtimestamp",
    "minute",
    "month",
    "natural",
    "not",
    "notnull",
    "null",
    "offset",
    "on",
    "only",
    "or",
    "order",
    "outer",
    "over",
    "overlaps",
    "placing",
    "primary",
    "references",
    "returning",
    "right",
    "second",
    "select",
    "session_user",
    "similar",
    "some",
    "symmetric",
    "system_user",
    "table",
    "tablesample",
    "then",
    "to",
    "trailing",
    "true",
    "union",
    "unique",
    "user",
    "using",
    "variadic",
    "varying",
    "verbose",
    "when",
    "where",
    "window",
    "with",
    "within",
    "without",
    "year",
  )
}
