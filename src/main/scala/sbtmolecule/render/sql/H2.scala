package sbtmolecule.render.sql

import molecule.base.ast.{CardOne, MetaAttr}

object H2 extends Dialect {

  override def tpe(a: MetaAttr): String = {
    if (a.attr == "id")
      "BIGINT AUTO_INCREMENT PRIMARY KEY"
    else a.card match {
      case _: CardOne => a.baseTpe match {
        case "String"         => "LONGVARCHAR"
        case "Int"            => "INT"
        case "Long"           => "BIGINT"
        case "Float"          => "REAL"
        case "Double"         => "DOUBLE PRECISION"
        case "Boolean"        => "BOOLEAN"
        case "BigInt"         => "DECIMAL(100, 0)"
        case "BigDecimal"     => "DECIMAL(65535, 25)"
        case "Date"           => "DATE"
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
        case "Byte"           => "TINYINT"
        case "Short"          => "SMALLINT"
        case "Char"           => "CHAR"
      }
      case _          => a.baseTpe match {
        case "String"         => "LONGVARCHAR ARRAY"
        case "Int"            => "INT ARRAY"
        case "Long"           => "BIGINT ARRAY"
        case "Float"          => "REAL ARRAY"
        case "Double"         => "DOUBLE PRECISION ARRAY"
        case "Boolean"        => "BOOLEAN ARRAY"
        case "BigInt"         => "DECIMAL(100, 0) ARRAY"
        case "BigDecimal"     => "DECIMAL(65535, 25) ARRAY"
        case "Date"           => "DATE ARRAY"
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
        case "Byte"           => "TINYINT ARRAY"
        case "Short"          => "SMALLINT ARRAY"
        case "Char"           => "CHAR ARRAY"
      }
    }
  }

  // http://www.h2database.com/html/advanced.html#keywords
  override def reservedKeyWords: List[String] = List(
    "_rowid_",
    "all",
    "and",
    "any",
    "array",
    "as",
    "asymmetric",
    "authorization",
    "between",
    "both",
    "case",
    "cast",
    "check",
    "constraint",
    "cross",
    "current_catalog",
    "current_date",
    "current_path",
    "current_role",
    "current_schema",
    "current_time",
    "current_timestamp",
    "current_user",
    "day",
    "default",
    "distinct",
    "else",
    "end",
    "except",
    "exists",
    "false",
    "fetch",
    "for",
    "foreign",
    "from",
    "full",
    "group",
    "groups",
    "having",
    "hour",
    "if",
    "ilike",
    "in",
    "inner",
    "intersect",
    "interval",
    "is",
    "join",
    "key",
    "leading",
    "left",
    "like",
    "limit",
    "localtime",
    "localtimestamp",
    "minus",
    "minute",
    "month",
    "natural",
    "not",
    "null",
    "offset",
    "on",
    "or",
    "order",
    "over",
    "partition",
    "primary",
    "qualify",
    "range",
    "regexp",
    "right",
    "row",
    "rownum",
    "rows",
    "second",
    "select",
    "session_user",
    "set",
    "some",
    "symmetric",
    "system_user",
    "table",
    "to",
    "top",
    "trailing",
    "true",
    "uescape",
    "union",
    "unique",
    "unknown",
    "user",
    "using",
    "value",
    "values",
    "when",
    "where",
    "window",
    "with",
    "year",
  )
}
