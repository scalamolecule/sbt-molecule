package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer

object FooJVM {

  // sbt> fooJVM/run
  def main(args: Array[String]): Unit = {
    println(Shared.confirm("Run jvm"))
  }

  def hello(msg: String): String = "Hello " + msg

  def findLisa: (String, Int, String) = {

    // Make in-mem db
    implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

    // Save Lisa
    Person.name("Lisa").age(27).gender("female").save.eid

    // Retrieve Lisa
    Person.name.age.gender.get.head
  }
}



//import app.dsl.yourDomain._
//import app.schema._
//import molecule.datomic.api.out3._
//import molecule.datomic.peer.facade.Datomic_Peer
//
//object YourApp {
//
//
//  def getLisa: (String, Int, String) = {
//
//    // Make in-mem db
//    implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)
//
//    // Save Lisa
//    Person.name("Lisa").age(27).gender("female").save.eid
//
//    // Retrieve Lisa
//    Person.name.age.gender.get.head
//  }
//}