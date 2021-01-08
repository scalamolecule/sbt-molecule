package app

import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer
import org.specs2.mutable.Specification


class YourTest extends Specification {


  "animal" >> {
    YourApp.getAnimal === "bob"
  }

  "order" >> {
    YourApp.getOrder === "abc"
  }


  "test" >> {
    import app.domains.nested.dsl.animal._
    import app.domains.nested.schema.AnimalSchema

    // Make in-mem db
    implicit val conn = Datomic_Peer.recreateDbFrom(AnimalSchema)
    Animal.name("bob").save.eid
    Animal.name.get.head === "bob"
  }
}
