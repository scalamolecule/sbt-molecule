package app.domains

import molecule.Domain

object Person extends Domain(2) {

  trait Person {
    val name = oneString
  }
}