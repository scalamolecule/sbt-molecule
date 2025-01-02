package app

import app.domain.dsl.Person._
import molecule.sql.h2.sync._


class Test_sync extends TestSetup {

  "test" - h2 { implicit conn =>
    female_Character.name("Kim").mood("furious").Question.says("What's going on?").save.transact
    male_Character.name("Jimmie").mood("good").Answer.says("It's all good, man").save.transact

    female_Character.name.mood.Question.says.query.get.head ==>
      ("Kim", "furious", "What's going on?")

    male_Character.name.mood.Answer.says.query.get.head ==>
      ("Jimmie", "good", "It's all good, man")


    // Qualified back refs
    female_Character.name.Question.says._female_Character.mood.query.get.head ==>
      ("Kim", "What's going on?", "furious")

    male_Character.name.Answer.says._male_Character.mood.query.get.head ==>
      ("Jimmie", "It's all good, man", "good")
  }
}
