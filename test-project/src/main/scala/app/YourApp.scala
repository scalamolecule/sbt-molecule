package app

import java.io.StringReader
import java.util.{List => jList}
import java.util.UUID.randomUUID
import app.dsl.YourDomain._
import app.schema._
import molecule.core.data.SchemaTransaction
import molecule.datomic.api.out3._
import molecule.datomic.base.facade.exception.DatomicFacadeException
import molecule.datomic.peer.facade.Datomic_Peer.{connect, createDatabase, deleteDatabase}
import molecule.datomic.peer.facade.{Conn_Peer, Datomic_Peer}


object YourApp {

  def recreateDbFrom(
    schema: Seq[String],
    dbIdentifier: String = "",
    protocol: String = "mem"
  ): Conn_Peer = {
    val id = if (dbIdentifier == "") randomUUID().toString else dbIdentifier
    try {
      deleteDatabase(id, protocol)
      createDatabase(id, protocol)
      val conn = connect(id, protocol)
      //      if (schema.partitions.size() > 0) {
      //        conn.transact(schema.partitions)
      //      }
      //
      //      conn.transact(schema.namespaces)

      schema.foreach { edn =>
        conn.peerConn.transact(
          datomic.Util.readAll(new StringReader(edn)).get(0).asInstanceOf[jList[_]]
        ).get
      }


      conn
    } catch {
      case e: Throwable => throw new DatomicFacadeException(e.toString)
    }
  }

  def findLisa: (String, Int, String) = {

    // Make in-mem db
    implicit val conn = recreateDbFrom(YourDomainSchema.datomicPeer)

    // Save Lisa
    Person.name("Lisa").age(27).gender("female").save.eid

    // Retrieve Lisa
    Person.name.age.gender.get.head
  }
}
