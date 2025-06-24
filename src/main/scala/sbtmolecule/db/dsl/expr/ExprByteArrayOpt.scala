package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprByteArrayOpt(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity)  {

  val tpe2 = if (first) "T" else "Tpl, T"

  def get: String =
    s"""
       |class ${entA}_ExprBArOpt[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(byteArray: Option[Array[T]]) = new $entA[$tpe2](addBArOpt(dataModel, Eq, byteArray))
       |}
       |""".stripMargin
}
