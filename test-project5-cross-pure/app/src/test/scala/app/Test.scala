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
    val url     = "jdbc:h2:mem:test"
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, PersonSchema_h2)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }


  override def tests: Tests = Tests {

    "test" - {
      Person.name("Bob").age(42).save.transact
      Person.name.age.query.get ==> List(("Bob", 42))
    }
  }
}
