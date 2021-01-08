package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out3._
import molecule.datomic.peer.facade.Datomic_Peer

object YourApp {


  def jimmieSays: String = {
    // Make db
    implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)
    male_character.name("Jimmie").Answer.says("It's all good, man").save
    male_character.Answer.says.get.head
  }
}
