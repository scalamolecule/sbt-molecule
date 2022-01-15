package app.dataModel

import molecule.core.data.model._


@InOut(0, 2)
object PersonDataModel {

  // Note lowercase namespace name
  trait person {
    val name = oneString
    val age  = oneInt
  }
}