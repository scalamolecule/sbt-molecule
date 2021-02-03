package app

import app.dsl.yourDomain._
import app.schema._
import molecule.datomic.api.out5._
import molecule.datomic.peer.facade.Datomic_Peer


object YourApp extends App {


  // Connect and recreate database
  // Since the in-memory db is new each time, we need to transact the schema on each run
  implicit val conn = Datomic_Peer.recreateDbFrom(YourDomainSchema)


    val o0 = m(A.int_.str).getObjList.head
//    o0.int
    o0.str

    val o1 = B.int1.str1.getObjList.head
    o1.int1
    o1.str1
  //
  //
  //  // card-one --------------------------------------------------
  //
  //  val o2 = A.RefB.int1.getObjList.head
  //  o2.RefB.int1
  //
  //  {
  //    val o = A.int.RefB.int1.getObjList.head
  //    o.int
  //    o.RefB.int1
  //  }
  //  {
  //    val o = A.int.str.RefB.int1.getObjList.head
  //    o.int
  //    o.str
  //    o.RefB.int1
  //  }
  //  {
  //    val o = A.int.RefB.int1.str1.getObjList.head
  //    o.int
  //    o.RefB.int1
  //    o.RefB.str1
  //  }
  //  {
  //    val o = A.int.str.RefB.int1.str1.getObjList.head
  //    o.int
  //    o.str
  //    o.RefB.int1
  //    o.RefB.str1
  //  }
  //
  //  {
  //    val o2 = A.int.RefB.int1._A.str.getObjList.head
  //    o2.int
  //    o2.RefB.int1
  //    o2.str
  //  }
  //
  //  {
  //    val o3 = A.int.RefB.RefC.int2._B._A.str.RefB.str1.getObjList.head
  //    o3.int
  //    o3.RefB.RefC.int2
  //    o3.str
  //    //    o3.RefB.str1 // Can't revisit... todo or reject?
  //  }
  //  {
  //    val o3x = A.int.RefB.str1.RefC.int2._B._A.str.getObjList.head
  //    o3x.int
  //    o3x.RefB.str1
  //    o3x.RefB.RefC.int2
  //    o3x.str
  //  }
  //
  //  // card-many flat --------------------------------------------------
  //
  //  val o4 = A.int.RefsBB.int1.getObjList.head
  //  o4.int
  //  o4.RefsBB.int1
  //
  //  // card-many nested -----------------------------------------------
  //  {
  //    val o5 = m(A.int.RefsBB.*(B.int1)).getObjList.head
  //    o5.int
  //    o5.RefsBB.head.int1
  //  }
  //
  //  //  val x: z1[Int]
  //
  //  {
  //    val o5 = A.int.RefsBB.*(B.int1.str1).getObjList.head
  //    o5.int
  //    o5.RefsBB.head.int1
  //    o5.RefsBB.head.str1
  //  }
  //
//    // card-many nested
//    val o6 = A.int.RefsBB.*(B.int1.str1.RefC.str2).getObjList.head
//    o6.int
//    o6.RefsBB.head.int1
//    o6.RefsBB.head.str1
//    o6.RefsBB.head.RefC.str2
//
//    val o7 = A.int.RefsBB.*(A.int.str).getObjList.head
//    o7.int
//    o7.RefsBB.head.int
//    o7.RefsBB.head.str
//
//  val o6a = A.int.RefsBB.*(B.int1.RefsCC.*(C.str2)).getObjList.head
//  o6a.int
//  o6a.RefsBB.head.int1
//  o6a.RefsBB.head.RefsCC.head.str2
  //
  //
  //  // Composite -------------------------------------------------
  //
  //  //  // todo: have compiler prevent associated Ns to clash with Ref name
  //  //  // todo: prevent associations to already referenced namespaces - use ref in those cases!
  //  //  val o81 = A.int.+(B.int1)
  //  //  val o8c = A.int.+(B.int1).getObj
  //  //  o8c.int
  //  //  o8c.B.int1
  //
  //
  //  val o93 = B.int1.+(A.int).getObjList.head
  //  o93.B.int1
  //  o93.A.int
  //
  //  val o9a = A.int.+(B.int1.RefC.int2).getObjList.head
  //  o9a.A.int
  //  o9a.B.int1
  //  o9a.B.RefC.int2
  //
  //
  //  val o10 = A.int.+(B.int1).+(C.int2).getObjList.head
  //  o10.A.int
  //  o10.B.int1
  //  o10.C.int2
  //
  //  // shouldn't compile
  //  //  val o11 = C.int2.+(A.int.+(B.int1)).getProps
  //
  //  val o12 = B.int1.RefC.int2.+(A.int).getObjList.head
  //  o12.B.int1
  //  o12.B.RefC.int2
  //  o12.A.int
  //
  //
  //  // Tx meta data ------------------------------------------------
  //
  //  val o13 = A.int.Tx(C.int2).getObjList.head
  //  o13.int
  //  o13.Tx.C.int2
  //
  //  val o14 = A.int.Tx(B.int1.RefC.int2).getObjList.head
  //  o14.int
  //  o14.Tx.B.int1
  //  o14.Tx.B.RefC.int2
  //
  //  // flat card-many ok
  //  val o15 = A.int.Tx(B.int1.RefsCC.int2).getObjList.head
  //  o15.int
  //  o15.Tx.B.int1
  //  o15.Tx.B.RefsCC.int2
  //
  //  // todo: have compiler disallow nested tx meta data
  //  //  val o16x = A.int.Tx(B.int1.RefsCC.*(C.int2)).getObjList.head
  //  //  o16x.int
  //  //  o16x.Tx.B.int1
  //  //  o16x.Tx.B.RefsCC.int2
  //
  //  // Composite tx meta data
  //  val o16 = A.int.Tx(C.int2.str2 + B.int1.str1).getObjList.head
  //  o16.int
  //  o16.Tx.C.int2
  //  o16.Tx.C.str2
  //  o16.Tx.B.int1
  //  o16.Tx.B.str1


  // Self -----------------------------------

  //  val o17: Any = A.int.Self.int.getObjList.head


//  trait Nav[This, Next]
//
//  trait OneString[A, B] { self: Nav[A, B] =>
//
//    def <(other: String): A
//  }
//
//  trait AA {
//    val x: Nav[this.type, Int] with OneString
//  }


}
