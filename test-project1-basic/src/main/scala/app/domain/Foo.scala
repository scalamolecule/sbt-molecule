package app.domain

import molecule.DomainStructure


object Foo extends DomainStructure(2) {

  enum Color:
    case RED, BLUE, GREEN

  trait Person {
    val name = oneString
    val age  = oneInt

    val color    = oneEnum[Color]
    val colorSet = setEnum[Color]
    val colorSeq = seqEnum[Color]("optional comment")
  }
}
