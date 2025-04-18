package app

import java.sql.DriverManager
import app.domain.schema.PersonSchema_h2
import molecule.core.marshalling.JdbcProxy
import molecule.core.spi.Conn
import molecule.sql.core.facade.JdbcHandler_JVM
import munit.FunSuite
import scala.util.Random
import scala.util.Using.Manager

trait TestSetup extends FunSuite {

  def h2[T](test: Conn => T): T = {
    val url = "jdbc:h2:mem:test" + Random.nextInt().abs
    Class.forName("org.h2.Driver") // Explicitly load the driver
    Manager { use =>
      val proxy   = JdbcProxy(url, PersonSchema_h2)
      val sqlConn = use(DriverManager.getConnection(proxy.url))
      val conn    = use(JdbcHandler_JVM.recreateDb(proxy, sqlConn))
      test(conn)
    }.get
  }


  // Some helper functions to make tests simpler

  implicit class TestableString(s: String) {
    def -(x: => Any): Unit = test(s)(x)
  }

  implicit class ArrowAssert(lhs: Any) {
    def ==>[V](rhs: V): Unit = assertEquals(lhs, rhs)
  }
}
