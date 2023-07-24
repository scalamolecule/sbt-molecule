//package app
//
//import app.dataModel.dsl.PersonDataModel._
//import app.dataModel.schema.PersonDataModelSchema
//import molecule.db.datomic.facade.{Conn_Peer, Datomic_Peer}
//import utest._
//
//
//object Test extends TestSuite {
//
//  lazy val tests = Tests {
//    implicit val conn: Conn_Peer = Datomic_Peer.recreateDbFromEdn(PersonDataModelSchema.datomicSchema)
//    "test" - {
//      for {
//        _ <- conn
//
//
////        _ <- Person(42).age.inspectGet
//        _ <- Person.name.age_.inspectGet
////        _ <- Person.name.age_.get
////        _ <- Person.name.age_(12).get
////        _ <- Person.name.age_(12).save
//
////        _ = {
////
////          val a: Person.Next[Person.name, Person_name, String]                                                                                                                                                                                                                                         = Person.name
////          val b: (Person_1_0[Person_, base.Init with Person_age, Int] with Person.age[Person_1_0[Person_, base.Init with Person_age, Int]])#Next[(Person_1_0[Person_, base.Init with Person_age, Int] with Person.age[Person_1_0[Person_, base.Init with Person_age, Int]])#name, Person_name, String] = Person.age.name
////
////          val a1: Molecule_X.Molecule_01[base.Init with Person_name, String]                      = m(a)
////          val b1: Molecule_X.Molecule_02[base.Init with Person_name with Person_age, String, Int] = m(b)
////
////
////          //          val c: (Person.name[Person_1_0[Person_, base.Init with Person_name, String]] with Person_1_0[Person_, base.Init with Person_name, String])#Stay[(Person.name[Person_1_0[Person_, base.Init with Person_name, String]] with Person_1_0[Person_, base.Init with Person_name, String])#age_] = Person.name.age_
////          //
////          //          val c1: Molecule_X.Molecule_01[base.Init with Person_name, String] = m(c)
////          //
////          //
////          //          val d: Person.Next[Person.age, Person_age, Int] = Person.name_.age
////          //
////          //          val d1: Molecule_X.Molecule_01[base.Init with Person_age, Int] = m(d)
////        }
////
////
////        _ <- Person.name("Bob").save
////        _ <- Person.name("Bob").age(42).save
////        _ <- Person.name("Bob").age_(42).save
////        _ <- Person.name.apply("Bob").age_(42).save
////        _ <- Person.name_("Bob").age.get.map(_.head ==> 42)
////        _ <- {
////          Person.name("Bob").age.getObj.map { o =>
////            o.name ==> "Bob"
////            o.age ==> 42
////          }
////        }
//      } yield ()
//    }
//
////        "test" - {
////          implicit val conn = Datomic_Peer.recreateDbFrom(CoreTestSchema)
////          import app.dsl.CoreTest._
////
////          for {
////
////            _ <- conn
////
////            _ <- Ns.int.long_.inspectGet
////
////            List(a0, a1, b0, b1, c0, c1) <- Ns.str.Ref1.str1.insert(List(
////              ("a0", "a1"),
////              ("b0", "b1"),
////              ("c0", "c1")
////            )).map(_.eids)
////            _ <- Person.name("Bob").save
////            _ <- Person.name("Bob").age(42).save
////            _ <- m(Person.name("Bob").age(42)).save
////            _ <- Person.name_("Bob").age.get.map(_.head ==> 42)
////          } yield ()
////        }
//  }
//}
