package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprMapOpt(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity)  {

  val tpe2 = if (first) "T" else "Tpl, T"

  def get: String =
    s"""
       |class ${entA}_ExprMapOpt[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(map: Option[Map[String, T]]) = new $entA[Tpl                   , T](addMapOpt(dataModel, Eq , map     )) with CardMap
       |  def apply(key: String                ) = new $entA[Option[T] *: Tail[Tpl], T](addMapKs (dataModel, Has, Seq(key))) with CardMap
       |}
       |""".stripMargin
}
