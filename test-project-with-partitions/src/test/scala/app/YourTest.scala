package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer
import org.specs2.mutable.Specification


class YourTest extends Specification {


  "app" >> {
    YourApp.jimmieSays === "It's all good, man"
  }


  "test" >> {
    // Make in-mem db
    implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

    // Save data
    female_Character.name("Kim").mood("furious").Question.says("What's going on?").save
    male_Character.name("Jimmie").mood("good").Answer.says("It's all good, man").save


    // Retrieve data

    female_Character.name.mood.Question.says.get.head ==
      ("Kim", "furious", "What's going on?")

    male_Character.name.mood.Answer.says.get.head ==
      ("Jimmie", "good", "It's all good, man")


    // Checking multiple back references to same-named namespaces
    // with same name in different partitions

    female_Character.name.Question.says._female_Character.mood.get.head ===
      ("Kim", "What's going on?", "furious")

    male_Character.name.Answer.says._male_Character.mood.get.head ==
      ("Jimmie", "It's all good, man", "good")
  }
}
