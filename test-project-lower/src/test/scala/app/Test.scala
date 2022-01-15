package app

import app.dsl.Person._
import app.schema.PersonSchema
import molecule.core.util.Executor._
import molecule.datomic.api._
import molecule.datomic.peer.facade.Datomic_Peer
import utest._


object Test extends TestSuite {

  implicit val conn = Datomic_Peer.recreateDbFrom(PersonSchema)

  lazy val tests = Tests {
    "test" - {
      for {
        _ <- person.name("Bob").age(42).save
        _ <- person.name_("Bob").age.get.map(_.head ==> 42)
      } yield ()
    }
  }
}
