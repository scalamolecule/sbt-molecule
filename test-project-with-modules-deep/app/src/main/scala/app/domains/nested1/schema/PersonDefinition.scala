package app.domains.nested1.schema
import molecule.core.schema.definition._

@InOut(0, 3)
object PersonDefinition {

  trait Person {
    val name     = oneString.fulltext
    val age      = oneInt
    val gender   = oneEnum("male", "female")
  }
}