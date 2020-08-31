package app

import app.dsl.yourDomain._
import app.schema._
import molecule.api.out3._

object YourApp extends App {

  // Make db
  implicit val conn = recreateDbFrom(YourDomainSchema)

  // Save data
  female_Character.name("Kim").mood("furious").Question.says("What's going on?").save
  male_Character.name("Jimmie").mood("good").Answer.says("It's all good, man").save


  // Retrieve data

  assert(female_Character.name.mood.Question.says.get.head ==
    ("Kim", "pissed", "What's going on?"))

  assert(male_Character.name.mood.Answer.says.get.head ==
    ("Jimmie", "defensive", "It's all good, man"))


  // Checking multiple back references to same-named namespaces
  // with same name in different partitions

  assert(female_Character.name.Question.says._female_Character.mood.get.head ==
    ("Kim", "What's going on?", "pissed"))

  assert(male_Character.name.Answer.says._male_Character.mood.get.head ==
    ("Jimmie", "It's all good, man", "defensive"))


  println("It's all good, man")
}
