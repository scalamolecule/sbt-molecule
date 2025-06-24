package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class Entity_Builder(
  metaDomain: MetaDomain,
  entityList: Seq[String],
  attrList: Seq[String],
  metaEntity: MetaEntity,
  arity: Int,
) extends Formatting(metaDomain, metaEntity, arity) {

//  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
//  private val attrList  : Seq[String] = {
//    for {
//      segment <- metaDomain.segments
//      entity <- segment.entities
//      attribute <- entity.attributes
//    } yield entity.entity + "." + attribute.attribute
//  }

  private val man = List.newBuilder[String]
  private val opt = List.newBuilder[String]
  private val tac = List.newBuilder[String]
  private val ref = List.newBuilder[String]

  private var hasOne       = false
  private var hasSet       = false
  private var hasSeq       = false
  private var hasMap       = false
  private var hasByteArray = false
  private var seqCount     = 0

  private val ptMax = {
    attributes.map(a => a.cardinality match {
      case _: CardOne =>
        hasOne = true
        getTpe(a.baseTpe).length // Account for "ID" type being String
      case _: CardSet =>
        hasSet = true
        5 + getTpe(a.baseTpe).length // Account for "ID" type being String
      case _: CardSeq =>
        hasSeq = true
        if (a.baseTpe == "Byte") {
          hasByteArray = true
          7 + 4 // "Byte".length
        } else {
          seqCount += 1
          5 + a.baseTpe.length
        }
      case _: CardMap =>
        hasMap = true
        13 + a.baseTpe.length
    }).max
  }

  private val hasEnum = attributes.exists {
    case a if a.enumTpe.isDefined => true
    case _                        => false
  }

  private val ptOne       = (t: String) => " " * (ptMax - t.length)
  private val ptSet       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptSeq       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptMap       = (t: String) => " " * (ptMax - 13 - t.length)
  private val ptByteArray = " " * (ptMax - 7 - 4)

  attributes.foreach {
    case MetaAttribute(attr, card, baseType0, _, optRef, optEnum, _, _, _, _, _, _) =>
      val isByteArray = card == CardSeq && baseType0 == "Byte"
      val c           = if (isByteArray) "BAr" else card._marker
      val baseType    = getTpe(baseType0)
      val baseType1   = optEnum.getOrElse(baseType)
      val isEnum      = optEnum.isDefined
      val padA        = padAttr(attr)
      val pad1        = padType1(baseType1)

      val (tM, tO) = card match {
        case _: CardOne                       => (baseType + ptOne(baseType), s"Option[$baseType" + ptOne(baseType) + "]")
        case _: CardSet                       => (s"Set[$baseType]" + ptSet(baseType), s"Option[Set[$baseType]" + ptSet(baseType) + "]")
        case _: CardSeq if baseType == "Byte" => (s"Array[$baseType]" + ptByteArray, s"Option[Array[$baseType]" + ptByteArray + "]")
        case _: CardSeq                       => (s"Seq[$baseType]" + ptSeq(baseType), s"Option[Seq[$baseType]" + ptSeq(baseType) + "]")
        case _: CardMap                       => (s"Map[String, $baseType]" + ptMap(baseType), s"Option[Map[String, $baseType]" + ptMap(baseType) + "]")
      }

      val (tpesM, tpesO, tpesT) = {
        if (first)
          (
            s"Tuple1[$tM], $baseType1$pad1",
            s"Tuple1[$tO], $baseType1$pad1",
            s"$baseType1$pad1"
          )
        else
          (
            s"$tM *: Tpl, $baseType1$pad1",
            s"$tO *: Tpl, $baseType1$pad1",
            s"Tpl, $baseType1$pad1"
          )
      }

      lazy val elemsM = s"dataModel.add(${attr}_man$padA)"
      lazy val elemsO = s"dataModel.add(${attr}_opt$padA)"
      lazy val elemsT = s"dataModel.add(${attr}_tac$padA)"

      val filtersMan = if (card == CardOne && attr != "id" && optRef.isEmpty) baseType match {
        case _ if isEnum                                  => "_Enum   "
        case "String"                                     => "_String "
        case "Int" | "Long" | "BigInt" | "Byte" | "Short" => "_Integer"
        case "Double" | "BigDecimal" | "Float"            => "_Decimal"
        case "Boolean"                                    => "_Boolean"
        case _                                            => "        "
      } else if (isEnum)
        "_Enum   "
      else
        "        "

      val filtersOpt = if (isEnum) "_Enum" else if (hasEnum) "     " else ""
      val filtersTac = if (card == CardOne && attr != "id" && optRef.isEmpty) baseType match {
        case _ if isEnum                                  => "_Enum   "
        case "String"                                     => "_String "
        case "Int" | "Long" | "BigInt" | "Byte" | "Short" => "_Integer"
        case _                                            => "        "
      } else if (isEnum)
        "_Enum   "
      else
        "        "

      lazy val exprM = s"Expr${c}Man$filtersMan[$tpesM]"
      lazy val exprO = s"Expr${c}Opt$filtersOpt[$tpesO]"
      lazy val exprT = s"Expr${c}Tac$filtersTac[$tpesT]"

      man += s"""def $attr  $padA = new ${entB}_$exprM($elemsM)"""
      if (attr != "id")
        opt += s"""def ${attr}_?$padA = new ${entB}_$exprO($elemsO)"""
      tac += s"""def ${attr}_ $padA = new ${entA}_$exprT($elemsT)"""
  }


  private val hasRefOne  = refs.exists(_.cardinality == CardOne)
  private val hasRefMany = refs.exists(_.cardinality == CardSet)
  refs.collect {
    case MetaAttribute(attr, card, _, _, Some(ref0), _, options, _, _, _, _, _) =>
      val refName        = camel(attr)
      val pRefAttr       = padRefAttr(attr)
      val pRef           = padRefEntity(ref0)
      val ref_X          = ref0 + cur
      val nsIndex        = entityList.indexOf(entity)
      val refAttrIndex   = attrList.indexOf(entity + "." + attr)
      val refEntityIndex = entityList.indexOf(ref0)
      val isOwner        = options.contains("owner")
      val owner          = s"$isOwner" + (if (isOwner) " " else "") // align true/false
      val coord          = s"List($nsIndex, $refAttrIndex, $refEntityIndex)"
      val refObj         = s"""_m.Ref("$entity", "$attr"$pRefAttr, "$ref0"$pRef, $card, $owner, $coord)"""
      val tpl            = if (first) "" else "Tpl, "
      if (card == CardOne) {
        ref += s"""object $refName$pRefAttr extends $ref_X$pRef[${tpl}Nothing](dataModel.add($refObj)) with OptRefInit"""
      } else {
        ref += s"""object $refName$pRefAttr extends $ref_X$pRef[${tpl}Nothing](dataModel.add($refObj)) with NestedInit"""
      }
  }

  private val manAttrs = man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs = opt.result().mkString("", "\n  ", "\n\n  ")
  private val tacAttrs = tac.result().mkString("\n  ")


  private val optRefInit = if (hasRefOne) {
    if (first)
      s"""trait OptRefInit { self: Molecule =>
         |    def ?[T              ](optRef: Molecule_1[T     ]) = new $entB[Tuple1[Option[T              ]], Nothing](addOptRef(self, optRef))
         |    def ?[RefTpl <: Tuple](optRef: Molecule_n[RefTpl]) = new $entB[Tuple1[Option[Reverse[RefTpl]]], Nothing](addOptRef(self, optRef))
         |  }""".stripMargin
    else
      s"""trait OptRefInit { self: Molecule =>
         |    def ?[T              ](optRef: Molecule_1[T     ]) = new $entB[Option[T              ] *: Tpl, Nothing](addOptRef(self, optRef))
         |    def ?[RefTpl <: Tuple](optRef: Molecule_n[RefTpl]) = new $entB[Option[Reverse[RefTpl]] *: Tpl, Nothing](addOptRef(self, optRef))
         |  }""".stripMargin
  } else ""

  private val nestedInit = if (hasRefMany) {
    if (first)
      s"""trait NestedInit { self: Molecule =>
         |    def * [T                 ](nested: Molecule_1[T        ]) = new $entB[Tuple1[Seq[T                 ]], Nothing](addNested(self, nested))
         |    def *?[T                 ](nested: Molecule_1[T        ]) = new $entB[Tuple1[Seq[T                 ]], Nothing](addOptNested(self, nested))
         |    def * [NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[Tuple1[Seq[Reverse[NestedTpl]]], Nothing](addNested(self, nested))
         |    def *?[NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[Tuple1[Seq[Reverse[NestedTpl]]], Nothing](addOptNested(self, nested))
         |  }""".stripMargin
    else
      s"""trait NestedInit { self: Molecule =>
         |    def * [T                 ](nested: Molecule_1[T        ]) = new $entB[Seq[T                 ] *: Tpl, Nothing](addNested(self, nested))
         |    def *?[T                 ](nested: Molecule_1[T        ]) = new $entB[Seq[T                 ] *: Tpl, Nothing](addOptNested(self, nested))
         |    def * [NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[Seq[Reverse[NestedTpl]] *: Tpl, Nothing](addNested(self, nested))
         |    def *?[NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[Seq[Reverse[NestedTpl]] *: Tpl, Nothing](addOptNested(self, nested))
         |  }""".stripMargin
  } else ""

  private val refResult = ref.result()
  private val refDefs   = if (refResult.isEmpty) "" else refResult.mkString("\n\n  ", "\n  ", "")

  private val backRefDefs = if (backRefs.isEmpty) "" else {
    val max = backRefs.map(_.length).max
    backRefs.flatMap { backRef =>
      if (entity == backRef) None else {
        val pad             = padS(max, backRef)
        val prevEntityIndex = entityList.indexOf(backRef)
        val curEntityIndex  = entityList.indexOf(entity)
        val coord           = s"List($prevEntityIndex, $curEntityIndex)"
        val backRef_X       = backRef + cur
        val tpl             = if (first) "Nothing" else "Tpl, Nothing"
        Some(s"""object _$backRef$pad extends $backRef_X$pad[$tpl](dataModel.add(_m.BackRef("$backRef", "$entity", $coord)))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }


  private val (refClass, refClassBody) = if (refResult.isEmpty && backRefs.isEmpty) {
    ("", "")
  } else {
    val (t1, t2)   = if (first) ("", "") else ("[Tpl]", "[Tpl <: Tuple]")
    val refHandles = List(optRefInit, nestedInit, refDefs, backRefDefs).map(_.trim).filterNot(_.isEmpty).mkString("\n\n  ")
    (
      s"$entity_refs_cur$t1(dataModel) with ",
      s"""
         |
         |class $entity_refs_cur$t2(dataModel: DataModel) extends ModelTransformations_ {
         |  $refHandles
         |}""".stripMargin
    )
  }


  val t1 = if (first) "T" else "Tpl <: Tuple, T"
  private val (n, t2) = arity match {
    case 0 => ("0", "")
    case 1 => ("1", "[Head[Tpl]]")
    case _ => ("n", "[Tpl]")
  }

  def get: String =
    s"""class ${entity}_$n[$t1](override val dataModel: DataModel)
       |  extends $refClass${entity}_attrs with Molecule_$n$t2 with ModelTransformations_ {
       |
       |  $manAttrs$optAttrs$tacAttrs
       |}$refClassBody
       |""".stripMargin

//  def get: String =
//    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
//       |package $pkg.$domain
//       |package ops // to access enums and let them be public to the user
//       |
//       |$imports
//       |
//       |class ${entity}_$n[$t1](override val dataModel: DataModel)
//       |  extends $refClass${entity}_attrs with Molecule_$n$t2 with ModelTransformations_ {
//       |
//       |  $manAttrs$optAttrs$tacAttrs
//       |}$refClassBody
//       |""".stripMargin
}
