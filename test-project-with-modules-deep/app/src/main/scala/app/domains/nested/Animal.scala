package app.domains.nested

import molecule.DomainStructure

object Animal extends DomainStructure(2) {

  trait Animal {
    val name = oneString
  }
}