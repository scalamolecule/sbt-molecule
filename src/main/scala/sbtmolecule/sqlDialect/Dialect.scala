package sbtmolecule.sqlDialect

import molecule.base.ast.MetaAttribute
import molecule.base.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: MetaAttribute): String
  def reservedKeyWords: List[String]
}
