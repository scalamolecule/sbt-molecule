package app

import app.domains.dsl.Person._
import app.domains.nested.dsl.Animal._
import app.domains.nested.schema.AnimalSchema
import app.domains.schema.PersonSchema
import molecule.sql.h2.sync._
import utest._


object Test_sync extends TestSuite with Connection {

  override lazy val tests = Tests {

    "person" - h2(PersonSchema) { implicit conn =>
      Person.name("Bob").save.transact
      Person.name.query.get.head ==> "Bob"
    }
    "animal" - h2((AnimalSchema)) { implicit conn =>
      Animal.name("Rex").save.transact
      Animal.name.query.get.head ==> "Rex"
    }
  }
}
