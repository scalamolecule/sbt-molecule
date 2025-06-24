package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprMapTac(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity) {

  val tpe1 = if (first) "T" else "Tpl <: Tuple, T"
  val tpe2 = if (first) "T" else "Tpl, T"
  val tpe3 = if (first) "T" else "Tpl           , T"
  val tpe4 = if (first) "T" else "T *: Tail[Tpl], T"

  def get: String =
    s"""
       |class ${entA}_ExprMapTac[$tpe1](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply(                           ) = new $entA[$tpe3](addMap   (dataModel, NoValue, Map.empty[String, T])) with CardMap
       |  def apply(key : String, keys: String*) = new $entA[$tpe4](addMapKs (dataModel, Eq     , Seq(key) ++ keys    )) with CardMap
       |  def apply(keys: Seq[String]          ) = new $entA[$tpe4](addMapKs (dataModel, Eq     , keys                )) with CardMap
       |  def not  (key : String, keys: String*) = new $entA[$tpe4](addMapKs (dataModel, Neq    , Seq(key) ++ keys    )) with CardMap
       |  def not  (keys: Seq[String]          ) = new $entA[$tpe4](addMapKs (dataModel, Neq    , keys                )) with CardMap
       |  def has  (v : T, vs: T*              ) = new $entA[$tpe3](addMapVs (dataModel, Has    , Seq(v) ++ vs        )) with CardMap
       |  def has  (vs: Seq[T]                 ) = new $entA[$tpe3](addMapVs (dataModel, Has    , vs                  )) with CardMap
       |  def hasNo(v : T, vs: T*              ) = new $entA[$tpe3](addMapVs (dataModel, HasNo  , Seq(v) ++ vs        )) with CardMap
       |  def hasNo(vs: Seq[T]                 ) = new $entA[$tpe3](addMapVs (dataModel, HasNo  , vs                  )) with CardMap
       |}
       |""".stripMargin
}
