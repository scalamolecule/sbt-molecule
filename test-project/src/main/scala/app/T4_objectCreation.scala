package app


trait T4_objectCreation extends T2_propBuilderA {


  val o = B.int1.str1.getObj
  o.int1
  o.str1

  val o0 = A.int.str.getObj
  o0.int
  o0.str


  val o1a = A.RefB.int1.getObj
  o1a.RefB.int1

  val o1 = A.int.str.RefB.int1.str1.getObj
  o1.int
  o1.str
  o1.RefB.int1
  o1.RefB.str1

  val o2 = A.int.RefB.int1._A.str.getObj
  o2.int
  o2.RefB.int1
  o2.str

  val o3 = A.int.RefB.int1.RefC.int2._B.str1._A.str.RefC.str2.getObj
  o3.int
  o3.RefB.int1
  o3.RefB.RefC.int2
  o3.RefB.str1
  o3.str
  o3.RefC.str2

  val o3x = A.int.RefB.str1.RefC.int2._B._A.str.getObj
  o3x.int
  o3x.RefB.str1
  o3x.RefB.RefC.int2
  o3x.str


  // card-many flat
  val o4 = A.int.RefsBB.int1.getObj
  o4.int
  o4.RefsBB.int1

  // card-many nested
  val o5 = A.int.RefsBB.*(B.int1.str1).getObj
  o5.int
  o5.RefsBB.head.int1
  o5.RefsBB.head.str1

  // card-many nested
  val o6 = A.int.RefsBB.*(B.int1.str1.RefC.str2).getObj
  o6.int
  o6.RefsBB.head.int1
  o6.RefsBB.head.str1
  o6.RefsBB.head.RefC.str2

  val o7 = A.int.RefsBB.*(A.int.str).getObj
  o7.int
  o7.RefsBB.head.int
  o7.RefsBB.head.str

  //  val o71 = A.int.RefB.int1.RefC.int2.*(A.int.str).getObj
  //  o71.int
  //  o71.RefsBB.head.int
  //  o71.RefsBB.head.str


  val o8a = A.int.RefB.int1.getObj
  o8a.int
  o8a.RefB.int1

  // todo: have compiler prevent associated Ns to clash with Ref name
  // todo: prevent associations to already referenced namespaces - use ref in those cases!
  val o81 = A.int.+(B.int1)
  val o8c = A.int.+(B.int1).getObj
  o8c.int
  o8c.B.int1


  val oa = A.int.RefB.int1.RefC.int2.getObj
  oa.int
  oa.RefB.int1
  oa.RefB.RefC.int2


  val o9a = A.int.+(B.int1.RefC.int2).getObj
  o9a.int
  o9a.B.int1
  o9a.B.RefC.int2


  val o10 = A.int.+(B.int1).+(C.int2).getObj
  o10.int
  o10.B.int1
  o10.C.int2

  // shouldn't compile
  //    val o11 = C.int2.+(A.int.+(B.int1)).getProps

  val o12 = B.int1.RefC.int2.+(A.int).getObj
  o12.int1
  o12.RefC.int2
  o12.A.int

}
