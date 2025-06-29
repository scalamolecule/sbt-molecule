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
  private val res = List.newBuilder[String]
  private val ref = List.newBuilder[String]

  private val first      = arity == 0
  private val last       = arity == metaDomain.maxArity
  private val secondLast = arity == metaDomain.maxArity - 1

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

      val padA = padAttr(attr)
      val pad1 = padType1(baseType1)
      val pad2 = " " * ("Option[]".length + ptMax)

      val (tM, tO) = card match {
        case _: CardOne                       => (baseType + ptOne(baseType), s"Option[$baseType]" + ptOne(baseType))
        case _: CardSet                       => (s"Set[$baseType]" + ptSet(baseType), s"Option[Set[$baseType]]" + ptSet(baseType))
        case _: CardSeq if baseType == "Byte" => (s"Array[$baseType]" + ptByteArray, s"Option[Array[$baseType]]" + ptByteArray)
        case _: CardSeq                       => (s"Seq[$baseType]" + ptSeq(baseType), s"Option[Seq[$baseType]]" + ptSeq(baseType))
        case _: CardMap                       => (s"Map[String, $baseType]" + ptMap(baseType), s"Option[Map[String, $baseType]]" + ptMap(baseType))
      }

      lazy val tpesM = s"${`A..V, `}$tM        , $baseType1$pad1"
      lazy val tpesO = s"${`A..V, `}$tO, $baseType1$pad1"
      lazy val tpesT = if (arity == 0)
        s"${`A..V`} $pad2 $baseType1$pad1"
      else if (arity == metaDomain.maxArity)
        s"${`A..V`}, $baseType1$pad1"
      else
        s"${`A..V`}  $pad2, $baseType1$pad1"

      lazy val elemsM = s"dataModel.add(${attr}_man$padA)"
      lazy val elemsO = s"dataModel.add(${attr}_opt$padA)"
      lazy val elemsT = s"dataModel.add(${attr}_tac$padA)"

      val nextNextEntity = if (secondLast) s"X${arity + 3}" else ent_2
      val nextEntity     = if (last) s"X${arity + 2}" else ent_1

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

      val filtersOpt = if (isEnum) "_Enum    " else "         "
      val filtersTac = if (card == CardOne && attr != "id" && optRef.isEmpty) baseType match {
        case _ if isEnum                                  => "_Enum    "
        case "String"                                     => "_String  "
        case "Int" | "Long" | "BigInt" | "Byte" | "Short" => "_Integer "
        case _                                            => "         "
      } else if (isEnum)
        "_Enum    "
      else
        "         "

      lazy val exprM = s"Expr${c}Man${_1}$filtersMan[$tpesM, $ent_1, $nextNextEntity]"
      lazy val exprO = s"Expr${c}Opt${_1}$filtersOpt[$tpesO, $ent_1, $nextNextEntity]"
      lazy val exprT = s"Expr${c}Tac${_0}$filtersTac[$tpesT, $ent_0, $nextEntity]"

      if (!last) {
        man += s"""lazy val $attr  $padA = new $ent_1[$tpesM]($elemsM) with $exprM with $card"""
        if (attr != "id")
          opt += s"""lazy val ${attr}_?$padA = new $ent_1[$tpesO]($elemsO) with $exprO with $card"""
      }
      tac += s"""lazy val ${attr}_ $padA = new $ent_0[$tpesT]($elemsT) with $exprT with $card"""
  }

  if (first) {
    val pArg = if (hasMap) "  " else ""
    if (hasOne) {
      res += s"""override protected def _exprOneTac(op: Op, vs$pArg: Seq[t], binding: Boolean) = new $ent_0[t](addOne  (dataModel, op, vs, binding)) with CardOne"""
    }
    if (hasSet) {
      res += s"""override protected def _exprSet   (op: Op, vs$pArg: Set[t]                  ) = new $ent_0[t](addSet  (dataModel, op, vs         )) with CardSet"""
    }
    if (hasSeq && seqCount > 0) {
      res += s"""override protected def _exprSeq   (op: Op, vs$pArg: Seq[t]                  ) = new $ent_0[t](addSeq  (dataModel, op, vs         )) with CardSet"""
    }
    if (hasByteArray) {
      res += s"""override protected def _exprBAr   (op: Op, ba$pArg: Array[t]                ) = new $ent_0[t](addBAr  (dataModel, op, ba         )) with CardSet"""
    }
    if (hasMap) {
      res += s"""override protected def _exprMap   (op: Op, map : Map[String, t]          ) = new $ent_0[t](addMap  (dataModel, op, map        )) with CardMap"""
      res += s"""override protected def _exprMapK  (op: Op, keys: Seq[String]             ) = new $ent_0[t](addMapKs(dataModel, op, keys       )) with CardMap"""
      res += s"""override protected def _exprMapV  (op: Op, vs  : Seq[t]                  ) = new $ent_0[t](addMapVs(dataModel, op, vs         )) with CardMap"""
    }

  } else {
    val tMap = if (hasMap) "   " else ""

    val ttInt_ = s"${`A..U`}Int   , Int   "
    val ttT___ = s"${`A..U`}t     , t     "
    val ttDoub = s"${`A..U`}Double, Double"
    val ttA___ = s"${`A..V`}     , t     "

    val tInt_ = s"${`A..U`}Int   $tMap, Int   "
    val tDoub = s"${`A..U`}Double$tMap, Double"
    val tDist = s"${`A..U`}Set[$V]$tMap, t     "
    val tSet_ = s"${`A..U`}Set[t]$tMap, t     "
    val tT___ = s"${`A..U`}t     $tMap, t     "
    val tA___ = s"${`A..V`}     $tMap, t     "
    val uA___ = s"${`A..V`}     $tMap, t     "
    val tO___ = s"${`A..U`}Option[t], t     "

    def agg1 = s"override protected def _aggrInt   (kw: Kw                              ) = new $ent_0"
    def agg2 = s"override protected def _aggrT     (kw: Kw                              ) = new $ent_0"
    def agg3 = s"override protected def _aggrDouble(kw: Kw                              ) = new $ent_0"
    def agg4 = s"override protected def _aggrSet   (kw: Kw, n: Option[Int]              ) = new $ent_0"
    def agg5 = s"override protected def _aggrDist  (kw: Kw                              ) = new $ent_0"

    def one1 = s"override protected def _exprOneMan(op: Op, vs: Seq[t], binding: Boolean) = new $ent_0"
    def one2 = s"override protected def _exprOneOpt(op: Op, v : Option[t]               ) = new $ent_0"
    def one3 = s"override protected def _exprOneTac(op: Op, vs: Seq[t], binding: Boolean) = new $ent_0"

    def set1 = s"override protected def _exprSet   (op: Op, vs: Set[t]                  ) = new $ent_0"
    def set2 = s"override protected def _exprSetOpt(op: Op, vs: Option[Set[t]]          ) = new $ent_0"

    def seq1 = s"override protected def _exprSeq   (op: Op, vs: Seq[t]                  ) = new $ent_0"
    def seq2 = s"override protected def _exprSeqOpt(op: Op, vs: Option[Seq[t]]          ) = new $ent_0"

    def bar1 = s"override protected def _exprBAr   (op: Op, ba: Array[t]                ) = new $ent_0"
    def bar2 = s"override protected def _exprBArOpt(op: Op, ba: Option[Array[t]]        ) = new $ent_0"

    def map1 = s"override protected def _exprMap   (op: Op, map : Map[String, t]        ) = new $ent_0"
    def map2 = s"override protected def _exprMapK  (op: Op, keys: Seq[String]           ) = new $ent_0"
    def map3 = s"override protected def _exprMapV  (op: Op, vs  : Seq[t]                ) = new $ent_0"
    def map4 = s"override protected def _exprMapOpt(op: Op, map : Option[Map[String, t]]) = new $ent_0"
    def map5 = s"override protected def _exprMapOpK(op: Op, key : String                ) = new $ent_0"

    def sort = s"override protected def _sort      (sort: String                        ) = new $ent_0"


    if (hasOne || hasSet) {
      res += s"$agg1[$tInt_](toInt    (dataModel, kw             )) with SortAttrs_$arity[$ttInt_, $ent_0]"
      res += s"$agg2[$tT___](asIs     (dataModel, kw             )) with SortAttrs_$arity[$ttT___, $ent_0]"
      res += s"$agg3[$tDoub](toDouble (dataModel, kw             )) with SortAttrs_$arity[$ttDoub, $ent_0]"
      res += s"$agg4[$tSet_](asIs     (dataModel, kw, n          ))"
      res += s"$agg5[$tDist](asIs     (dataModel, kw             ))"
    }
    if (hasOne) {
      res += s"$one1[$tA___](addOne   (dataModel, op, vs, binding)) with SortAttrs_$arity[$ttA___, $ent_0] with CardOne"
      res += s"$one2[$tA___](addOneOpt(dataModel, op, v          )) with SortAttrs_$arity[$ttA___, $ent_0]"
      res += s"$one3[$tA___](addOne   (dataModel, op, vs, binding)) with CardOne"
    }
    if (hasSet) {
      res += s"$set1[$tA___](addSet   (dataModel, op, vs         )) with CardSet"
      res += s"$set2[$tA___](addSetOpt(dataModel, op, vs         ))"
    }
    if (hasSeq && seqCount > 0) {
      res += s"$seq1[$uA___](addSeq   (dataModel, op, vs         )) with CardSeq"
      res += s"$seq2[$uA___](addSeqOpt(dataModel, op, vs         ))"
    }
    if (hasByteArray) {
      res += s"$bar1[$uA___](addBAr   (dataModel, op, ba         )) with CardSeq"
      res += s"$bar2[$uA___](addBArOpt(dataModel, op, ba         ))"
    }
    if (hasMap) {
      res += s"""$map1[$tA___](addMap   (dataModel, op, map        )) with CardMap"""
      res += s"""$map2[$tT___](addMapKs (dataModel, op, keys       )) with CardMap"""
      res += s"""$map3[$tA___](addMapVs (dataModel, op, vs         )) with CardMap"""
      res += s"""$map4[$tA___](addMapOpt(dataModel, op, map        )) with CardMap"""
      res += s"""$map5[$tO___](addMapKs (dataModel, op, Seq(key)   )) with CardMap"""
    }
    if (hasOne) {
      res += s"$sort[$tA___](addSort  (dataModel, sort           ))"
    }
  }

  private val hasRefOne  = refs.exists(_.cardinality == CardOne)
  private val hasRefMany = refs.exists(_.cardinality == CardSet)
  refs.collect {
    case MetaAttribute(attr, card, _, _, Some(ref0), _, options, _, _, _, _, _) =>
      val refName        = camel(attr)
      val pRefAttr       = padRefAttr(attr)
      val pRef           = padRefEntity(ref0)
      val nsIndex        = entityList.indexOf(entity)
      val refAttrIndex   = attrList.indexOf(entity + "." + attr)
      val refEntityIndex = entityList.indexOf(ref0)
      val isOwner        = options.contains("owner")
      val owner          = s"$isOwner" + (if (isOwner) " " else "") // align true/false
      val coord          = s"List($nsIndex, $refAttrIndex, $refEntityIndex)"
      val refObj         = s"""_dm.Ref("$entity", "$attr"$pRefAttr, "$ref0"$pRef, $card, $owner, $coord)"""
      if (arity == maxArity) {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj))"""
      } else if (card == CardOne) {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj)) with OptRefInit"""
      } else {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj)) with NestedInit"""
      }
  }

  private val manAttrs = if (last) "" else man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs = if (last) "" else opt.result().mkString("", "\n  ", "\n\n  ")
  private val tacAttrs = tac.result().mkString("\n  ")

  private val dataModel = "override val dataModel: DataModel"

  private val lastEntity = if (last) s"X${arity + 2}" else ent_1
  private val modelOps   = s"ModelOps_$arity[${`A..V, `}t, $ent_0, $lastEntity]"

  private val resolvers = res.result().mkString("\n  ")


  private val filterAttrs = {
    if (first) {
      s"""
         |
         |  override protected def _attrTac[   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2]) = new $ent_0[   t](filterAttr(dataModel, op, a))
         |  override protected def _attrMan[X, ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[X, t, ns1, ns2]) = new $ent_1[X, t](filterAttr(dataModel, op, a))""".stripMargin
    } else if (last) {
      s"""
         |
         |  override protected def _attrSortTac[ns1[_], ns2[_, _]](op: Op, a: ModelOps_0[t, ns1, ns2] & CardOne) = new $ent_0[${`A..V`}, t](filterAttr(dataModel, op, a)) with SortAttrs${_0}[${`A..V`}, t, $ent_0]
         |  override protected def _attrTac    [ns1[_], ns2[_, _]](op: Op, a: ModelOps_0[t, ns1, ns2]          ) = new $ent_0[${`A..V`}, t](filterAttr(dataModel, op, a))""".stripMargin
    } else {
      s"""
         |
         |  override protected def _attrSortTac[   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2] & CardOne) = new $ent_0[${`A..V`},    t](filterAttr(dataModel, op, a)) with SortAttrs${_0}[${`A..V`},    t, $ent_0]
         |  override protected def _attrSortMan[   ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[$V, t, ns1, ns2]          ) = new $ent_1[${`A..V`}, $V, t](filterAttr(dataModel, op, a)) with SortAttrs${_1}[${`A..V`}, $V, t, $ent_1]
         |  override protected def _attrTac    [   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2]          ) = new $ent_0[${`A..V`},    t](filterAttr(dataModel, op, a))
         |  override protected def _attrMan    [X, ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[X, t, ns1, ns2]          ) = new $ent_1[${`A..V`}, X, t](filterAttr(dataModel, op, a))""".stripMargin
    }
  }

  private val optRefInit = if (hasRefOne && arity < maxArity) {
    val ns_refs = ent_1 + "_refs"
    s"""trait OptRefInit extends OptRefOp_$arity[${`A..V, `}$ns_refs] with OptRef_$arity[${`A..V, `}$ns_refs] { self: Molecule =>
       |    override protected def _optRef[RefTpl](optRefDataModel: DataModel): $ns_refs[${`A..V, `}Option[RefTpl], Any] =
       |      new $ns_refs[${`A..V, `}Option[RefTpl], Any](DataModel(
       |        self.dataModel.elements.init :+ OptRef(self.dataModel.elements.last.asInstanceOf[Ref], optRefDataModel.elements),
       |        self.dataModel.attrIndexes ++ optRefDataModel.attrIndexes,
       |        binds = self.dataModel.binds + optRefDataModel.binds
       |      ))
       |  }""".stripMargin
  } else ""

  private val nestedInit = if (hasRefMany && arity < maxArity) {
    s"""trait NestedInit extends NestedOp_$arity${`[A..V]`} with Nested_$arity${`[A..V]`} { self: Molecule =>
       |    override protected def _nestedMan[NestedTpl](nestedDataModel: DataModel): NestedInit_$n0[${`A..V, `}NestedTpl] =
       |      new NestedInit_$n0(DataModel(
       |        self.dataModel.elements.init :+ Nested(self.dataModel.elements.last.asInstanceOf[Ref], nestedDataModel.elements),
       |        self.dataModel.attrIndexes ++ nestedDataModel.attrIndexes,
       |        binds = self.dataModel.binds + nestedDataModel.binds
       |      ))
       |
       |    override protected def _nestedOpt[NestedTpl](nestedDataModel: DataModel): NestedInit_$n0[${`A..V, `}NestedTpl] =
       |      new NestedInit_$n0(DataModel(
       |        self.dataModel.elements.init :+ OptNested(self.dataModel.elements.last.asInstanceOf[Ref], nestedDataModel.elements),
       |        self.dataModel.attrIndexes ++ nestedDataModel.attrIndexes,
       |        binds = self.dataModel.binds + nestedDataModel.binds
       |      ))
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
        Some(s"""object _$backRef$pad extends $backRef${_0}$pad[${`A..V, `}t](dataModel.add(_dm.BackRef("$backRef", "$entity", $coord)))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }

  private val (refClass, refClassBody) = if (refResult.isEmpty && backRefs.isEmpty) ("", "") else {
    val refHandles = List(optRefInit, nestedInit, refDefs, backRefDefs).map(_.trim).filterNot(_.isEmpty).mkString("\n\n  ")
    (
      s"${ent_0}_refs[${`A..V, `}t](dataModel) with ",
      s"""
         |
         |class ${ent_0}_refs[${`A..V, `}t]($dataModel) extends Molecule_$n0${`[A..V]`} {
         |  $refHandles
         |}""".stripMargin
    )
  }

  def get: String =
    s"""class $ent_0[${`A..V, `}t]($dataModel) extends $refClass${entity}_base with $modelOps {
       |  $manAttrs$optAttrs$tacAttrs
       |
       |  $resolvers$filterAttrs
       |}$refClassBody
       |""".stripMargin
}
