package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprSeqTac(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasEnum: Boolean
) extends Formatting(metaDomain, metaEntity, arity)  {

  val base  = s"${entA}_ExprSeqTac"
  val tpe1 = if (first) "T" else "Tpl <: Tuple, T"
  val tpe2 = if (first) "T" else "Tpl, T"

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                ) = new $entA[$tpe2](addSeq(dataModel, NoValue, Seq.empty[T]                                  )) with CardSeq
       |  def apply(seq: Seq[T]     ) = new $entA[$tpe2](addSeq(dataModel, Eq     , seq           .map(_.toString.asInstanceOf[T]))) with CardSeq
       |  def has  (v  : T, vs: T*  ) = new $entA[$tpe2](addSeq(dataModel, Has    , (Seq(v) ++ vs).map(_.toString.asInstanceOf[T]))) with CardSeq
       |  def has  (vs : Iterable[T]) = new $entA[$tpe2](addSeq(dataModel, Has    , (Seq()  ++ vs).map(_.toString.asInstanceOf[T]))) with CardSeq
       |  def hasNo(v  : T, vs: T*  ) = new $entA[$tpe2](addSeq(dataModel, HasNo  , (Seq(v) ++ vs).map(_.toString.asInstanceOf[T]))) with CardSeq
       |  def hasNo(vs : Iterable[T]) = new $entA[$tpe2](addSeq(dataModel, HasNo  , (Seq()  ++ vs).map(_.toString.asInstanceOf[T]))) with CardSeq
       |}
       |""".stripMargin
  else ""

  val filterTpe = if(first) "Tuple1[T]" else "T *: Tpl"
  val distinguisher = if (last) "(implicit x: DummyImplicit)" else ""
  def get: String =
    s"""
       |class $base[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                ) = new $entA[$tpe2](addSeq(dataModel, NoValue, Seq.empty[T])) with CardSeq
       |  def apply(seq: Seq[T]     ) = new $entA[$tpe2](addSeq(dataModel, Eq     , seq         )) with CardSeq
       |  def has  (v  : T, vs: T*  ) = new $entA[$tpe2](addSeq(dataModel, Has    , Seq(v) ++ vs)) with CardSeq
       |  def has  (vs : Iterable[T]) = new $entA[$tpe2](addSeq(dataModel, Has    , Seq()  ++ vs)) with CardSeq
       |  def hasNo(v  : T, vs: T*  ) = new $entA[$tpe2](addSeq(dataModel, HasNo  , Seq(v) ++ vs)) with CardSeq
       |  def hasNo(vs : Iterable[T]) = new $entA[$tpe2](addSeq(dataModel, HasNo  , Seq()  ++ vs)) with CardSeq
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
