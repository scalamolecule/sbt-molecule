package app

import molecule.core.util.Executor._
import molecule.datomic.api._
import molecule.datomic.peer.facade.Datomic_Peer
import utest._
import app.domains.dsl.Order._
import app.domains.schema.OrderSchema
import app.domains.nested.dsl.Animal._
import app.domains.nested.schema.AnimalSchema

object Test extends TestSuite {


  lazy val tests = Tests {

    "order" - {
      implicit val conn = Datomic_Peer.recreateDbFrom(OrderSchema)
      for {
        _ <- Order.id("abc").save
        _ <- Order.id.get.map(_.head ==> "abc")
      } yield ()
    }

    "person" - {
      implicit val conn = Datomic_Peer.recreateDbFrom(AnimalSchema)
      for {
        _ <- Animal.name("bob").save
        _ <- Animal.name.get.map(_.head ==> "bob")
      } yield ()
    }
  }
}
