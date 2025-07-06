package sbtmolecule.db.sqlDialect

import molecule.base.metaModel.MetaAttribute
import molecule.base.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: MetaAttribute): String
  def reservedKeyWords: List[String]
}
