package app.domain

import molecule.DomainStructure


object Bar extends DomainStructure(2) {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
