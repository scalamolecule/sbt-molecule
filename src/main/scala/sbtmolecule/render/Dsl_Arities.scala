package sbtmolecule.render

import molecule.base.ast.*


case class Dsl_Arities(
  schema: MetaSchema,
  partPrefix: String,
  nsList: Seq[String],
  attrList: Seq[String],
  namespace: MetaNs,
  arity: Int
) extends DslFormatting(schema, namespace, arity) {

  private val man = List.newBuilder[String]
  private val opt = List.newBuilder[String]
  private val tac = List.newBuilder[String]
  private val res = List.newBuilder[String]
  private val ref = List.newBuilder[String]

  private val first      = arity == 0
  private val last       = arity == schema.maxArity
  private val secondLast = arity == schema.maxArity - 1

  private var hasOne       = false
  private var hasSet       = false
  private var hasSeq       = false
  private var hasMap       = false
  private var hasByteArray = false
  private var seqCount     = 0

  private val (pMax, ppMax, ptMax) = {
    val (a, b, c) = attrs.map(a => a.card match {
      case _: CardOne =>
        hasOne = true
        (0, 4, getTpe(a.baseTpe).length) // Account for "ID" type being String
      case _: CardSet =>
        hasSet = true
        (5, 9, 5 + getTpe(a.baseTpe).length) // Account for "ID" type being String
      case _: CardSeq =>
        hasSeq = true
        if (a.baseTpe == "Byte") {
          hasByteArray = true
          (7, 6, 7 + 4) // "Byte".length
        } else {
          seqCount += 1
          (5, 9, 5 + a.baseTpe.length)
        }
      case _: CardMap =>
        hasMap = true
        (8, 9, 13 + a.baseTpe.length)
    }).unzip3
    (a.max, b.max, c.max)
  }

  private val pOne = " " * pMax
  private val pSet = " " * (pMax - 5)
  private val pSeq = " " * (pMax - 5)
  private val pBar = " " * (pMax - 7)
  private val pMap = " " * (pMax - 13)

  private val ppAgr = " " * ppMax
  private val ppOne = " " * (ppMax - 4)
  private val ppSet = " " * (ppMax - 9)
  private val ppSeq = " " * (ppMax - 9)
  private val ppBar = " " * (ppMax - 6)
  private val ppMap = " " * (ppMax - 6)

  private val ptOne       = (t: String) => " " * (ptMax - t.length)
  private val ptSet       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptSeq       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptMap       = (t: String) => " " * (ptMax - 13 - t.length)
  private val ptByteArray = " " * (ptMax - 7 - 4)

  //  println("--------------")
  //  attrs.foreach(println)

  attrs.foreach {
    case MetaAttr(attr, card, baseType, refNs, _, _, _, _, _, _) =>
      val isByteArray = card == CardSeq && baseType == "Byte"
      val c           = if (isByteArray) "BAr" else card._marker
      val tpe         = getTpe(baseType)

      val padA = padAttr(attr)
      val pad1 = padType(tpe)
      val pad2 = " " * ("Option[]".length + ptMax)

      val (tM, tO) = card match {
        case _: CardOne                       => (tpe + ptOne(tpe), s"Option[$tpe]" + ptOne(tpe))
        case _: CardSet                       => (s"Set[$tpe]" + ptSet(tpe), s"Option[Set[$tpe]]" + ptSet(tpe))
        case _: CardSeq if baseType == "Byte" => (s"Array[$tpe]" + ptByteArray, s"Option[Array[$tpe]]" + ptByteArray)
        case _: CardSeq                       => (s"Seq[$tpe]" + ptSeq(tpe), s"Option[Seq[$tpe]]" + ptSeq(tpe))
        case _: CardMap                       => (s"Map[String, $tpe]" + ptMap(tpe), s"Option[Map[String, $tpe]]" + ptMap(tpe))
      }

      lazy val tpesM = s"${`A..V, `}$tM        , $tpe$pad1"
      lazy val tpesO = s"${`A..V, `}$tO, $tpe$pad1"
      lazy val tpesT = if (arity == 0)
        s"${`A..V`} $pad2 $tpe$pad1"
      else if (arity == schema.maxArity)
        s"${`A..V`}, $tpe$pad1"
      else
        s"${`A..V`}  $pad2, $tpe$pad1"

      lazy val elemsM = s"elements :+ ${attr}_man$padA"
      lazy val elemsO = s"elements :+ ${attr}_opt$padA"
      lazy val elemsT = s"elements :+ ${attr}_tac$padA"

      val nextNextNs = if (secondLast) s"X${arity + 3}" else ns_2
      val nextNs     = if (last) s"X${arity + 2}" else ns_1

      val filters = if (card == CardOne && attr != "id" && refNs.isEmpty) baseType match {
        case "String" => "_String "
        case "Int"    => "_Number "
        case "Long"   => "_Number "
        case "BigInt" => "_Number "
        case "Byte"   => "_Number "
        case "Short"  => "_Number "
        case _        => "        "
      } else "        "

      lazy val exprM = s"Expr${c}Man${_1}$filters[$tpesM, $ns_1, $nextNextNs]"
      lazy val exprO = s"Expr${c}Opt${_1}        [$tpesO, $ns_1, $nextNextNs]"
      lazy val exprT = s"Expr${c}Tac${_0}$filters[$tpesT, $ns_0, $nextNs]"

      if (!last) {
        man += s"""lazy val $attr  $padA = new $ns_1[$tpesM]($elemsM) with $exprM with $card"""
        if (attr != "id")
          opt += s"""lazy val ${attr}_?$padA = new $ns_1[$tpesO]($elemsO) with $exprO with $card"""
      }
      tac += s"""lazy val ${attr}_ $padA = new $ns_0[$tpesT]($elemsT) with $exprT with $card"""
  }

  if (first) {
    val pArg = if (hasMap) "  " else ""
    if (hasOne) {
      res += s"""override protected def _exprOneTac(op: Op, vs$pArg: Seq[t]$pOne) = new $ns_0[t](addOne(elements, op, vs)) with CardOne"""
    }
    if (hasSet) {
      res += s"""override protected def _exprSet   (op: Op, vs$pArg: Seq[Set[t]]$pSet) = new $ns_0[t](addSet(elements, op, vs)) with CardSet"""
    }
    if (hasSeq && seqCount > 0) {
      res += s"""override protected def _exprSeq   (op: Op, vs$pArg: Seq[Seq[t]]$pSeq) = new $ns_0[t](addSeq(elements, op, vs)) with CardSet"""
    }
    if (hasByteArray) {
      res += s"""override protected def _exprBAr   (op: Op, ba$pArg: Seq[Array[t]]$pBar) = new $ns_0[t](addBAr(elements, op, ba)) with CardSet"""
    }
    if (hasMap) {
      res += s"""override protected def _exprMap   (op: Op, map : Map[String, t]$pMap) = new $ns_0[t](addMap(elements, op, map          )) with CardMap"""
      res += s"""override protected def _exprMapK  (op: Op, keys: Seq[String]   $pMap) = new $ns_0[t](addMap(elements, op, mapK[t](keys))) with CardMap"""
      res += s"""override protected def _exprMapV  (op: Op, vs  : Seq[t]        $pMap) = new $ns_0[t](addMap(elements, op, mapV[t](vs)  )) with CardMap"""
    }

  } else {
    val tMap = if(hasMap) "   " else ""

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

    def agg1 = s"override protected def _aggrInt   (kw: Kw                $ppAgr) = new $ns_0"
    def agg2 = s"override protected def _aggrT     (kw: Kw                $ppAgr) = new $ns_0"
    def agg3 = s"override protected def _aggrDouble(kw: Kw                $ppAgr) = new $ns_0"
    def agg4 = s"override protected def _aggrSet   (kw: Kw, n: Option[Int]$ppAgr) = new $ns_0"
    def agg5 = s"override protected def _aggrDist  (kw: Kw                $ppAgr) = new $ns_0"

    def one1 = s"override protected def _exprOneMan(op: Op, vs: Seq[t]        $ppOne) = new $ns_0"
    def one2 = s"override protected def _exprOneOpt(op: Op, vs: Option[Seq[t]]$ppOne) = new $ns_0"
    def one3 = s"override protected def _exprOneTac(op: Op, vs: Seq[t]        $ppOne) = new $ns_0"

    def set1 = s"override protected def _exprSet   (op: Op, vs: Seq[Set[t]]        $ppSet) = new $ns_0"
    def set2 = s"override protected def _exprSetOpt(op: Op, vs: Option[Seq[Set[t]]]$ppSet) = new $ns_0"

    def seq1 = s"override protected def _exprSeq   (op: Op, vs: Seq[Seq[t]]        $ppSeq) = new $ns_0"
    def seq2 = s"override protected def _exprSeqOpt(op: Op, vs: Option[Seq[Seq[t]]]$ppSeq) = new $ns_0"

    def bar1 = s"override protected def _exprBAr   (op: Op, ba: Seq[Array[t]]   $ppBar) = new $ns_0"
    def bar2 = s"override protected def _exprBArOpt(op: Op, ba: Option[Array[t]]$ppBar) = new $ns_0"

    def map1 = s"override protected def _exprMap   (op: Op, map : Map[String, t]$ppMap) = new $ns_0"
    def map2 = s"override protected def _exprMapK  (op: Op, keys: Seq[String]   $ppMap) = new $ns_0"
    def map3 = s"override protected def _exprMapV  (op: Op, vs  : Seq[t]        $ppMap) = new $ns_0"
    def map4 = s"override protected def _exprMapOpt(op: Op, key : String        $ppMap) = new $ns_0"

    def sort = s"override protected def _sort      (sort: String              $ppOne) = new $ns_0"


    if (hasOne || hasSet) {
      res += s"$agg1[$tInt_](toInt    (elements, kw    )) with SortAttrs_$arity[$ttInt_, $ns_0]"
      res += s"$agg2[$tT___](asIs     (elements, kw    )) with SortAttrs_$arity[$ttT___, $ns_0]"
      res += s"$agg3[$tDoub](toDouble (elements, kw    )) with SortAttrs_$arity[$ttDoub, $ns_0]"
      res += s"$agg4[$tSet_](asIs     (elements, kw, n ))"
      res += s"$agg5[$tDist](asIs     (elements, kw    ))"
    }
    if (hasOne) {
      res += s"$one1[$tA___](addOne   (elements, op, vs)) with SortAttrs_$arity[$ttA___, $ns_0] with CardOne"
      res += s"$one2[$tA___](addOneOpt(elements, op, vs)) with SortAttrs_$arity[$ttA___, $ns_0]"
      res += s"$one3[$tA___](addOne   (elements, op, vs)) with CardOne"
    }
    if (hasSet) {
      res += s"$set1[$tA___](addSet   (elements, op, vs)) with CardSet"
      res += s"$set2[$tA___](addSetOpt(elements, op, vs))"
    }
    if (hasSeq && seqCount > 0) {
      res += s"$seq1[$uA___](addSeq   (elements, op, vs)) with CardSeq"
      res += s"$seq2[$uA___](addSeqOpt(elements, op, vs))"
    }
    if (hasByteArray) {
      res += s"$bar1[$uA___](addBAr   (elements, op, ba)) with CardSeq"
      res += s"$bar2[$uA___](addBArOpt(elements, op, ba))"
    }
    if (hasMap) {
      res += s"""$map1[$tA___](addMap   (elements, op, map              )) with CardMap"""
      res += s"""$map2[$tT___](addMap   (elements, op, mapK[t](keys)    )) with CardMap"""
      res += s"""$map3[$tA___](addMap   (elements, op, mapV[t](vs)      )) with CardMap"""
      res += s"""$map4[$tO___](addMap   (elements, op, mapK[t](Seq(key)))) with CardMap"""
    }
    if (hasOne) {
      res += s"$sort[$tA___](addSort  (elements, sort))"
    }
  }

  private val hasCardSet     = refs.exists(_.card == CardSet)
  private val withNestedInit = s" with NestedInit"
  private val nestedPad      = " " * withNestedInit.length
  refs.collect {
    case MetaAttr(attr, card, _, Some(refNs), options, _, _, _, _, _) =>
      val refName      = camel(attr)
      val pRefAttr     = padRefAttr(attr)
      val pRefNs       = padRefNs(refNs)
      val nsIndex      = nsList.indexOf(ns)
      val refAttrIndex = attrList.indexOf(ns + "." + attr)
      val refNsIndex   = nsList.indexOf(refNs)
      val isOwner      = options.contains("owner")
      val owner        = s"$isOwner" + (if (isOwner) " " else "") // align true/false
      val coord        = s"Seq($nsIndex, $refAttrIndex, $refNsIndex)"
      val refObj       = s"""Model.Ref("$ns", "$attr"$pRefAttr, "$refNs"$pRefNs, $card, $owner, $coord)"""

      val nested = if (hasCardSet) {
        if (arity < maxArity)
          if (card == CardOne) nestedPad else withNestedInit
        else
          ""
      } else ""
      ref += s"""object $refName$pRefAttr extends $refNs${_0}$pRefNs[${`A..V, `}t](elements :+ $refObj)$nested"""
  }

  private val manAttrs = if (last) "" else man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs = if (last) "" else opt.result().mkString("", "\n  ", "\n\n  ")
  private val tacAttrs = tac.result().mkString("\n  ")

  private val elements = "override val elements: List[Element]"

  private val lastNs   = if (last) s"X${arity + 2}" else ns_1
  private val modelOps = s"ModelOps_$arity[${`A..V, `}t, $ns_0, $lastNs]"

  private val resolvers = res.result().mkString("\n  ")

  private val filterAttrs = if (first) {
    s"""
       |
       |  override protected def _attrTac[   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2]) = new $ns_0[   t](filterAttr(elements, op, a))
       |  override protected def _attrMan[X, ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[X, t, ns1, ns2]) = new $ns_1[X, t](filterAttr(elements, op, a))""".stripMargin
  } else if (last) {
    s"""
       |
       |  override protected def _attrSortTac[ns1[_], ns2[_, _]](op: Op, a: ModelOps_0[t, ns1, ns2] with CardOne) = new $ns_0[${`A..V`}, t](filterAttr(elements, op, a)) with SortAttrs${_0}[${`A..V`}, t, $ns_0]
       |  override protected def _attrTac    [ns1[_], ns2[_, _]](op: Op, a: ModelOps_0[t, ns1, ns2]             ) = new $ns_0[${`A..V`}, t](filterAttr(elements, op, a))""".stripMargin
  } else {
    s"""
       |
       |  override protected def _attrSortTac[   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2] with CardOne) = new $ns_0[${`A..V`},    t](filterAttr(elements, op, a)) with SortAttrs${_0}[${`A..V`},    t, $ns_0]
       |  override protected def _attrSortMan[   ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[$V, t, ns1, ns2]             ) = new $ns_1[${`A..V`}, $V, t](filterAttr(elements, op, a)) with SortAttrs${_1}[${`A..V`}, $V, t, $ns_1]
       |  override protected def _attrTac    [   ns1[_]   , ns2[_, _]   ](op: Op, a: ModelOps_0[   t, ns1, ns2]             ) = new $ns_0[${`A..V`},    t](filterAttr(elements, op, a))
       |  override protected def _attrMan    [X, ns1[_, _], ns2[_, _, _]](op: Op, a: ModelOps_1[X, t, ns1, ns2]             ) = new $ns_1[${`A..V`}, X, t](filterAttr(elements, op, a))""".stripMargin
  }

  private val nested = if (hasCardSet && arity < maxArity) {
    s"""
       |
       |  trait NestedInit extends NestedOp_$arity${`[A..V]`} with Nested_$arity${`[A..V]`} { self: Molecule =>
       |    override protected def _nestedMan[NestedTpl](nestedElements: List[Element]): NestedInit_$n0[${`A..V, `}NestedTpl] = new NestedInit_$n0(self.elements.init :+ Nested   (self.elements.last.asInstanceOf[Ref], nestedElements))
       |    override protected def _nestedOpt[NestedTpl](nestedElements: List[Element]): NestedInit_$n0[${`A..V, `}NestedTpl] = new NestedInit_$n0(self.elements.init :+ NestedOpt(self.elements.last.asInstanceOf[Ref], nestedElements))
       |  }""".stripMargin
  } else ""

  private val refResult = ref.result()
  private val refDefs   = if (refResult.isEmpty) "" else refResult.mkString("\n\n  ", "\n  ", "")

  private val backRefDefs = if (backRefs.isEmpty) "" else {
    val max = backRefs.map(_.length).max
    backRefs.flatMap { backRef =>
      if (ns == backRef) None else {
        val pad         = padS(max, backRef)
        val prevNsIndex = nsList.indexOf(backRef)
        val curNsIndex  = nsList.indexOf(ns)
        val coord       = s"Seq($prevNsIndex, $curNsIndex)"
        Some(s"""object _$backRef$pad extends $backRef${_0}$pad[${`A..V, `}t](elements :+ Model.BackRef("$backRef", "$ns", $coord))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }

  def get: String =
    s"""class $ns_0[${`A..V, `}t]($elements) extends ${ns}_base with $modelOps {
       |  $manAttrs$optAttrs$tacAttrs
       |
       |  $resolvers$filterAttrs$nested$refDefs$backRefDefs
       |}
       |""".stripMargin
}
