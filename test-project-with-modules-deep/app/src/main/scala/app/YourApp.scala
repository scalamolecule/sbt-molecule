package app

import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer


object YourApp {


  def getOrder: String = {
    import app.domains.dsl.order._
    import app.domains.schema.OrderSchema

    // Make in-mem db
    implicit val conn = Datomic_Peer.recreateDbFrom(OrderSchema)

    // Save Lisa
    Order.id("abc").save.eid

    // Retrieve Lisa
    Order.id.get.head
  }


  def getAnimal: String = {
    import app.domains.nested.dsl.animal._
    import app.domains.nested.schema.AnimalSchema

    // Make in-mem db
    implicit val conn = Datomic_Peer.recreateDbFrom(AnimalSchema)

    // Save Lisa
    Animal.name("bob").save.eid

    // Retrieve Lisa
    Animal.name.get.head
  }
}
