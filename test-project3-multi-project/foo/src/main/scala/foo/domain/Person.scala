package foo.domain

import molecule.DomainStructure


object Person extends DomainStructure(2) {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}