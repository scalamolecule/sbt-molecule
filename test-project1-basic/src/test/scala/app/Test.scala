package app

import java.sql.DriverManager
import app.domain.dsl.Bar.metadb.Bar_MetaDb_h2
import app.domain.dsl.Foo.metadb.Foo_MetaDb_h2
import molecule.db.common.api.MetaDb
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import utest.*

object Test extends TestSuite {

  def getConn(metaDb: MetaDb): JdbcConn_JVM = {
    val url = "jdbc:h2:mem:"
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, metaDb)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }


  override def tests: Tests = Tests {

    "bar" - {
      import app.domain.dsl.Bar.*
      given JdbcConn_JVM = getConn(Bar_MetaDb_h2())

      Person.name("Bob").age(42).save.i.transact
      Person.name.age.query.i.get ==> List(("Bob", 42))
    }

    "foo" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn(Foo_MetaDb_h2())

      Person.name("Liz").age(38).save.transact
      Person.name.age.query.get ==> List(("Liz", 38))
    }

    "types" - {
      import molecule.db.h2.async.*
      import molecule.db.common.util.Executor.*

      import app.domain.dsl.Bar.*
      given JdbcConn_JVM = getConn(Bar_MetaDb_h2())

      val chatRoomQuery = Person.name.query
      var chatRoomUI    = List.empty[String]
      for {
        _ <- Person.name("a").save.transact

        _ <- chatRoomQuery.subscribe { updatedStrings =>
          println("--- " + updatedStrings)
          chatRoomUI = updatedStrings
        }

        _ <- Person.name("b").save.transact
        _ <- Person.name("c").save.transact

        _ <- chatRoomQuery.unsubscribe()
        _ = chatRoomUI ==> List("a", "b", "c")

        _ <- Person.name("x").save.transact
        _ = chatRoomUI ==> List("a", "b", "c")
      } yield ()
    }
  }
}