package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer

object YourApp extends App {

  // Make db
  implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

  // Load data
  val companyId = person.name("John").age(26).gender("male").save.eid

  // Retrieve data
  val (name, age, gender) = person.name.age.gender.get.head

  // Verify
  assert(s"$name is a $age years old $gender" == "John is a 26 years old male")

  println(s"SUCCESS: $name is a $age years old $gender") //SUCCESS: John is a 26 years old male
}
