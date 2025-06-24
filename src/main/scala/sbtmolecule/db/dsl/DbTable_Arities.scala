package sbtmolecule.db.dsl

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class DbTable_Arities(
  metaDomain: MetaDomain,
  entityList: Seq[String],
  attrList: Seq[String],
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity) {

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
//        case _: CardOne                       => (baseType + ptOne(baseType), s"Option[$baseType]" + ptOne(baseType))
//        case _: CardSet                       => (s"Set[$baseType]" + ptSet(baseType), s"Option[Set[$baseType]]" + ptSet(baseType))
//        case _: CardSeq if baseType == "Byte" => (s"Array[$baseType]" + ptByteArray, s"Option[Array[$baseType]]" + ptByteArray)
//        case _: CardSeq                       => (s"Seq[$baseType]" + ptSeq(baseType), s"Option[Seq[$baseType]]" + ptSeq(baseType))
//        case _: CardMap                       => (s"Map[String, $baseType]" + ptMap(baseType), s"Option[Map[String, $baseType]]" + ptMap(baseType))

        case _: CardOne                       => (baseType + ptOne(baseType), s"Option[$baseType" + ptOne(baseType) + "]")
        case _: CardSet                       => (s"Set[$baseType]" + ptSet(baseType), s"Option[Set[$baseType]" + ptSet(baseType) + "]")
        case _: CardSeq if baseType == "Byte" => (s"Array[$baseType]" + ptByteArray, s"Option[Array[$baseType]" + ptByteArray + "]")
        case _: CardSeq                       => (s"Seq[$baseType]" + ptSeq(baseType), s"Option[Seq[$baseType]" + ptSeq(baseType) + "]")
        case _: CardMap                       => (s"Map[String, $baseType]" + ptMap(baseType), s"Option[Map[String, $baseType]" + ptMap(baseType) + "]")
      }

      val (tpesM, tpesO, tpesT) = if (first)
        (
          s"Tuple1[$tM], $baseType1$pad1",
          s"Tuple1[$tO], $baseType1$pad1",
          s"Tpl, $baseType1$pad1"
        )
      else
        (
          s"$tM *: Tpl, $baseType1$pad1",
          s"$tO *: Tpl, $baseType1$pad1",
          s"Tpl, $baseType1$pad1"
        )

      lazy val elemsM = s"dataModel.add(${attr}_man$padA)"
      lazy val elemsO = s"dataModel.add(${attr}_opt$padA)"
      lazy val elemsT = s"dataModel.add(${attr}_tac$padA)"

      val filtersMan = if (card == CardOne && attr != "id" && optRef.isEmpty) baseType match {
        case _ if isEnum                                  => "_Enum    "
        case "String"                                     => "_String  "
        case "Int" | "Long" | "BigInt" | "Byte" | "Short" => "_Integer "
        case "Double" | "BigDecimal" | "Float"            => "_Decimal "
        case "Boolean"                                    => "_Boolean "
        case _                                            => "         "
      } else if (isEnum)
        "_Enum    "
      else
        "         "

      val filtersOpt = if (isEnum) "_Enum " else if (hasEnum) "      " else ""
      val filtersTac = if (card == CardOne && attr != "id" && optRef.isEmpty) baseType match {
        case _ if isEnum                                  => "_Enum    "
        case "String"                                     => "_String  "
        case "Int" | "Long" | "BigInt" | "Byte" | "Short" => "_Integer "
        case _                                            => "         "
      } else if (isEnum)
        "_Enum    "
      else
        "         "

      lazy val exprM = s"Expr${c}Man$filtersMan[$tpesM, $entB, $entC]"
      lazy val exprO = s"Expr${c}Opt$filtersOpt[$tpesO, $entB, $entC]"
      lazy val exprT = s"Expr${c}Tac$filtersTac[$tpesT, $entA, $entB]"

      man += s"""def $attr  $padA = new $entB[$tpesM]($elemsM) with $exprM with $card"""
      if (attr != "id")
        opt += s"""def ${attr}_?$padA = new $entB[$tpesO]($elemsO) with $exprO with $card"""
      tac += s"""def ${attr}_ $padA = new $entA[$tpesT]($elemsT) with $exprT with $card"""
  }


  private val resolvers = if (first) {
    val (p1, p2)  = if (hasMap) ("           ", "  ") else ("", "")
    (if (hasOne) {
      s"""
         |  override protected def _exprOneTac(op: _op, vs: Seq[T], binding: Boolean) = new $entA[Tpl$p1, T](addOne$p2(dataModel, op, vs, binding)) with CardOne""".stripMargin
    } else "") +
      // Presuming that there's always cardinality-one attributes...
      (if (hasSet) {
        s"""
           |  override protected def _exprSet   (op: _op, vs: Set[T]                  ) = new $entA[Tpl$p1, T](addSet$p2(dataModel, op, vs         )) with CardSet""".stripMargin
      } else "") +
      (if (hasSeq && seqCount > 0) { // could be Array[Byte] only
        s"""
           |  override protected def _exprSeq   (op: _op, vs: Seq[T]                  ) = new $entA[Tpl$p1, T](addSeq$p2(dataModel, op, vs         )) with CardSeq""".stripMargin
      } else "") +
      (if (hasByteArray) {
        s"""
           |  override protected def _exprBAr   (op: _op, ba: Array[T]                ) = new $entA[Tpl$p1, T](addBAr$p2(dataModel, op, ba         )) with CardSeq""".stripMargin
      } else "") +
      (if (hasMap) {
        s"""
           |  override protected def _exprMap   (op: _op, map : Map[String, T]        ) = new $entA[Tpl           , T](addMap  (dataModel, op, map        )) with CardMap
           |  override protected def _exprMapK  (op: _op, keys: Seq[String]           ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, op, keys       )) with CardMap
           |  override protected def _exprMapV  (op: _op, vs  : Seq[T]                ) = new $entA[Tpl           , T](addMapVs(dataModel, op, vs         )) with CardMap""".stripMargin
      } else "")
  } else {
    val p1 = if (hasMap) "   " else ""
    (if (hasOne) {
      s"""
         |  override protected def _sort      (sort: String                         ) = new $entA[Tpl                $p1, T     ](addSort  (dataModel, sort           ))
         |  override protected def _aggrInt   (kw: _kw                              ) = new $entA[Int *: Tail[Tpl]   $p1, Int   ](toInt    (dataModel, kw             )) with SortAttrs[Int *: Tail[Tpl]   , Int   , $entA]
         |  override protected def _aggrT     (kw: _kw                              ) = new $entA[Tpl                $p1, T     ](asIs     (dataModel, kw             )) with SortAttrs[Tpl                , T     , $entA]
         |  override protected def _aggrDouble(kw: _kw                              ) = new $entA[Double *: Tail[Tpl]$p1, Double](toDouble (dataModel, kw             )) with SortAttrs[Double *: Tail[Tpl], Double, $entA]
         |  override protected def _aggrSet   (kw: _kw, n: Option[Int]              ) = new $entA[Set[T] *: Tail[Tpl]$p1, T     ](asIs     (dataModel, kw, n          ))
         |  override protected def _aggrDist  (kw: _kw                              ) = new $entA[Set[T] *: Tail[Tpl]$p1, T     ](asIs     (dataModel, kw             ))
         |  override protected def _exprOneMan(op: _op, vs: Seq[T], binding: Boolean) = new $entA[Tpl                $p1, T     ](addOne   (dataModel, op, vs, binding)) with SortAttrs[Tpl, T, $entA] with CardOne
         |  override protected def _exprOneOpt(op: _op, v : Option[T]               ) = new $entA[Tpl                $p1, T     ](addOneOpt(dataModel, op, v          )) with SortAttrs[Tpl, T, $entA]
         |  override protected def _exprOneTac(op: _op, vs: Seq[T], binding: Boolean) = new $entA[Tpl                $p1, T     ](addOne   (dataModel, op, vs, binding)) with CardOne""".stripMargin
    } else "") +
      // Presuming that there's always cardinality-one attributes...
      (if (hasSet) {
        s"""
           |  override protected def _exprSet   (op: _op, vs: Set[T]                  ) = new $entA[Tpl                $p1, T     ](addSet   (dataModel, op, vs         )) with CardSet
           |  override protected def _exprSetOpt(op: _op, vs: Option[Set[T]]          ) = new $entA[Tpl                $p1, T     ](addSetOpt(dataModel, op, vs         ))""".stripMargin
      } else "") +
      (if (hasSeq && seqCount > 0) { // could be Array[Byte] only
        s"""
           |  override protected def _exprSeq   (op: _op, vs: Seq[T]                  ) = new $entA[Tpl                $p1, T     ](addSeq   (dataModel, op, vs         )) with CardSeq
           |  override protected def _exprSeqOpt(op: _op, vs: Option[Seq[T]]          ) = new $entA[Tpl                $p1, T     ](addSeqOpt(dataModel, op, vs         ))""".stripMargin
      } else "") +
      (if (hasByteArray) {
        s"""
           |  override protected def _exprBAr   (op: _op, ba: Array[T]                ) = new $entA[Tpl                $p1, T     ](addBAr   (dataModel, op, ba         )) with CardSeq
           |  override protected def _exprBArOpt(op: _op, ba: Option[Array[T]]        ) = new $entA[Tpl                $p1, T     ](addBArOpt(dataModel, op, ba         ))""".stripMargin
      } else "") +
      (if (hasMap) {
        s"""
           |  override protected def _exprMap   (op: _op, map : Map[String, T]        ) = new $entA[Tpl                   , T     ](addMap   (dataModel, op, map        )) with CardMap
           |  override protected def _exprMapK  (op: _op, keys: Seq[String]           ) = new $entA[T *: Tail[Tpl]        , T     ](addMapKs (dataModel, op, keys       )) with CardMap
           |  override protected def _exprMapV  (op: _op, vs  : Seq[T]                ) = new $entA[Tpl                   , T     ](addMapVs (dataModel, op, vs         )) with CardMap
           |  override protected def _exprMapOpt(op: _op, map : Option[Map[String, T]]) = new $entA[Tpl                   , T     ](addMapOpt(dataModel, op, map        )) with CardMap
           |  override protected def _exprMapOpK(op: _op, key : String                ) = new $entA[Option[T] *: Tail[Tpl], T     ](addMapKs (dataModel, op, Seq(key)   )) with CardMap""".stripMargin
      } else "")
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
      val refObj         = s"""_dm.Ref("$entity", "$attr"$pRefAttr, "$ref0"$pRef, $card, $owner, $coord)"""
      val tpl            = if (first) "EmptyTuple" else "Tpl"
      if (card == CardOne) {
        ref += s"""object $refName$pRefAttr extends $ref_X$pRef[$tpl, Nothing](dataModel.add($refObj)) with OptRefInit"""
      } else {
        ref += s"""object $refName$pRefAttr extends $ref_X$pRef[$tpl, Nothing](dataModel.add($refObj)) with NestedInit"""
      }
  }

  private val manAttrs = man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs = opt.result().mkString("", "\n  ", "\n\n  ")
  private val tacAttrs = tac.result().mkString("\n  ")

  private val dataModel = "override val dataModel: _dm.DataModel"

  private val expressionOps = if (first) {
    (if (hasOne)
      s"""
         |    with ExprOneTacOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasSet)
        s"""
           |    with ExprSetTacOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasSeq)
        s"""
           |    with ExprSeqTacOps[Tpl, T, $entA, $entB]
           |    with ExprBArTacOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasMap)
        s"""
           |    with ExprMapTacOps[Tpl, T, $entA, $entB]""".stripMargin else "")
  } else {
    (if (hasOne)
      s"""
         |    with SortAttrsOps[Tpl, T, $entA]
         |    with AggregatesOps[Tpl, T, $entA]
         |    with ExprOneManOps[Tpl, T, $entA, $entB]
         |    with ExprOneOptOps[Tpl, T, $entA, $entB]
         |    with ExprOneTacOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasSet)
        s"""
           |    with ExprSetOptOps[Tpl, T, $entA, $entB]
           |    with ExprSetTacOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasSeq)
        s"""
           |    with ExprSeqOptOps[Tpl, T, $entA, $entB]
           |    with ExprSeqTacOps[Tpl, T, $entA, $entB]
           |    with ExprBArTacOps[Tpl, T, $entA, $entB]
           |    with ExprBArOptOps[Tpl, T, $entA, $entB]""".stripMargin else "") +
      (if (hasMap)
        s"""
           |    with ExprMapOptOps[Tpl, T, $entA, $entB]
           |    with ExprMapTacOps[Tpl, T, $entA, $entB]""".stripMargin else "")
  }

  private val filterAttrs = if (first)
    s"""
       |
       |  override protected def _filterAttrTacTac(op: _op, a: Molecule_0 & CardOne)(using NotTuple[T])                      = new $entA[Tpl     , T](filterAttr(dataModel, op, a))
       |  override protected def _filterAttrTacMan[ns[_ <: Tuple, _]](op: _op, a: Molecule & SortAttrsOps[Tuple1[T], T, ns]) = new $entB[T *: Tpl, T](filterAttr(dataModel, op, a))""".stripMargin
  else
    s"""
       |
       |  override protected def _filterAttrTacTac(op: _op, a: Molecule_0 & CardOne)(using NotTuple[T])                      = new $entA[Tpl     , T](filterAttr(dataModel, op, a))
       |  override protected def _filterAttrManTac(op: _op, a: Molecule_0 & CardOne)(using NotTuple[T])                      = new $entA[Tpl     , T](filterAttr(dataModel, op, a)) with SortAttrs[Tpl     , T, $entA]
       |  override protected def _filterAttrTacMan[ns[_ <: Tuple, _]](op: _op, a: Molecule & SortAttrsOps[Tuple1[T], T, ns]) = new $entB[T *: Tpl, T](filterAttr(dataModel, op, a))
       |  override protected def _filterAttrManMan[ns[_ <: Tuple, _]](op: _op, a: Molecule & SortAttrsOps[Tuple1[T], T, ns]) = new $entB[T *: Tpl, T](filterAttr(dataModel, op, a)) with SortAttrs[T *: Tpl, T, $entB]""".stripMargin

  private val optRefInit = if (hasRefOne) {
    val (t1, t2) = if (first)
      (
        "Tuple1[RefT           ]",
        "Tuple1[Reverse[RefTpl]]"
      )
    else
      (
        "Option[RefT           ] *: Tpl",
        "Option[Reverse[RefTpl]] *: Tpl"
      )

    s"""trait OptRefInit { self: Molecule =>
       |
       |    def ?[T_or_Tpl](optRef: MoleculeBase[T_or_Tpl]) = new $entB[Option[T_or_Tpl] *: Tpl, Nothing](addOptRef(self, optRef))
       |
       | //   def ?[RefT](optRef: Molecule_1[RefT])(using NotTuple[RefT]) = new $entB[$t1, Nothing](addOptRef(self, optRef))
       | //   def ?[RefTpl <: Tuple](optRef: Molecule_n[Reverse[RefTpl]]) = new $entB[$t2, Nothing](addOptRef(self, optRef))
       |  }""".stripMargin
  } else ""

  private val nestedInit = if (hasRefMany) {
    val (t1, t2) = if (first)
      (
        "Tuple1[Seq[NestedT           ]]",
        "Tuple1[Seq[Reverse[NestedTpl]]]"
      )
    else
      (
        "Seq[NestedT           ] *: Tpl",
        "Seq[Reverse[NestedTpl]] *: Tpl"
      )
    s"""trait NestedInit { self: Molecule =>
       |    def * [NestedT](nested: Molecule_1[NestedT])(using NotTuple[NestedT]) = new $entB[$t1, Nothing](addNested(self, nested))
       |    def *?[NestedT](nested: Molecule_1[NestedT])(using NotTuple[NestedT]) = new $entB[$t1, Nothing](addOptNested(self, nested))
       |
       |    def * [NestedTpl <: Tuple](nested: Molecule_n[Reverse[NestedTpl]])    = new $entB[$t2, Nothing](addNested(self, nested))
       |    def *?[NestedTpl <: Tuple](nested: Molecule_n[Reverse[NestedTpl]])    = new $entB[$t2, Nothing](addOptNested(self, nested))
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
        val tpl             = if (first) "EmptyTuple" else "Tpl"
        Some(s"""object _$backRef$pad extends $backRef_X$pad[$tpl, Nothing](dataModel.add(_dm.BackRef("$backRef", "$entity", $coord)))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }

  private val (refClass, refClassBody) = if (refResult.isEmpty && backRefs.isEmpty) {
    ("", "")
  } else {
    val (t1, t2)   = if (first) ("T", "") else ("Tpl", "[Tpl <: Tuple]")
    val refHandles = List(optRefInit, nestedInit, refDefs, backRefDefs).map(_.trim).filterNot(_.isEmpty).mkString("\n\n  ")
    (
      s"$entity_refs_cur(dataModel) with ",
      s"""
         |
         |class $entity_refs_cur$t2(dataModel: _dm.DataModel) extends ModelTransformations_ {
         |  $refHandles
         |}""".stripMargin
    )
  }

  private val molecule = arity match {
    case 0 => "Molecule_0"
    case 1 => "Molecule_1[Head[Tpl]]"
    case _ => "Molecule_n[Tpl]"
  }

  def get: String =
    s"""class $entA[Tpl <: Tuple, T]($dataModel)
       |  extends $refClass${entity}_base
       |    with $molecule
       |    with ModelTransformations_
       |    with FilterAttr[Tpl, T, $entA, $entB]$expressionOps {
       |
       |  $manAttrs$optAttrs$tacAttrs
       |  $resolvers$filterAttrs
       |}$refClassBody
       |""".stripMargin
}
