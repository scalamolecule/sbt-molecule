package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprOneOpt(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasEnum: Boolean
) extends Formatting(metaDomain, metaEntity, arity) {

  val base = s"${entA}_ExprOneOpt"
  val tpe  = if (first) "T" else "Tpl, T"

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe](dataModel) {
       |  def apply(opt: Option[T]) = new $entA[$tpe](addOneOpt(dataModel, Eq, opt.map(_.toString.asInstanceOf[T]))) with ${entA}_Sorting[Tpl, T]
       |}
       |""".stripMargin
  else ""

  val sort = if (first) "" else s" with ${entA}_Sorting[Tpl, T]"
  def get: String =
    s"""
       |class $base[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe](dataModel) with CardOne$sort {
       |  def apply(opt: Option[T]) = new $entA[$tpe](addOneOpt(dataModel, Eq, opt)) with ${entA}_Sorting[Tpl, T]
       |}
       |$enu
       |""".stripMargin
}
