package app.domain

import molecule.DomainStructure


object Bar extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
