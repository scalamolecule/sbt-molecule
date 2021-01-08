package app.dataModel
import molecule.core.data.model._


@InOut(0, 3)
object YourDomainDataModel {

  // Note lowercase namespace name
  trait person {
    val name     = oneString.fulltext
    val age      = oneInt
    val gender   = oneEnum("male", "female")
  }
}