package sbtmolecule.db

import molecule.DomainStructure

object Starwars extends DomainStructure {

  trait Character {
    val name      = oneString
//    val friends   = many[Character]
    val appearsIn = setString
  }

  trait Droid {
    val name            = oneString
//    val friends         = many[Character]
    val appearsIn       = setString
    val primaryFunction = oneString
  }

  trait Human {
    val name       = oneString
//    val friends    = many[Character]
    val appearsIn  = setString
    val homePlanet = oneString
  }
}

object Starwars2 extends DomainStructure {

  trait Character {
    val name      = oneString
//    val friends   = many[Character]
    val appearsIn = setString
  }

  trait Droid {
    val name            = oneString
//    val friends         = many[Character]
    val appearsIn       = setString
    val primaryFunction = oneString
  }

  trait Human {
    val name       = oneString
//    val friends    = many[Character]
    val appearsIn  = setString
    val homePlanet = oneString

    val x = manyToOne[Character].oneToMany("Friends")
  }

//  trait Friend {
//    val
//  }
}