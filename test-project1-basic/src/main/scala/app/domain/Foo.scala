package app.domain

import molecule.DomainStructure


object Foo extends DomainStructure {

  enum Color:
    case RED, BLUE, GREEN

  trait Person {
    val name          = oneString
    val age           = oneInt
    val favoriteColor = oneEnum[Color]
    val home          = one[Address]
  }

  trait Address {
    val street = oneString
  }
}
