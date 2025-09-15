package app.domain

import molecule.DomainStructure

//object Accounting extends DomainStructure {
//  trait Invoice {
//    val no = oneInt
//  }
//  trait InvoiceLine {
//    val invoice = one[Invoice]("lines")
//    val amount  = oneInt
//  }
//}

object Accounting extends DomainStructure {

  trait Invoice {
    val no    = oneInt


    // .Lines.amount
    // .Lines.*(InvoiceLine.amount)

    // Invoice.no.Lines.*(InvoiceLine.product.amount).insert(
    //   (8, List(
    //     ("Hammer", 48),
    //     ("Spikes", 20))
    //   )
    // ).transact

    // Doesn't make sense - limited to adding only one invoice line
    // Invoice.no(8).Lines.product("Hammer").amount(48).save.transact
  }


  trait InvoiceLine {
    val product = oneString
    val amount  = oneInt

    val invoice = manyToOne[Invoice]("Lines")

    // .invoice
    // .Invoice.no


    // InvoiceLine.product("Screws").amount(14).invoice(invoiceId).save
    //
    // InvoiceLine.product.amount.invoice.insert(
    //   ("Hammer", 48, invoiceId),
    //   ("Spikes", 20, invoiceId),
    // )


    // Not allowed:
    // InvoiceLine.product("Screws").amount(14).Invoice.no(8).save
    //
    // InvoiceLine.product.amount.Invoice.no.insert(
    //   ("Hammer", 48, 8),
    //   ("Spikes", 20, 8), // would become a redundant additional invoice!
    // )
  }
}
