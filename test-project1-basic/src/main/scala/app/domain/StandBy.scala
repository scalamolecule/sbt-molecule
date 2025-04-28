package app.domain

import molecule.DomainStructure

// When maxArity is set to 0, no boilerplate is generated from the domain.
// This is an easy way to quickly omit generation for some domain.
object StandBy extends DomainStructure(0) {

  trait Person {
    val name = oneString
    val age  = oneInt
  }
}