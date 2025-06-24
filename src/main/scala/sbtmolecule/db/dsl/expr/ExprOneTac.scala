package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprOneTac(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasString: Boolean,
  hasEnum: Boolean,
  hasInteger: Boolean,
)extends Formatting(metaDomain, metaEntity, arity)  {

  val base = s"${entA}_ExprOneTac"
  val tpe1 = if (first) "T" else "Tpl <: Tuple, T"
  val tpe2 = if (first) "T" else "Tpl, T"


  val string = if (hasString)
    s"""
       |class ${base}_String[$tpe1](override val dataModel: DataModel) extends $base[$tpe2](dataModel) {
       |  def startsWith(prefix: T) = new $entA[$tpe2](addOne(dataModel, StartsWith, Seq(prefix))) with CardOne
       |  def endsWith  (suffix: T) = new $entA[$tpe2](addOne(dataModel, EndsWith  , Seq(suffix))) with CardOne
       |  def contains  (needle: T) = new $entA[$tpe2](addOne(dataModel, Contains  , Seq(needle))) with CardOne
       |  def matches   (regex : T) = new $entA[$tpe2](addOne(dataModel, Matches   , Seq(regex) )) with CardOne
       |
       |  def startsWith(prefix: qm) = new $entA[$tpe2](addOne(dataModel, StartsWith, Nil, true)) with CardOne
       |  def endsWith  (suffix: qm) = new $entA[$tpe2](addOne(dataModel, EndsWith  , Nil, true)) with CardOne
       |  def contains  (needle: qm) = new $entA[$tpe2](addOne(dataModel, Contains  , Nil, true)) with CardOne
       |  def matches   (regex : qm) = new $entA[$tpe2](addOne(dataModel, Matches   , Nil, true)) with CardOne
       |}
       |""".stripMargin
  else ""

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(             ) = new $entA[$tpe2](addOne(dataModel, NoValue, Nil                                      )) with CardOne
       |  def apply(v : T, vs: T*) = new $entA[$tpe2](addOne(dataModel, Eq     , (v +: vs).map(_.toString.asInstanceOf[T]))) with CardOne
       |  def apply(vs: Seq[T]   ) = new $entA[$tpe2](addOne(dataModel, Eq     , vs       .map(_.toString.asInstanceOf[T]))) with CardOne
       |  def not  (v : T, vs: T*) = new $entA[$tpe2](addOne(dataModel, Neq    , (v +: vs).map(_.toString.asInstanceOf[T]))) with CardOne
       |  def not  (vs: Seq[T]   ) = new $entA[$tpe2](addOne(dataModel, Neq    , vs       .map(_.toString.asInstanceOf[T]))) with CardOne
       |
       |  def apply(v : qm) = new $entA[$tpe2](addOne(dataModel, Eq , Nil, true)) with CardOne
       |  def not  (v : qm) = new $entA[$tpe2](addOne(dataModel, Neq, Nil, true)) with CardOne
       |}
       |""".stripMargin
  else ""

  val integer = if (hasInteger)
    s"""
       |class ${base}_Integer[$tpe1](override val dataModel: DataModel) extends $base[$tpe2](dataModel) {
       |  def even                        = new $entA[$tpe2](addOne(dataModel, Even     , Nil                    )) with CardOne
       |  def odd                         = new $entA[$tpe2](addOne(dataModel, Odd      , Nil                    )) with CardOne
       |  def %(divider: T, remainder: T) = new $entA[$tpe2](addOne(dataModel, Remainder, Seq(divider, remainder))) with CardOne
       |}
       |""".stripMargin
  else ""

  val filterTpe = if(first) "Tuple1[T]" else "T *: Tpl"
  val distinguisher = if (last) "(implicit x: DummyImplicit)" else ""

  def get: String =
    s"""
       |class $base[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) with CardOne {
       |  def apply(                ) = new $entA[$tpe2](addOne(dataModel, NoValue, Nil         )) with CardOne
       |  def apply(v    : T, vs: T*) = new $entA[$tpe2](addOne(dataModel, Eq     , Seq(v) ++ vs)) with CardOne
       |  def apply(vs   : Seq[T]   ) = new $entA[$tpe2](addOne(dataModel, Eq     , vs          )) with CardOne
       |  def not  (v    : T, vs: T*) = new $entA[$tpe2](addOne(dataModel, Neq    , Seq(v) ++ vs)) with CardOne
       |  def not  (vs   : Seq[T]   ) = new $entA[$tpe2](addOne(dataModel, Neq    , vs          )) with CardOne
       |  def <    (upper: T        ) = new $entA[$tpe2](addOne(dataModel, Lt     , Seq(upper)  )) with CardOne
       |  def <=   (upper: T        ) = new $entA[$tpe2](addOne(dataModel, Le     , Seq(upper)  )) with CardOne
       |  def >    (lower: T        ) = new $entA[$tpe2](addOne(dataModel, Gt     , Seq(lower)  )) with CardOne
       |  def >=   (lower: T        ) = new $entA[$tpe2](addOne(dataModel, Ge     , Seq(lower)  )) with CardOne
       |
       |  def apply(v    : qm) = new $entA[$tpe2](addOne(dataModel, Eq , Nil, true)) with CardOne
       |  def not  (v    : qm) = new $entA[$tpe2](addOne(dataModel, Neq, Nil, true)) with CardOne
       |  def <    (upper: qm) = new $entA[$tpe2](addOne(dataModel, Lt , Nil, true)) with CardOne
       |  def <=   (upper: qm) = new $entA[$tpe2](addOne(dataModel, Le , Nil, true)) with CardOne
       |  def >    (lower: qm) = new $entA[$tpe2](addOne(dataModel, Gt , Nil, true)) with CardOne
       |  def >=   (lower: qm) = new $entA[$tpe2](addOne(dataModel, Ge , Nil, true)) with CardOne
       |
       |  def apply(a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Eq , a))
       |  def not  (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Neq, a))
       |  def <    (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Lt , a))
       |  def <=   (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Le , a))
       |  def >    (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Gt , a))
       |  def >=   (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Ge , a))
       |
       |  def apply(a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Eq , a))
       |  def not  (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Neq, a))
       |  def <    (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Lt , a))
       |  def <=   (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Le , a))
       |  def >    (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Gt , a))
       |  def >=   (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Ge , a))
       |}
       |$string$enu$integer
       |""".stripMargin
}
