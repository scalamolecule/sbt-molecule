package app

import app.dsl.Person._
import app.schema.PersonSchema
import molecule.core.util.Executor._
import molecule.datomic.api._
import molecule.datomic.peer.facade.Datomic_Peer
import utest._


object Test extends TestSuite {

  implicit val conn = Datomic_Peer.recreateDbFrom(PersonSchema)

  lazy val tests = Tests {
    "test" - {
      for {
        _ <- female_character.name("Kim").mood("furious").Question.says("What's going on?").save
        _ <- male_character.name("Jimmie").mood("good").Answer.says("It's all good, man").save


        _ <- female_character.name.mood.Question.says.get.map(_.head ==> ("Kim", "furious", "What's going on?"))

        _ <- male_character.name.mood.Answer.says.get.map(_.head ==>
          ("Jimmie", "good", "It's all good, man"))

        // Checking multiple back references to same-named namespaces
        // with same name in different partitions

        _ <- female_character.name.Question.says._female_character.mood.get.map(_.head ==>
          ("Kim", "What's going on?", "furious"))

        _ <- male_character.name.Answer.says._male_character.mood.get.map(_.head ==>
          ("Jimmie", "It's all good, man", "good"))
      } yield ()
    }
  }
}
