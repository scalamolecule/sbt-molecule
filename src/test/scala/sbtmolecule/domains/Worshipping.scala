package sbtmolecule.domains

import molecule.DomainStructure

//trait Worshipping extends DomainStructure {
//
//  trait Worshipper {
//    val name = oneString
//    val god  = one[God].worshippers
//  }
//
//  trait God {
//    val name        = oneString
//    val worshippers = _many[Worshipper] // reverse side
//  }
//}
//
//trait Worshipping2 extends DomainStructure {
//
//  trait Worshipper {
//    val name = oneString
//    val god  = one[God]
//  }
//
//  trait God {
//    val name        = oneString
//    val worshippers = many[Worshipper].god
//  }
//}

trait Worshipping extends DomainStructure {

  trait Worshipper {
    val name = oneString
    val god  = manyToOne[God]
  }

  trait God {
    val name = oneString
  }
}
