package app.dataModel

import molecule.core.data.model._

@InOut(0, 1)
object TxCountDataModel {

  trait TxCount {
    val db     = oneString.doc("Database name")
    val basisT = oneLong.doc("Datomic basis T")
  }
}
