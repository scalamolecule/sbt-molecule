package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class Entity_Sort(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0
) extends Formatting(metaDomain, metaEntity) {

  def get: String =
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
       |package $pkg.$domain
       |package ops // to access enums and let them be public to the user
       |
       |$imports
       |
       |trait ${entity}_1_Sorting[Tpl <: Tuple, T] { self: Molecule & ModelTransformations_ =>
       |  def a1 = new ${entity}_1[Tpl, T](addSort(dataModel, "a1"))
       |  def a2 = new ${entity}_1[Tpl, T](addSort(dataModel, "a2"))
       |  def a3 = new ${entity}_1[Tpl, T](addSort(dataModel, "a3"))
       |  def a4 = new ${entity}_1[Tpl, T](addSort(dataModel, "a4"))
       |  def a5 = new ${entity}_1[Tpl, T](addSort(dataModel, "a5"))
       |  def d1 = new ${entity}_1[Tpl, T](addSort(dataModel, "d1"))
       |  def d2 = new ${entity}_1[Tpl, T](addSort(dataModel, "d2"))
       |  def d3 = new ${entity}_1[Tpl, T](addSort(dataModel, "d3"))
       |  def d4 = new ${entity}_1[Tpl, T](addSort(dataModel, "d4"))
       |  def d5 = new ${entity}_1[Tpl, T](addSort(dataModel, "d5"))
       |  def sort(i: Int): ${entity}_1[Tpl, T] = {
       |    i match {
       |      case 0     => new ${entity}_1[Tpl, T](addSort(dataModel, ""))
       |      case 1     => new ${entity}_1[Tpl, T](addSort(dataModel, "a1"))
       |      case -1    => new ${entity}_1[Tpl, T](addSort(dataModel, "d1"))
       |      case 2     => new ${entity}_1[Tpl, T](addSort(dataModel, "a2"))
       |      case -2    => new ${entity}_1[Tpl, T](addSort(dataModel, "d2"))
       |      case 3     => new ${entity}_1[Tpl, T](addSort(dataModel, "a3"))
       |      case -3    => new ${entity}_1[Tpl, T](addSort(dataModel, "d3"))
       |      case 4     => new ${entity}_1[Tpl, T](addSort(dataModel, "a4"))
       |      case -4    => new ${entity}_1[Tpl, T](addSort(dataModel, "d4"))
       |      case 5     => new ${entity}_1[Tpl, T](addSort(dataModel, "a5"))
       |      case -5    => new ${entity}_1[Tpl, T](addSort(dataModel, "d5"))
       |      case other => throw ExecutionError(
       |        s"Please use 1 to 5 for ascending orders and -1 to -5 for descending orders. Found $$other"
       |      )
       |    }
       |  }
       |}
       |
       |trait ${entity}_n_Sorting[Tpl <: Tuple, T] { self: Molecule & ModelTransformations_ =>
       |  def a1 = new ${entity}_n[Tpl, T](addSort(dataModel, "a1"))
       |  def a2 = new ${entity}_n[Tpl, T](addSort(dataModel, "a2"))
       |  def a3 = new ${entity}_n[Tpl, T](addSort(dataModel, "a3"))
       |  def a4 = new ${entity}_n[Tpl, T](addSort(dataModel, "a4"))
       |  def a5 = new ${entity}_n[Tpl, T](addSort(dataModel, "a5"))
       |  def d1 = new ${entity}_n[Tpl, T](addSort(dataModel, "d1"))
       |  def d2 = new ${entity}_n[Tpl, T](addSort(dataModel, "d2"))
       |  def d3 = new ${entity}_n[Tpl, T](addSort(dataModel, "d3"))
       |  def d4 = new ${entity}_n[Tpl, T](addSort(dataModel, "d4"))
       |  def d5 = new ${entity}_n[Tpl, T](addSort(dataModel, "d5"))
       |  def sort(i: Int): ${entity}_n[Tpl, T] = {
       |    i match {
       |      case 0     => new ${entity}_n[Tpl, T](addSort(dataModel, ""))
       |      case 1     => new ${entity}_n[Tpl, T](addSort(dataModel, "a1"))
       |      case -1    => new ${entity}_n[Tpl, T](addSort(dataModel, "d1"))
       |      case 2     => new ${entity}_n[Tpl, T](addSort(dataModel, "a2"))
       |      case -2    => new ${entity}_n[Tpl, T](addSort(dataModel, "d2"))
       |      case 3     => new ${entity}_n[Tpl, T](addSort(dataModel, "a3"))
       |      case -3    => new ${entity}_n[Tpl, T](addSort(dataModel, "d3"))
       |      case 4     => new ${entity}_n[Tpl, T](addSort(dataModel, "a4"))
       |      case -4    => new ${entity}_n[Tpl, T](addSort(dataModel, "d4"))
       |      case 5     => new ${entity}_n[Tpl, T](addSort(dataModel, "a5"))
       |      case -5    => new ${entity}_n[Tpl, T](addSort(dataModel, "d5"))
       |      case other => throw ExecutionError(
       |        s"Please use 1 to 5 for ascending orders and -1 to -5 for descending orders. Found $$other"
       |      )
       |    }
       |  }
       |}
       |""".stripMargin
}
