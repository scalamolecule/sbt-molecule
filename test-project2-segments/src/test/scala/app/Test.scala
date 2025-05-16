package app

import java.sql.DriverManager
import app.domain.dsl.Person.*
import app.domain.schema.PersonSchema_h2
import molecule.db.core.marshalling.JdbcProxy
import molecule.db.sql.core.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.sql.h2.sync.*
import utest.*

object Test extends TestSuite {

  implicit val conn: JdbcConn_JVM = {
    val url = "jdbc:h2:mem:test"
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, PersonSchema_h2)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }


  override def tests: Tests = Tests {

    "test" - {
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
}