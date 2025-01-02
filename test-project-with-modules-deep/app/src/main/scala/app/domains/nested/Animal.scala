package app.domains.nested

import molecule.Domain

object Animal extends Domain(2) {

  trait Animal {
    val name = oneString
  }
}