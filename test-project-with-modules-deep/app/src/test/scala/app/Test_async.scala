package app

import app.domains.dataModel.dsl.Person._
import app.domains.dataModel.schema.PersonSchema
import app.domains.nested.dataModel.dsl.Animal._
import app.domains.nested.dataModel.schema.AnimalSchema
import molecule.core.util.Executor._
import molecule.sql.h2.async._
import utest._


object Test_async extends TestSuite with Connection {

  override lazy val tests = Tests {
    "person" - h2(PersonSchema) { implicit conn =>
      for {
        _ <- Person.name("Bob").save.transact
        _ <- Person.name.query.get.map(_.head ==> "Bob")
      } yield ()
    }
    "animal" - h2(AnimalSchema) { implicit conn =>
      for {
        _ <- Animal.name("Rex").save.transact
        _ <- Animal.name.query.get.map(_.head ==> "Rex")
      } yield ()
    }
  }
}
