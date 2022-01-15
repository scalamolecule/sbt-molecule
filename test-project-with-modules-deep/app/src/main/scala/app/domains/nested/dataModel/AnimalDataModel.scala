package app.domains.nested.dataModel

import molecule.core.data.model._

@InOut(0, 2)
object AnimalDataModel {

  trait Animal {
    val name = oneString
  }
}