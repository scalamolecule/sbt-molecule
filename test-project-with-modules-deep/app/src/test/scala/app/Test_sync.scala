package app

import app.domains.dataModel.dsl.Person._
import app.domains.dataModel.schema.PersonSchema
import app.domains.nested.dataModel.dsl.Animal._
import app.domains.nested.dataModel.schema.AnimalSchema
import molecule.sql.h2.sync._
import utest._


object Test_sync extends TestSuite with Connection {

  override lazy val tests = Tests {

    "person" - h2(PersonSchema) { implicit conn =>
      Person.name("Bob").save.transact
      Person.name.query.get.head ==> "Bob"
    }
    "animal" - h2(AnimalSchema) { implicit conn =>
      Animal.name("Rex").save.transact
      Animal.name.query.get.head ==> "Rex"
    }
  }
}
