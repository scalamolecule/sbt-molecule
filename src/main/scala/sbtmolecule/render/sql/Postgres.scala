package sbtmolecule.render.sql

import molecule.base.ast.{CardOne, MetaAttr}

object Postgres extends Field {

  override def tpe(a: MetaAttr): String = {
    if (a.attr == "id")
      "BIGSERIAL PRIMARY KEY"
    else a.card match {
      case _: CardOne => a.baseTpe match {
        case "String"     => "TEXT COLLATE ucs_basic"
        case "Int"        => "INTEGER"
        case "Long"       => "BIGINT"
        case "Float"      => "DECIMAL"
        case "Double"     => "DOUBLE PRECISION"
        case "Boolean"    => "BOOLEAN"
        case "BigInt"     => "DECIMAL"
        case "BigDecimal" => "DECIMAL"
        case "Date"       => "DATE"
        case "UUID"       => "UUID"
        case "URI"        => "VARCHAR"
        case "Byte"       => "SMALLINT"
        case "Short"      => "SMALLINT"
        case "Char"       => "CHAR(1)"
      }
      case _          => a.baseTpe match {
        case "String"     => "TEXT ARRAY"
        case "Int"        => "INTEGER ARRAY"
        case "Long"       => "BIGINT ARRAY"
        case "Float"      => "DECIMAL ARRAY"
        case "Double"     => "DOUBLE PRECISION ARRAY"
        case "Boolean"    => "BOOLEAN ARRAY"
        case "BigInt"     => "DECIMAL ARRAY"
        case "BigDecimal" => "DECIMAL ARRAY"
        case "Date"       => "DATE ARRAY"
        case "UUID"       => "UUID ARRAY"
        case "URI"        => "VARCHAR ARRAY"
        case "Byte"       => "SMALLINT ARRAY"
        case "Short"      => "SMALLINT ARRAY"
        case "Char"       => "CHAR(1) ARRAY"
      }
    }
  }
}
