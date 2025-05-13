package sbtmolecule.sqlDialect

import molecule.db.base.ast.MetaAttribute
import molecule.db.base.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: MetaAttribute): String
  def reservedKeyWords: List[String]
}
