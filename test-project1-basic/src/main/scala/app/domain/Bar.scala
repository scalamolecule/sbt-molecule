package app.domain

import molecule.DomainStructure


object Bar extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
    val home = one[Address]
//    val query = oneString
    val `type` = oneString.alias("tpe")
  }

  trait Address {
    val street = oneString

    val select = oneString
  }
}
