package app

import java.sql.DriverManager
import app.domain.schema.*
import molecule.base.api.*
import molecule.core.marshalling.JdbcProxy
import molecule.db.sql.core.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.sql.h2.sync.*
import munit.FunSuite

class Test extends FunSuite {

  def getConn(schema: Schema): JdbcConn_JVM = {
    val url     = "jdbc:h2:mem:" + schema.getClass.getSimpleName
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, schema)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }


  test("bar") {
    import app.domain.dsl.Bar.*
    implicit val conn: JdbcConn_JVM = getConn(BarSchema_h2)

    Person.name("Bob").age(42).save.transact
    assertEquals(Person.name.age.query.get, List(("Bob", 42)))
  }


  test("foo") {
    import app.domain.dsl.Foo.*
    implicit val conn: JdbcConn_JVM = getConn(FooSchema_h2)

    Person.name("Liz").age(38).save.transact
    assertEquals(Person.name.age.query.get, List(("Liz", 38)))
  }
}
