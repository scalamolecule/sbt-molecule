package app.domains.dataModel

import molecule.DataModel

object Person extends DataModel(2) {

  trait Person {
    val name = oneString
  }
}