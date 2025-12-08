package sbtmolecule.db.sqlDialect

import molecule.base.metaModel.MetaAttribute
import molecule.base.util.BaseHelpers

trait Dialect extends BaseHelpers {
  def tpe(a: MetaAttribute, generalProps: Map[String, String] = Map.empty): String
  def reservedKeyWords: List[String]

  /** Database identifier for looking up custom column properties */
  def dbId: String
}
