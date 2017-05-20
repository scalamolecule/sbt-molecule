package app.domains.nested1.schema
import molecule.schema.definition._

@InOut(0, 3)
object AnimalDefinition {

  trait Animal {
    val name = oneString
  }
}