package app.dataModel

import molecule.core._1_dataModel.data.model._


@InOut(3, 4)
object YourDomainDataModel {

  trait A {
    val int    = oneInt
    val str    = oneString
    val refB   = one[B]
    val refsBB = many[B]
  }

  trait B {
    val int1   = oneInt
    val str1   = oneString
    val refC   = one[C]
    val refsCC = many[C]
  }

  trait C {
    val int2 = oneInt
    val str2 = oneString
  }
}