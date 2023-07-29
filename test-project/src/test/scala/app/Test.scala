package app

import app.dsl.PersonDataModel._
import app.schema._
import molecule.core.util.Executor._
import molecule.datalog.datomic.async._
import molecule.datalog.datomic.facade.DatomicPeer
import utest._

object Test extends TestSuite {

  def futConn = DatomicPeer.recreateDb(PersonDataModelSchema)

  override lazy val tests = Tests {

    "test" - {
      futConn.flatMap { implicit conn =>
        for {
          _ <- Person.name("Bob").age(42).save.transact
          _ <- Person.name.age.query.get.map(_ ==> List(("Bob", 42)))
        } yield ()
      }
    }
  }
}
