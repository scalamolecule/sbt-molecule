package app

import app.domain.dsl.Person._
import molecule.core.util.Executor._
import molecule.sql.h2.async._


class Test_async extends TestSetup {

  "test" - h2 { implicit conn =>
    for {
      _ <- female_Character.name("Kim").mood("furious").Question.says("What's going on?").save.transact
      _ <- male_Character.name("Jimmie").mood("good").Answer.says("It's all good, man").save.transact

      _ <- female_Character.name.mood.Question.says.query.get.map(_.head ==>
        ("Kim", "furious", "What's going on?"))

      _ <- male_Character.name.mood.Answer.says.query.get.map(_.head ==>
        ("Jimmie", "good", "It's all good, man"))


      // Qualified back refs
      _ <- female_Character.name.Question.says._female_Character.mood.query.get.map(_.head ==>
        ("Kim", "What's going on?", "furious"))

      _ <- male_Character.name.Answer.says._male_Character.mood.query.get.map(_.head ==>
        ("Jimmie", "It's all good, man", "good"))
    } yield ()
  }
}
