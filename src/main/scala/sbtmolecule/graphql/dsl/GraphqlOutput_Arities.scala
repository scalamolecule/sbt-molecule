package sbtmolecule.graphql.dsl

import molecule.core.model.*
import sbtmolecule.DslFormatting


case class GraphqlOutput_Arities(
  dbModel: DbModel,
  entityList: Seq[String],
  attrList: Seq[String],
  dbEntity: DbEntity,
  arity: Int
) extends DslFormatting(dbModel, dbEntity, arity) {

  private val man = List.newBuilder[String]
  private val opt = List.newBuilder[String]
  private val ref = List.newBuilder[String]

  private val last         = arity == dbModel.maxArity
  private var hasOne       = false
  private var hasSeq       = false
  private var hasByteArray = false
  private var seqCount     = 0

  private val ptMax = {
    attrs.collect {
      case DbAttribute(_, CardOne, baseTpe, _, _, _, _, _, _, _) =>
        hasOne = true
        getTpe(baseTpe).length // Account for "ID" type being String

      case DbAttribute(_, CardSeq, "Byte", _, _, _, _, _, _, _) =>
        hasSeq = true
        hasByteArray = true
        7 + 4 // "Byte".length

      case DbAttribute(_, CardSeq, baseTpe, _, _, _, _, _, _, _) =>
        hasSeq = true
        seqCount += 1
        5 + baseTpe.length
    }.max
  }

  private val ptOne       = (t: String) => " " * (ptMax - t.length)
  private val ptSet       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptSeq       = (t: String) => " " * (ptMax - 5 - t.length)
  private val ptMap       = (t: String) => " " * (ptMax - 13 - t.length)
  private val ptByteArray = " " * (ptMax - 7 - 4)

  attrs.foreach {
    case DbAttribute(attr, card, baseType, ref, _, _, _, _, _, _) =>
      val tpe  = getTpe(baseType)
      val padA = padAttr(attr)
      val pad1 = padType(tpe)

      val (tM, tO) = card match {
        case _: CardOne                       => (tpe + ptOne(tpe), s"Option[$tpe]" + ptOne(tpe))
        case _: CardSet                       => (s"Set[$tpe]" + ptSet(tpe), s"Option[Set[$tpe]]" + ptSet(tpe))
        case _: CardSeq if baseType == "Byte" => (s"Array[$tpe]" + ptByteArray, s"Option[Array[$tpe]]" + ptByteArray)
        case _: CardSeq                       => (s"Seq[$tpe]" + ptSeq(tpe), s"Option[Seq[$tpe]]" + ptSeq(tpe))
        case _: CardMap                       => (s"Map[String, $tpe]" + ptMap(tpe), s"Option[Map[String, $tpe]]" + ptMap(tpe))
      }

      lazy val tpesM = s"${`A..V, `}$tM        , $tpe$pad1"
      lazy val tpesO = s"${`A..V, `}$tO, $tpe$pad1"

      lazy val elemsM = s"dataModel.add(${attr}_man$padA)"
      lazy val elemsO = s"dataModel.add(${attr}_opt$padA)"

      if (!last && baseType.nonEmpty) {
        man += s"""lazy val $attr  $padA = new $ent_1[$tpesM]($elemsM) with $card"""
        if (attr != "id")
          opt += s"""lazy val ${attr}_?$padA = new $ent_1[$tpesO]($elemsO) with $card"""
      }
  }

  private val hasRefOne  = refs.exists(_.card == CardOne)
  private val hasRefMany = refs.exists(_.card == CardSet)
  refs.collect {
    case DbAttribute(attr, card, _, Some(ref0), _, _, _, _, _, _) =>
      val refName  = camel(attr)
      val pRefAttr = padRefAttr(attr)
      val pRef     = padRefEntity(ref0)
      val refObj   = s"""molecule.core.model.Ref("$ent", "$attr"$pRefAttr, "$ref0"$pRef, $card)"""
      if (arity == maxArity) {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj))"""
      } else if (card == CardOne) {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj)) with OptRefInit"""
      } else {
        ref += s"""object $refName$pRefAttr extends $ref0${_0}$pRef[${`A..V, `}t](dataModel.add($refObj)) with NestedInit"""
      }
  }

  private val manAttrs  = if (last) "" else man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs  = if (last) "" else opt.result().mkString("\n  ")
  private val dataModel = "override val dataModel: DataModel"

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
      if (ent == backRef) None else {
        val pad = padS(max, backRef)
        Some(s"""object _$backRef$pad extends $backRef${_0}$pad[${`A..V, `}t](dataModel.add(molecule.core.model.BackRef("$backRef", "$ent")))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }

  private val (refClass, refClassBody) = if (refResult.isEmpty && backRefs.isEmpty) ("", "") else {
    val refHandles = List(optRefInit, nestedInit, refDefs, backRefDefs).map(_.trim).filterNot(_.isEmpty).mkString("\n\n  ")
    (
      s"${ent_0}_refs[${`A..V, `}t](dataModel) with ",
      s"""
         |
         |private[$domain] class ${ent_0}_refs[${`A..V, `}t]($dataModel) extends Molecule_$n0${`[A..V]`} {
         |  $refHandles
         |}""".stripMargin
    )
  }

  def get: String =
    s"""private[$domain] class $ent_0[${`A..V, `}t]($dataModel) extends $refClass${ent}_base {
       |  $manAttrs$optAttrs
       |}$refClassBody
       |""".stripMargin
}
