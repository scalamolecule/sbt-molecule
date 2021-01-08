package app

import utest._
import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer

object TestFooJVM extends TestSuite {

  val tests = Tests {

    test("jvm") {
      FooJVM.findLisa ==> ("Lisa", 27, "female")
    }

    test("test") {
      // Make in-mem db
      implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

      // Save John
      Person.name("John").age(26).gender("male").save.eid

      // Retrieve John
      Person.name.age.gender.get.head ==> ("John", 26, "male")
    }
  }
}