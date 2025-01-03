package sbtmolecule.domain

import molecule.DomainStructure

object Scopes extends DomainStructure(3) {

  object accounting {
    trait Invoice {
      val no          = oneInt
      val mainProduct = one[warehouse.Item]
      val lines       = many[InvoiceLine]
    }
    trait InvoiceLine {
      val text    = oneString
      val qty     = oneInt
      val product = one[warehouse.Item]
      val invoice = one[Invoice]
    }
  }

  object warehouse {
    trait Item {
      val name     = oneString
      val invoiced = many[accounting.Invoice]
    }
  }
}

