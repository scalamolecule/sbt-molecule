package sbtmolecule.parse.domains

import molecule.DomainStructure

//object Worshipping extends DomainStructure {
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
//object Worshipping2 extends DomainStructure {
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

object Worshipping extends DomainStructure {

  trait Worshipper {
    val name = oneString
    val god  = manyToOne[God]
  }

  trait God {
    val name = oneString
  }
}
