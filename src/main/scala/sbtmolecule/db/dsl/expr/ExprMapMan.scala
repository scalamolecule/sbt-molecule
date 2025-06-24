package sbtmolecule.db.dsl.expr

import molecule.base.metaModel.{MetaDomain, MetaEntity}
import sbtmolecule.Formatting

case class ExprMapMan(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  arity: Int
) extends Formatting(metaDomain, metaEntity, arity) {

  val tpe2 = if (first) "T" else "Tpl, T"

  def get: String =
    s"""
       |class ${entA}_ExprMapMan[Tpl <: Tuple, T](override val dataModel: DataModel) extends $entA[$tpe2](dataModel) {
       |  def apply (                                       ) = new $entA[Tpl           , T](addMap  (dataModel, NoValue, Map.empty[String, T] )) with CardMap
       |  def apply (map  : Map[String, T]                  ) = new $entA[Tpl           , T](addMap  (dataModel, Eq     , map                  )) with CardMap
       |  def apply (key  : String                          ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, Eq     , Seq(key)             )) with CardMap
       |  def not   (key : String, keys: String*            ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, Neq    , Seq(key) ++ keys     )) with CardMap
       |  def not   (keys: Seq[String]                      ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, Neq    , keys                 )) with CardMap
       |  def has   (v : T, vs: T*                          ) = new $entA[Tpl           , T](addMapVs(dataModel, Has    , Seq(v) ++ vs         )) with CardMap
       |  def has   (vs: Seq[T]                             ) = new $entA[Tpl           , T](addMapVs(dataModel, Has    , vs                   )) with CardMap
       |  def hasNo (v : T, vs: T*                          ) = new $entA[Tpl           , T](addMapVs(dataModel, HasNo  , Seq(v) ++ vs         )) with CardMap
       |  def hasNo (vs: Seq[T]                             ) = new $entA[Tpl           , T](addMapVs(dataModel, HasNo  , vs                   )) with CardMap
       |  def add   (pair : (String, T), pairs: (String, T)*) = new $entA[Tpl           , T](addMap  (dataModel, Add    , (pair +: pairs).toMap)) with CardMap
       |  def add   (pairs: Seq[(String, T)]                ) = new $entA[Tpl           , T](addMap  (dataModel, Add    , pairs.toMap          )) with CardMap
       |  def remove(key  : String, keys: String*           ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, Remove , Seq(key) ++ keys     )) with CardMap
       |  def remove(keys : Seq[String]                     ) = new $entA[T *: Tail[Tpl], T](addMapKs(dataModel, Remove , keys                 )) with CardMap
       |}
       |""".stripMargin
}
