package app

import java.sql.DriverManager
import app.domain.schema.*
import molecule.db.core.api.Schema
import molecule.db.core.marshalling.JdbcProxy
import molecule.db.sql.core.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.sql.h2.sync.*
import utest.*

object Test extends TestSuite {

  def getConn(schema: Schema): JdbcConn_JVM = {
    val url = "jdbc:h2:mem:" + schema.getClass.getSimpleName
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, schema)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }


  override def tests: Tests = Tests {

    "bar" - {
      import app.domain.dsl.Bar.*
      implicit val conn: JdbcConn_JVM = getConn(BarSchema_h2)

      Person.name("Bob").age(42).save.transact
      Person.name.age.query.get ==> List(("Bob", 42))
    }

    "foo" - {
      import app.domain.dsl.Foo.*
      implicit val conn: JdbcConn_JVM = getConn(FooSchema_h2)

      Person.name("Liz").age(38).save.transact
      Person.name.age.query.get ==> List(("Liz", 38))
    }
  }
}