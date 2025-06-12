package sbtmolecule.db.schema.sqlDialect

import molecule.core.model.DbAttribute
import molecule.core.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: DbAttribute): String
  def reservedKeyWords: List[String]
}
