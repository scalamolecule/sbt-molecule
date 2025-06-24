package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprSetTac(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasEnum: Boolean
) extends Formatting(metaDomain, metaEntity, arity)  {

  val base  = s"${entA}_ExprSetTac"
  val tpe1 = if (first) "T" else "Tpl <: Tuple, T"
  val tpe2 = if (first) "T" else "Tpl, T"

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                ) = new $entA[$tpe2](addSet(dataModel, NoValue, Set.empty[T]                                  )) with CardSet
       |  def apply(set: Set[T]     ) = new $entA[$tpe2](addSet(dataModel, Eq     , set           .map(_.toString.asInstanceOf[T]))) with CardSet
       |  def has  (v  : T, vs: T*  ) = new $entA[$tpe2](addSet(dataModel, Has    , (Set(v) ++ vs).map(_.toString.asInstanceOf[T]))) with CardSet
       |  def has  (vs : Iterable[T]) = new $entA[$tpe2](addSet(dataModel, Has    , (vs.toSet    ).map(_.toString.asInstanceOf[T]))) with CardSet
       |  def hasNo(v  : T, vs: T*  ) = new $entA[$tpe2](addSet(dataModel, HasNo  , (Set(v) ++ vs).map(_.toString.asInstanceOf[T]))) with CardSet
       |  def hasNo(vs : Iterable[T]) = new $entA[$tpe2](addSet(dataModel, HasNo  , (vs.toSet    ).map(_.toString.asInstanceOf[T]))) with CardSet
       |}
       |""".stripMargin
  else ""

  val filterTpe = if(first) "Tuple1[T]" else "T *: Tpl"
  val distinguisher = if (last) "(implicit x: DummyImplicit)" else ""
  def get: String =
    s"""
       |class $base[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                ) = new $entA[$tpe2](addSet(dataModel, NoValue, Set.empty[T])) with CardSet
       |  def apply(set: Set[T]     ) = new $entA[$tpe2](addSet(dataModel, Eq     , set         )) with CardSet
       |  def has  (v  : T, vs: T*  ) = new $entA[$tpe2](addSet(dataModel, Has    , Set(v) ++ vs)) with CardSet
       |  def has  (vs : Iterable[T]) = new $entA[$tpe2](addSet(dataModel, Has    , vs.toSet    )) with CardSet
       |  def hasNo(v  : T, vs: T*  ) = new $entA[$tpe2](addSet(dataModel, HasNo  , Set(v) ++ vs)) with CardSet
       |  def hasNo(vs : Iterable[T]) = new $entA[$tpe2](addSet(dataModel, HasNo  , vs.toSet    )) with CardSet
       |
       |  def has  (a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, Has  , a))
       |  def hasNo(a: Molecule_0 & CardOne)$distinguisher = new $entA[$tpe2](filterAttr(dataModel, HasNo, a))
       |
       |  def has  (a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, Has  , a))
       |  def hasNo(a: Molecule_1[T] & CardOne) = new $entB[$filterTpe, T](filterAttr(dataModel, HasNo, a))
       |}
       |$enu
       |""".stripMargin
}
