package app.dataModel

import molecule.core.data.model._


@InOut(0, 3)
object YourDomainDataModel {

  trait Person {
    val name   = oneString.fulltext.doc("A Person's name")
    val age    = oneInt.doc("Age of person")
    val gender = oneEnum("male", "female").doc("Gender of person")
  }
}