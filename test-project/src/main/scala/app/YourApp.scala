package app

import app.dsl.yourDomain._
import app.schema._
import molecule.api.out3._

object YourApp extends App {

  // Make db
  implicit val conn = recreateDbFrom(YourDomainSchema)

  // Load data
  val companyId = Person.name("John").age(26).gender("male").save.eid

  // Retrieve data
  val (person, age, gender) = Person.name.age.gender.get.head

  // Verify
  assert(s"$person is a $age years old $gender" == "John is a 26 years old male")
  val x = 7

  println(s"SUCCESS: $person is a $age years old $gender") //SUCCESS: John is a 26 years old male
}
