package app

import molecule.sql.h2.sync._
import app.domains.dsl.Person._
import app.domains.nested.dsl.Animal._
import app.domains.nested.schema.AnimalSchema_h2
import app.domains.schema.PersonSchema_h2

class Test_sync extends TestSetup {

  "person" - h2(PersonSchema_h2) { implicit conn =>
    Person.name("Bob").save.transact
    Person.name.query.get.head ==> "Bob"
  }

  "animal" - h2(AnimalSchema_h2) { implicit conn =>
    Animal.name("Rex").save.transact
    Animal.name.query.get.head ==> "Rex"
  }
}
