package app.domains

import molecule.DomainStructure

object Person extends DomainStructure(2) {

  trait Person {
    val name = oneString
  }
}