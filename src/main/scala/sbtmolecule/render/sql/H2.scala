package sbtmolecule.render.sql

import molecule.base.ast.{CardOne, MetaAttr}

object H2 extends Field {

  override def tpe(a: MetaAttr): String = {
    if (a.attr == "id")
      "BIGINT AUTO_INCREMENT PRIMARY KEY"
    else a.card match {
      case _: CardOne => a.baseTpe match {
        case "String"     => "LONGVARCHAR"
        case "Int"        => "INT"
        case "Long"       => "BIGINT"
        case "Float"      => "REAL"
        case "Double"     => "DOUBLE PRECISION"
        case "Boolean"    => "BOOLEAN"
        case "BigInt"     => "DECIMAL(100, 0)"
        case "BigDecimal" => "DECIMAL(65535, 25)"
        case "Date"       => "DATE"
        case "UUID"       => "UUID"
        case "URI"        => "VARCHAR"
        case "Byte"       => "TINYINT"
        case "Short"      => "SMALLINT"
        case "Char"       => "CHAR"
      }
      case _          => a.baseTpe match {
        case "String"     => "LONGVARCHAR ARRAY"
        case "Int"        => "INT ARRAY"
        case "Long"       => "BIGINT ARRAY"
        case "Float"      => "REAL ARRAY"
        case "Double"     => "DOUBLE PRECISION ARRAY"
        case "Boolean"    => "BOOLEAN ARRAY"
        case "BigInt"     => "DECIMAL(100, 0) ARRAY"
        case "BigDecimal" => "DECIMAL(65535, 25) ARRAY"
        case "Date"       => "DATE ARRAY"
        case "UUID"       => "UUID ARRAY"
        case "URI"        => "VARCHAR ARRAY"
        case "Byte"       => "TINYINT ARRAY"
        case "Short"      => "SMALLINT ARRAY"
        case "Char"       => "CHAR ARRAY"
      }
    }
  }
}
