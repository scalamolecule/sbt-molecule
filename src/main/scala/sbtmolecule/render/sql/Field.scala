package sbtmolecule.render.sql

import molecule.base.ast.MetaAttr
import molecule.base.util.BaseHelpers

trait Field extends BaseHelpers {
  def tpe(a: MetaAttr): String
}
