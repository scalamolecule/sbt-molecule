package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprOneMan(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasString: Boolean,
  hasEnum: Boolean,
  hasInteger: Boolean,
  hasDecimal: Boolean,
  hasBoolean: Boolean,
) extends Formatting(metaDomain, metaEntity, arity) {

  val base = s"${entA}_ExprOneMan"
  val tpe  = if (first) "T" else "Tpl, T"

  val string = if (hasString)
    s"""
       |class ${base}_String[Tpl <: Tuple, T](override val dataModel: DataModel) extends $base[$tpe](dataModel) {
       |  def startsWith(prefix: T)                = new $entA[$tpe](addOne(dataModel, StartsWith                  , Seq(prefix)            )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def endsWith  (suffix: T)                = new $entA[$tpe](addOne(dataModel, EndsWith                    , Seq(suffix)            )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def contains  (needle: T)                = new $entA[$tpe](addOne(dataModel, Contains                    , Seq(needle)            )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def matches   (regex : T)                = new $entA[$tpe](addOne(dataModel, Matches                     , Seq(regex)             )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def +         (str   : T)                = new $entA[$tpe](addOne(dataModel, AttrOp.Append               , Seq(str)               )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def prepend   (str   : T)                = new $entA[$tpe](addOne(dataModel, AttrOp.Prepend              , Seq(str)               )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def substring (start: Int, end: Int)     = new $entA[$tpe](addOne(dataModel, AttrOp.SubString(start, end), Nil                    )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def replaceAll(regex: T, replacement: T) = new $entA[$tpe](addOne(dataModel, AttrOp.ReplaceAll           , Seq(regex, replacement))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def toLower                              = new $entA[$tpe](addOne(dataModel, AttrOp.ToLower              , Nil                    )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def toUpper                              = new $entA[$tpe](addOne(dataModel, AttrOp.ToUpper              , Nil                    )) with ${entA}_Sorting[Tpl, T] with CardOne
       |
       |  def startsWith(prefix: qm) = new $entA[$tpe](addOne(dataModel, StartsWith, Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def endsWith  (suffix: qm) = new $entA[$tpe](addOne(dataModel, EndsWith  , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def contains  (needle: qm) = new $entA[$tpe](addOne(dataModel, Contains  , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def matches   (regex : qm) = new $entA[$tpe](addOne(dataModel, Matches   , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |}
       |""".stripMargin
  else ""

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe](dataModel) {
       |  def apply(             ) = new $entA[$tpe](addOne(dataModel, NoValue, Nil                                      )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def apply(v : T, vs: T*) = new $entA[$tpe](addOne(dataModel, Eq     , (v +: vs).map(_.toString.asInstanceOf[T]))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def apply(vs: Seq[T]   ) = new $entA[$tpe](addOne(dataModel, Eq     , vs       .map(_.toString.asInstanceOf[T]))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (v : T, vs: T*) = new $entA[$tpe](addOne(dataModel, Neq    , (v +: vs).map(_.toString.asInstanceOf[T]))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (vs: Seq[T]   ) = new $entA[$tpe](addOne(dataModel, Neq    , vs       .map(_.toString.asInstanceOf[T]))) with ${entA}_Sorting[Tpl, T] with CardOne
       |
       |  def apply(v : qm) = new $entA[$tpe](addOne(dataModel, Eq , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (v : qm) = new $entA[$tpe](addOne(dataModel, Neq, Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |}
       |""".stripMargin
  else ""

  val integer = if (hasInteger)
    s"""
       |class ${base}_Integer[Tpl <: Tuple, T](override val dataModel: DataModel) extends ${base}_Number[$tpe](dataModel) {
       |  def even                        = new $entA[$tpe](addOne(dataModel, Even         , Nil                    )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def odd                         = new $entA[$tpe](addOne(dataModel, Odd          , Nil                    )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def %(divider: T, remainder: T) = new $entA[$tpe](addOne(dataModel, Remainder    , Seq(divider, remainder))) with ${entA}_Sorting[Tpl, T] with CardOne
       |}
       |""".stripMargin
  else ""

  val decimal = if (hasDecimal)
    s"""
       |class ${base}_Decimal[Tpl <: Tuple, T](override val dataModel: DataModel) extends $base[$tpe](dataModel) {
       |  def ceil  = new $entA[$tpe](addOne(dataModel, AttrOp.Ceil  , Nil)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def floor = new $entA[$tpe](addOne(dataModel, AttrOp.Floor , Nil)) with ${entA}_Sorting[Tpl, T] with CardOne
       |}
       |""".stripMargin
  else ""

  val number = if (hasInteger || hasDecimal)
    s"""
       |class ${base}_Number[Tpl <: Tuple, T](override val dataModel: DataModel) extends $base[$tpe](dataModel) {
       |  def +(v: T) = new $entA[$tpe](addOne(dataModel, AttrOp.Plus  , Seq(v))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def -(v: T) = new $entA[$tpe](addOne(dataModel, AttrOp.Minus , Seq(v))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def *(v: T) = new $entA[$tpe](addOne(dataModel, AttrOp.Times , Seq(v))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def /(v: T) = new $entA[$tpe](addOne(dataModel, AttrOp.Divide, Seq(v))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def negate  = new $entA[$tpe](addOne(dataModel, AttrOp.Negate, Nil   )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def abs     = new $entA[$tpe](addOne(dataModel, AttrOp.Abs   , Nil   )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def absNeg  = new $entA[$tpe](addOne(dataModel, AttrOp.AbsNeg, Nil   )) with ${entA}_Sorting[Tpl, T] with CardOne
       |
       |  def apply(kw: sum)      = new $entA[Tpl                , T     ](asIs     (dataModel, kw)) with ${entA}_Sorting[Tpl                , T     ]
       |  def apply(kw: median)   = new $entA[Double *: Tail[Tpl], Double](toDouble (dataModel, kw)) with ${entA}_Sorting[Double *: Tail[Tpl], Double]
       |  def apply(kw: avg)      = new $entA[Double *: Tail[Tpl], Double](toDouble (dataModel, kw)) with ${entA}_Sorting[Double *: Tail[Tpl], Double]
       |  def apply(kw: variance) = new $entA[Double *: Tail[Tpl], Double](toDouble (dataModel, kw)) with ${entA}_Sorting[Double *: Tail[Tpl], Double]
       |  def apply(kw: stddev)   = new $entA[Double *: Tail[Tpl], Double](toDouble (dataModel, kw)) with ${entA}_Sorting[Double *: Tail[Tpl], Double]
       |}
       |""".stripMargin
  else ""

  val boolean = if (hasBoolean)
    s"""
       |class ${base}_Boolean[Tpl <: Tuple, T](override val dataModel: DataModel) extends $base[$tpe](dataModel) {
       |  def &&(bool: T) = new $entA[$tpe](addOne(dataModel, AttrOp.And, Seq(bool))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def ||(bool: T) = new $entA[$tpe](addOne(dataModel, AttrOp.Or , Seq(bool))) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def !           = new $entA[$tpe](addOne(dataModel, AttrOp.Not, Nil      )) with ${entA}_Sorting[Tpl, T] with CardOne
       |}
       |""".stripMargin
  else ""

  val distinguisher = if (last) "(implicit x: DummyImplicit)" else ""
  val sort = if (first) "" else s" with ${entA}_Sorting[Tpl, T]"
  def get: String =
    s"""
       |class $base[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe](dataModel) with CardOne$sort {
       |  def apply(                ) = new $entA[$tpe](addOne(dataModel, NoValue, Nil         )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def apply(v    : T, vs: T*) = new $entA[$tpe](addOne(dataModel, Eq     , Seq(v) ++ vs)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def apply(vs   : Seq[T]   ) = new $entA[$tpe](addOne(dataModel, Eq     , vs          )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (v    : T, vs: T*) = new $entA[$tpe](addOne(dataModel, Neq    , Seq(v) ++ vs)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (vs   : Seq[T]   ) = new $entA[$tpe](addOne(dataModel, Neq    , vs          )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def <    (upper: T        ) = new $entA[$tpe](addOne(dataModel, Lt     , Seq(upper)  )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def <=   (upper: T        ) = new $entA[$tpe](addOne(dataModel, Le     , Seq(upper)  )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def >    (lower: T        ) = new $entA[$tpe](addOne(dataModel, Gt     , Seq(lower)  )) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def >=   (lower: T        ) = new $entA[$tpe](addOne(dataModel, Ge     , Seq(lower)  )) with ${entA}_Sorting[Tpl, T] with CardOne
       |
       |  def apply(v    : qm) = new $entA[$tpe](addOne(dataModel, Eq , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def not  (v    : qm) = new $entA[$tpe](addOne(dataModel, Neq, Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def <    (upper: qm) = new $entA[$tpe](addOne(dataModel, Lt , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def <=   (upper: qm) = new $entA[$tpe](addOne(dataModel, Le , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def >    (lower: qm) = new $entA[$tpe](addOne(dataModel, Gt , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |  def >=   (lower: qm) = new $entA[$tpe](addOne(dataModel, Ge , Nil, true)) with ${entA}_Sorting[Tpl, T] with CardOne
       |
       |  def apply(a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Eq , a)) with ${entA}_Sorting[Tpl, T]
       |  def not  (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Neq, a)) with ${entA}_Sorting[Tpl, T]
       |  def <    (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Lt , a)) with ${entA}_Sorting[Tpl, T]
       |  def <=   (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Le , a)) with ${entA}_Sorting[Tpl, T]
       |  def >    (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Gt , a)) with ${entA}_Sorting[Tpl, T]
       |  def >=   (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe](filterAttr(dataModel, Ge , a)) with ${entA}_Sorting[Tpl, T]
       |
       |  def apply(a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Eq , a)) with ${entB}_Sorting[T *: Tpl, T]
       |  def not  (a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Neq, a)) with ${entB}_Sorting[T *: Tpl, T]
       |  def <    (a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Lt , a)) with ${entB}_Sorting[T *: Tpl, T]
       |  def <=   (a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Le , a)) with ${entB}_Sorting[T *: Tpl, T]
       |  def >    (a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Gt , a)) with ${entB}_Sorting[T *: Tpl, T]
       |  def >=   (a: Molecule_1[T] & CardOne) = new $entB[T *: Tpl, T](filterAttr(dataModel, Ge , a)) with ${entB}_Sorting[T *: Tpl, T]
       |
       |  def apply(kw: count)         = new $entA[Int *: Tail[Tpl]   , Int](toInt(dataModel, kw            )) with ${entA}_Sorting[Int *: Tail[Tpl], Int]
       |  def apply(kw: countDistinct) = new $entA[Int *: Tail[Tpl]   , Int](toInt(dataModel, kw            )) with ${entA}_Sorting[Int *: Tail[Tpl], Int]
       |  def apply(kw: min)           = new $entA[Tpl                , T  ](asIs (dataModel, kw            )) with ${entA}_Sorting[Tpl             , T  ]
       |  def apply(kw: max)           = new $entA[Tpl                , T  ](asIs (dataModel, kw            )) with ${entA}_Sorting[Tpl             , T  ]
       |  def apply(kw: sample)        = new $entA[Tpl                , T  ](asIs (dataModel, kw            )) with ${entA}_Sorting[Tpl             , T  ]
       |  def apply(kw: mins)          = new $entA[Set[T] *: Tail[Tpl], T  ](asIs (dataModel, kw, Some(kw.n)))
       |  def apply(kw: maxs)          = new $entA[Set[T] *: Tail[Tpl], T  ](asIs (dataModel, kw, Some(kw.n)))
       |  def apply(kw: samples)       = new $entA[Set[T] *: Tail[Tpl], T  ](asIs (dataModel, kw, Some(kw.n)))
       |  def apply(kw: distinct)      = new $entA[Set[T] *: Tail[Tpl], T  ](asIs (dataModel, kw            ))
       |}
       |$string$enu$integer$decimal$number$boolean
       |""".stripMargin
}
