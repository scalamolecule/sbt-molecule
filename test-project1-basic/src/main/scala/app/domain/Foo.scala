package app.domain

import molecule.DomainStructure


object Foo extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
