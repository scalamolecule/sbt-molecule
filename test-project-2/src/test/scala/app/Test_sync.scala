package app

import app.dsl.Person._
import molecule.sql.h2.sync._
import utest._


object Test_sync extends TestSuite with Connection {

  override lazy val tests = Tests {
    "test" - h2 { implicit conn =>
      Person.name("Bob").age(42).save.transact
      Person.name.age.query.get ==> List(("Bob", 42))
    }
  }
}
