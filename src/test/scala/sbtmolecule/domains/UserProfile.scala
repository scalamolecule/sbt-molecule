package sbtmolecule.domains

import molecule.DomainStructure


trait UserProfile extends DomainStructure {

  trait User {
    val name    = oneString
    val profile = manyToOne[Profile] //.unique // "user" used as reverse ref name
  }

  trait Profile {
    val name = oneString
  }
}

