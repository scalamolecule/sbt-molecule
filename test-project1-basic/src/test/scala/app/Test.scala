package app

import java.sql.DriverManager
import app.domain.dsl.Bar.metadb.Bar_MetaDb_h2
import app.domain.dsl.Foo.metadb.Foo_MetaDb_h2
import molecule.db.core.api.MetaDb
import molecule.db.core.marshalling.JdbcProxy
import molecule.db.sql.core.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.sql.h2.sync.*
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
      implicit val conn: JdbcConn_JVM = getConn(Bar_MetaDb_h2)

      Person.name("Bob").age(42).tpe("x").save.i.transact
      Person.name.age.tpe.query.i.get ==> List(("Bob", 42, "x"))
    }

    "foo" - {
      import app.domain.dsl.Foo.*
      implicit val conn: JdbcConn_JVM = getConn( Foo_MetaDb_h2)

      Person.name("Liz").age(38).save.transact
      Person.name.age.query.get ==> List(("Liz", 38))

      // Pass enum value (is saved as String in the database)
      Person.favoriteColor(Color.BLUE).save.transact

      // Enum as string returned
      Person.favoriteColor.query.get ==> List("BLUE")
      Person.favoriteColor.query.get ==> List(Color.BLUE.toString)
    }
  }
}