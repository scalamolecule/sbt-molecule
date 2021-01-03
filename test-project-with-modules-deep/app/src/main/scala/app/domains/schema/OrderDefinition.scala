package app.domains.schema
import molecule.core.schema.definition._

@InOut(0, 3)
object OrderDefinition {

  trait Order {
    val id = oneString
  }
}