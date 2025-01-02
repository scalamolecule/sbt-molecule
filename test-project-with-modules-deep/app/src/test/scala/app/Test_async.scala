package app

import app.domains.dsl.Person._
import app.domains.nested.dsl.Animal._
import app.domains.nested.schema.AnimalSchema_h2
import app.domains.schema.PersonSchema_h2
import molecule.core.util.Executor._
import molecule.sql.h2.async._


class Test_async extends TestSetup {

  "person" - h2(PersonSchema_h2) { implicit conn =>
    for {
      _ <- Person.name("Bob").save.transact
      _ <- Person.name.query.get.map(_.head ==> "Bob")
    } yield ()
  }

  "animal" - h2(AnimalSchema_h2) { implicit conn =>
    for {
      _ <- Animal.name("Rex").save.transact
      _ <- Animal.name.query.get.map(_.head ==> "Rex")
    } yield ()
  }
}
