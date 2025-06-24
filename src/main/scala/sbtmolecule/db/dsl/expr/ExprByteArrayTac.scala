package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprByteArrayTac(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity)  {

  val tpe1 = if (first) "T" else "Tpl <: Tuple, T"
  val tpe2 = if (first) "T" else "Tpl, T"

  def get: String =
    s"""
       |class ${entA}_ExprBArTac[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                   ) = new $entA[$tpe2](addBAr(dataModel, NoValue, Array.empty[Byte].asInstanceOf[Array[T]])) with CardSeq
       |  def apply(byteArray: Array[T]) = new $entA[$tpe2](addBAr(dataModel, Eq     , byteArray                               )) with CardSeq
       |  def not  (byteArray: Array[T]) = new $entA[$tpe2](addBAr(dataModel, Neq    , byteArray                               )) with CardSeq
       |}
       |""".stripMargin
}
