package app.domains.nested1.schema
import molecule.core.schema.definition._

@InOut(0, 3)
object AnimalDefinition {

  trait Animal {
    val name = oneString
  }
}