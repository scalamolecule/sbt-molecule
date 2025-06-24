package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprSetOpt(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int,
  hasEnum: Boolean
) extends Formatting(metaDomain, metaEntity, arity)  {

  val base = s"${entA}_ExprSetOpt"
  val tpe2 = if (first) "T" else "Tpl, T"

  val enu = if (hasEnum)
    s"""
       |class ${base}_Enum[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(optSet: Option[Set[T]]) = new $entA[$tpe2](addSetOpt(dataModel, Eq, optSet.map(_.map(_.toString.asInstanceOf[T]))))
       |}
       |""".stripMargin
  else ""

  def get: String =
    s"""
       |class $base[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(optSet: Option[Set[T]]) = new $entA[$tpe2](addSetOpt(dataModel, Eq, optSet))
       |}
       |$enu
       |""".stripMargin
}
