package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting
import sbtmolecule.db.dsl.expr.*


case class Entity_Exprs(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0
) extends Formatting(metaDomain, metaEntity) {

  private val hasString = attributes.exists {
    case a if a.baseTpe == "String" => true
    case _                          => false
  }
  private val hasEnum   = attributes.exists {
    case a if a.enumTpe.isDefined => true
    case _                        => false
  }
  val intTypes = Seq("Int", "Long", "BigInt", "Byte", "Short")
  private val hasInteger = attributes.exists {
    case a if intTypes.contains(a.baseTpe) => true
    case _                                 => false
  }
  val decimalTypes = Seq("Double", "Float", "BigDecimal")
  private val hasDecimal = attributes.exists {
    case a if decimalTypes.contains(a.baseTpe) => true
    case _                                     => false
  }
  private val hasBoolean = attributes.exists {
    case a if a.baseTpe == "Boolean" => true
    case _                           => false
  }

  val hasOne = attributes.exists {
    case a if a.cardinality == CardOne => true
    case _                             => false
  }
  val hasSet = attributes.exists {
    case a if a.cardinality == CardSet => true
    case _                             => false
  }
  val hasSeq = attributes.exists {
    case a if a.cardinality == CardSeq && a.baseTpe != "Byte" => true
    case _                             => false
  }
  val hasMap = attributes.exists {
    case a if a.cardinality == CardMap => true
    case _                             => false
  }
  val hasByteArray = attributes.exists {
    case a if a.cardinality == CardSeq && a.baseTpe == "Byte" => true
    case _                             => false
  }

  val exprOne = if (hasOne) List(0, 1, 2).flatMap {
    case 0     =>
      List(
        ExprOneTac(metaDomain, metaEntity, 0, hasString, hasEnum, hasInteger).get
      )
    case arity =>
      List(
        ExprOneMan(metaDomain, metaEntity, arity, hasString, hasEnum, hasInteger, hasDecimal, hasBoolean).get,
        ExprOneOpt(metaDomain, metaEntity, arity, hasEnum).get,
        ExprOneTac(metaDomain, metaEntity, arity, hasString, hasEnum, hasInteger).get,
      )
  } else Nil

  val exprSet = if (hasSet) List(0, 1, 2).flatMap {
    case 0     =>
      List(
        ExprSetTac(metaDomain, metaEntity, 0, hasEnum).get
      )
    case arity =>
      List(
        ExprSetMan(metaDomain, metaEntity, arity, hasEnum).get,
        ExprSetOpt(metaDomain, metaEntity, arity, hasEnum).get,
        ExprSetTac(metaDomain, metaEntity, arity, hasEnum).get,
      )
  } else Nil

  val exprSeq = if (hasSeq) List(0, 1, 2).flatMap {
    case 0     =>
      List(
        ExprSeqTac(metaDomain, metaEntity, 0, hasEnum).get
      )
    case arity =>
      List(
        ExprSeqMan(metaDomain, metaEntity, arity, hasEnum).get,
        ExprSeqOpt(metaDomain, metaEntity, arity, hasEnum).get,
        ExprSeqTac(metaDomain, metaEntity, arity, hasEnum).get,
      )
  } else Nil

  val exprMap = if (hasMap) List(0, 1, 2).flatMap {
    case 0     =>
      List(
        ExprMapTac(metaDomain, metaEntity, 0).get
      )
    case arity =>
      List(
        ExprMapMan(metaDomain, metaEntity, arity).get,
        ExprMapOpt(metaDomain, metaEntity, arity).get,
        ExprMapTac(metaDomain, metaEntity, arity).get,
      )
  } else Nil

  val exprByteArray = if (hasByteArray) List(0, 1, 2).flatMap {
    case 0     =>
      List(
        ExprByteArrayTac(metaDomain, metaEntity, 0).get
      )
    case arity =>
      List(
        ExprByteArrayMan(metaDomain, metaEntity, arity).get,
        ExprByteArrayOpt(metaDomain, metaEntity, arity).get,
        ExprByteArrayTac(metaDomain, metaEntity, arity).get,
      )
  } else Nil


  val exprs = (exprOne ++ exprSet ++ exprSeq ++ exprMap ++ exprByteArray).map(_.trim).mkString("\n\n\n")

  def get: String =
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
       |package $pkg.$domain
       |package ops // to access enums and let them be public to the user
       |
       |$imports
       |
       |$exprs
       |""".stripMargin
}
