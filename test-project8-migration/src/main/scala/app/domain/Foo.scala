package app.domain
import molecule.DomainStructure
trait Foo extends DomainStructure {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
