package sbtmolecule.render.sql

import molecule.base.ast.MetaAttr
import molecule.base.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: MetaAttr): String
  def reservedKeyWords: List[String]
}
