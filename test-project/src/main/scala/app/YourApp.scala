package app

import app.dsl.YourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object YourApp {

  def findLisa: Future[(String, Int, String)] = {

    // Make in-mem db
    implicit val futConn = Datomic_Peer.recreateDbFrom(YourDomainSchema)

    for {
      // Save Lisa
      _ <- Person.name("Lisa").age(27).gender("female").save

      // Retrieve Lisa
      res <- Person.name.age.gender.get.map(_.head)
    } yield res
  }
}
