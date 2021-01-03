package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer

object YourApp extends App {

  // Make db
  implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

  // Save data
  female_character.name("Kim").mood("pissed").Question.says("What's going on?").save
  male_character.name("Jimmie").mood("defensive").Answer.says("It's all good, man").save


  // Retrieve data

  assert(female_character.name.mood.Question.says.get.head ==
    ("Kim", "pissed", "What's going on?"))

  assert(male_character.name.mood.Answer.says.get.head ==
    ("Jimmie", "defensive", "It's all good, man"))


  // Checking multiple back references to same-named namespaces
  // with same name in different partitions

  assert(female_character.name.Question.says._female_character.mood.get.head ==
    ("Kim", "What's going on?", "pissed"))

  assert(male_character.name.Answer.says._male_character.mood.get.head ==
    ("Jimmie", "It's all good, man", "defensive"))


  println("It's all good, man")
}
