package app.domain

import molecule.DomainStructure

trait Bar extends DomainStructure {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
