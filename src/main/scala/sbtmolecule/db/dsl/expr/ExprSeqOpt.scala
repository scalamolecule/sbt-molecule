package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprSeqOpt(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasEnum: Boolean
) extends Formatting(metaDomain, metaEntity, arity)  {

  val base = s"${entA}_ExprSeqOpt"
  val tpe2 = if (first) "T" else "Tpl, T"

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(optSeq: Option[Seq[T]]) = new $entA[$tpe2](addSeqOpt(dataModel, Eq, optSeq.map(_.map(_.toString.asInstanceOf[T]))))
       |}
       |""".stripMargin
  else ""

  def get: String =
    s"""
       |class $base[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(optSeq: Option[Seq[T]]) = new $entA[$tpe2](addSeqOpt(dataModel, Eq, optSeq))
       |}
       |$enu
       |""".stripMargin
}
