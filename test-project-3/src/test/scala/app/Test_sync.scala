package app

import app.domain.dsl.Person._
import molecule.sql.h2.sync._


class Test_sync extends TestSetup {

  "test" - h2 { implicit conn =>
    Person.name("Bob").age(42).save.transact
    Person.name.age.query.get ==> List(("Bob", 42))
  }
}
