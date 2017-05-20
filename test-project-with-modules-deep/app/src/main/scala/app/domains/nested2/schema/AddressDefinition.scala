package app.domains.nested2.schema
import molecule.schema.definition._

@InOut(0, 3)
object AddressDefinition {

  trait Address {
    val street = oneString
    val city   = oneString
  }
}