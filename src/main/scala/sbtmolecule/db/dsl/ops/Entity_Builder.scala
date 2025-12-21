package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.Formatting

case class Entity_Builder(
  metaDomain: MetaDomain,
  entityList: Seq[String],
  attrList: Seq[String],
  metaEntity: MetaEntity,
  arity: Int,
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

  private val maxLength = {
    attributes.map(a => a.value match {
      case _: OneValue =>
        hasOne = true
        getTpe(a.baseTpe).length // Account for "ID" type being String
      case _: SetValue =>
        hasSet = true
        s"Set[${getTpe(a.baseTpe)}]".length
      case _: SeqValue =>
        hasSeq = true
        if (a.baseTpe == "Byte") {
          hasByteArray = true
          "Set[Byte]".length
        } else {
          seqCount += 1
          s"Seq[${a.baseTpe}]".length
        }
      case _: MapValue =>
        hasMap = true
        s"Map[String, ${a.baseTpe}]".length
    }).max
  }

  private val pOne       = (t: String) => " " * (maxLength - t.length)
  private val pSet       = (t: String) => " " * (maxLength - 5 - t.length)
  private val pSeq       = (t: String) => " " * (maxLength - 5 - t.length)
  private val pMap       = (t: String) => " " * (maxLength - 13 - t.length)
  private val pByteArray = (t: String) => " " * (maxLength - 7 - t.length)

  attributes.foreach {
    case MetaAttribute(attr, value, baseType0, _, optRef, _, _, optEnum, _, optAlias, _, _, _, _, _, _, _, _, _, _) =>
      val cleanAttr   = firstLow(optAlias.getOrElse(attr))
      val isByteArray = value == SeqValue && baseType0 == "Byte"
      val c           = if (isByteArray) "BAr" else value._marker
      val baseType    = getTpe(baseType0)
      val baseType1   = optEnum.getOrElse(baseType)
      val isEnum      = optEnum.isDefined
      val padA        = padAttr(cleanAttr)
      val pad1        = padType1(baseType1)
      val pad2        = value match {
        case _: OneValue                       => baseType + pOne(baseType)
        case _: SetValue                       => s"Set[$baseType]" + pSet(baseType)
        case _: SeqValue if baseType == "Byte" => s"Array[Byte]" + pByteArray("Byte")
        case _: SeqValue                       => s"Seq[$baseType]" + pSeq(baseType)
        case _: MapValue                       => s"Map[String, $baseType]" + pMap(baseType)
      }
      val pad3        = " " * maxLength

      val (tpesM, tpesO, tpesT) = {
        arity match {
          case 0 =>
            (
              s"$baseType1$pad1",
              s"$baseType1$pad1",
              s"$baseType1$pad1"
            )
          case 1 =>
            (
              s"$baseType1$pad1, (T, $pad2        )",
              s"$baseType1$pad1, (T, Option[$pad2])",
              s"T, $baseType1$pad1          $pad3  "
            )
          case _ =>
            (
              s"$baseType1$pad1, Tpl :* $pad2        ",
              s"$baseType1$pad1, Tpl :* Option[$pad2]",
              s"$baseType1$pad1, Tpl           $pad3 "
            )
        }
      }

      lazy val elemsM = s"dataModel.add(${cleanAttr}_man$padA)"
      lazy val elemsO = s"dataModel.add(${cleanAttr}_opt$padA)"
      lazy val elemsT = s"dataModel.add(${cleanAttr}_tac$padA)"

      val subType = if (value == OneValue && attr != "id" && optRef.isEmpty) baseType match {
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

      val filtersMan = subType
      val filtersOpt = if (isEnum) "_Enum   " else "        "
      val filtersTac = subType

      lazy val exprM = s"Expr${c}Man$filtersMan[$tpesM]"
      lazy val exprO = s"Expr${c}Opt$filtersOpt[$tpesO]"
      lazy val exprT = s"Expr${c}Tac$filtersTac[$tpesT]"

      man += s"""final lazy val $cleanAttr  $padA = ${entB}_$exprM($elemsM)"""
      if (attr != "id")
        opt += s"""final lazy val ${cleanAttr}_?$padA = ${entB}_$exprO($elemsO)"""
      tac += s"""final lazy val ${cleanAttr}_ $padA = ${entA}_$exprT($elemsT)"""
  }


  private val hasRefOne     = refs.exists(_.value == OneValue)
  private val hasRefMany    = refs.exists(_.value == SetValue)
  private val hasRef        = hasRefOne || hasRefMany
  private val hasManyToMany = refs.exists(_.relationship.get == ManyToMany)

  refs.collect {
    case MetaAttribute(attr, value, _, _, Some(ref0), Some(reverseRef), Some(relationship), _, options, optAlias, _, _, _, _, _, _, _, _, _, _) =>
      val cleanAttr      = optAlias.getOrElse(attr)
      val refName        = if (cleanAttr.contains("_")) cleanAttr else camel(cleanAttr)
      val pRefAttr       = padRefAttr(cleanAttr)
      val ref_X          = ref0 + cur
      val nsIndex        = entityList.indexOf(entity)
      val refAttrIndex   = attrList.indexOf(entity + "." + attr)
      val refEntityIndex = entityList.indexOf(ref0)
      val pRef0          = padRefEntity(ref0)
      val coord          = s"List($nsIndex, $refAttrIndex, $refEntityIndex)"
      val tpl            = arity match {
        case 0 => ""
        case 1 => "[T]"
        case _ => "[Tpl]"
      }
      if (relationship == ManyToMany) {
        val List(ent, revRef, joinTable, attr, refAttr2, targetRef) = reverseRef.split('-').toList
        val dummyCoord                                              = "List(0, 0, 0)"
        ref +=
          s"""object $refName$pRefAttr extends $ref_X$pRef0$tpl(dataModel
             |    .add(_dm.Ref("$ent", "$revRef", "$joinTable", OneToMany, $dummyCoord, Some("$attr")))
             |    .add(_dm.Ref("$joinTable", "$refAttr2", "$targetRef", ManyToOne, $dummyCoord, Some("$revRef")))
             |  ) with NestedInitJoin""".stripMargin
      } else {
        val pRef1  = padRefEntity(ref0)
        val owner  = if (options.contains("owned")) ", true" else ""
        val refObj = s"""_dm.Ref("$entity", "$attr"$pRefAttr, "$ref0"$pRef1, $relationship , $coord, Some("$reverseRef")$owner)"""
        if (value == OneValue) {
          ref += s"""object $refName$pRefAttr extends $ref_X$pRef0$tpl(dataModel.add($refObj)) with OptRefInit"""
        } else {
          ref += s"""object $refName$pRefAttr extends $ref_X$pRef0$tpl(dataModel.add($refObj)) with NestedInit"""
        }
      }

    case other => throw new Exception("Unexpected ref definition: " + other)
  }

  private val manAttrs = man.result().mkString("", "\n  ", "\n\n  ")
  private val optAttrs = opt.result().mkString("", "\n  ", "\n\n  ")
  private val tacAttrs = tac.result().mkString("\n  ")

  private val optRefInit = if (hasRef) {
    val (t1, t2) = arity match {
      case 0 => ("Option[OptRefT  ]", "Option[OptRefTpl]")
      case 1 => ("(T, Option[OptRefT  ])", "(T, Option[OptRefTpl])")
      case _ => ("Tpl :* Option[OptRefT  ]", "Tpl :* Option[OptRefTpl]")
    }
    s"""trait OptRefInit { self: Molecule =>
       |    def ?[OptRefT           ](optRef: Molecule_1[OptRefT  ]) = new $entB[$t1](addOptRef(self, optRef))
       |    def ?[OptRefTpl <: Tuple](optRef: Molecule_n[OptRefTpl]) = new $entB[$t2](addOptRef(self, optRef))
       |  }""".stripMargin
  } else ""

  private val nestedInit = if (hasRefMany) {
    val (t1, t2)       = arity match {
      case 0 => ("Seq[NestedT]", "Seq[NestedTpl]")
      case 1 => ("(T, Seq[NestedT])", "(T, Seq[NestedTpl])")
      case _ => ("Tpl :* Seq[NestedT]", "Tpl :* Seq[NestedTpl]")
    }
    val nestedInitJoin = if (hasManyToMany)
      s"""
         |
         |  trait NestedInitJoin extends OptRefInit { self: Molecule =>
         |    def ** [NestedT](nested: Molecule_1[NestedT]) = new $entB[$t1](addNestedJoin(self, nested))
         |    def **?[NestedT](nested: Molecule_1[NestedT]) = new $entB[$t1](addOptNestedJoin(self, nested))
         |
         |    def ** [NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[$t2](addNestedJoin(self, nested))
         |    def **?[NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[$t2](addOptNestedJoin(self, nested))
         |  }""".stripMargin else ""

    s"""trait NestedInit extends OptRefInit { self: Molecule =>
       |    def * [NestedT](nested: Molecule_1[NestedT]) = new $entB[$t1](addNested(self, nested))
       |    def *?[NestedT](nested: Molecule_1[NestedT]) = new $entB[$t1](addOptNested(self, nested))
       |
       |    def * [NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[$t2](addNested(self, nested))
       |    def *?[NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new $entB[$t2](addOptNested(self, nested))
       |  }$nestedInitJoin""".stripMargin
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
        val tpl             = arity match {
          case 0 => ""
          case 1 => "[T]"
          case _ => "[Tpl]"
        }
        Some(s"""object _$backRef$pad extends $backRef_X$pad$tpl(dataModel.add(_dm.BackRef("$backRef", "$entity", $coord)))""")
      }
    }.mkString("\n\n  ", "\n  ", "")
  }

  private val (n, t1, t2, t3) = arity match {
    case 0 => ("0", "", "", "")
    case 1 => ("1", "[T]", "[T]", "[T]")
    case _ => ("n", "[Tpl]", "[Tpl <: Tuple]", "[Tpl]")
  }

  private val (refClass, refClassBody) = if (refResult.isEmpty && backRefs.isEmpty) {
    (s"Molecule_$n$t3 with ${entity}_attrs", "")
  } else {
    val refHandles = List(optRefInit, nestedInit, refDefs, backRefDefs).map(_.trim).filterNot(_.isEmpty).mkString("\n\n  ")
    (
      s"$entity_refs_cur$t1(dataModel) with ${entity}_attrs",
      s"""
         |
         |class $entity_refs_cur$t2(override val dataModel: DataModel) extends Molecule_$n$t3 {
         |  $refHandles
         |}""".stripMargin
    )
  }

  def get: String =
    s"""class ${entity}_$n$t2(override val dataModel: DataModel) extends $refClass {
       |  $manAttrs$optAttrs$tacAttrs
       |}$refClassBody
       |""".stripMargin
}
