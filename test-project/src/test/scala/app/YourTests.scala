package app

import app.dsl.YourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer
import utest._
import scala.concurrent.ExecutionContext.Implicits.global


object YourTests extends TestSuite {



  lazy val tests = Tests {

    "app" - {
      YourApp.findLisa.map(_ ==> ("Lisa", 27, "female"))
    }


    "test" - {
      implicit val futConn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

      for {
        _ <- Person.name("John").age(25).gender("male").save
        _ <- Person.name.age.gender.get.map(_ ==> List(("John", 25, "male")))
      } yield ()
    }
  }
}
