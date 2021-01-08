package app.domains.dataModel
import molecule.core.data.model._

@InOut(0, 3)
object OrderDataModel {

  trait Order {
    val id = oneString
  }
}