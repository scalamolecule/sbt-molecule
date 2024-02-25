package app.domains.nested.dataModel

import molecule.DataModel

object Animal extends DataModel(2) {

  trait Animal {
    val name = oneString
  }
}