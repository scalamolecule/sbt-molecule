package app

import java.sql.DriverManager
import app.Test.getConn
import app.domain.dsl.Bar.metadb.Bar_h2
import app.domain.dsl.Foo.metadb.Foo_h2
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
      given JdbcConn_JVM = getConn(Bar_h2())

      Person.name("Bob").age(42).save.transact
      Person.name.age.query.get ==> List(("Bob", 42))
    }

    "foo" - {
      import app.domain.dsl.Foo.*
      given JdbcConn_JVM = getConn(Foo_h2())

      Person.name("Liz").age(38).save.transact
      Person.name.age.query.get ==> List(("Liz", 38))
    }
  }
}



