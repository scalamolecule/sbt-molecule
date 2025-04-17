package app

import app.domain.dsl.Person._
import molecule.core.util.Executor._
import molecule.sql.h2.async._


class Test_async extends TestSetup {

  "test" - h2 { implicit conn =>
    for {
      _ <- Person.name("Bob").age(42).save.transact
      _ <- Person.name.age.query.get.map(_ ==> List(("Bob", 42)))
    } yield ()
  }
}
