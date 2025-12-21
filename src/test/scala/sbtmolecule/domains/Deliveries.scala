package sbtmolecule.domains

import molecule.DomainStructure


trait Deliveries extends DomainStructure {

  trait User {
    val name = oneString
  }

  // 2 separate 1:N relationships
  trait Delivery {
    val contents = oneString
    val sender   = manyToOne[User].oneToMany("sentDeliveries")
    val receiver = manyToOne[User].oneToMany("receivedDeliveries")
  }

//  // one-to-many, flat
//  User.name.SentDeliveries.content
//  User.name.ReceivedDeliveries.content
//
//  // one-to-many, nested
//  User.name.SentDeliveries.*(Delivery.content)
//  User.name.ReceivedDeliveries.*(Delivery.content)
//
//
//  // many-to-one, flat
//  Delivery.content.Sender.name
//  Delivery.content.Receiver.name

  // (many-to-one, nested - not applicable)
}

