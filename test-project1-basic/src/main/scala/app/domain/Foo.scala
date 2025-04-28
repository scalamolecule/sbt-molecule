package app.domain

import molecule.DomainStructure


object Foo extends DomainStructure(2) {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
