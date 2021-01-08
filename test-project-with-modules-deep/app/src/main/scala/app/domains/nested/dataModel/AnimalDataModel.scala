package app.domains.nested.dataModel

import molecule.core.data.model._

@InOut(0, 3)
object AnimalDataModel {

  trait Animal {
    val name = oneString
  }
}