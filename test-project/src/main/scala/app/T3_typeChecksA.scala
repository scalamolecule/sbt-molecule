package app

trait T3_typeChecksA extends T2_propBuilderA {


  // A_init --> A_0
  //  val aa: A_0[Nothing with A_int]  = A.int
  //  val ab: A_0[Nothing with A_str] = A.str
  //  val ab: A_0[Nothing with A_refB] = A.refB
//
//  val aa: A_int with A_str                        = A.int.str.getObj
//  val ac: A_RefB_[B_int1]                         = A.RefB.int1.getObj
//  val ab: A_int with A_RefB_[Nothing with B_int1] = A.int.RefB.int1.getObj

  val ba: Nothing with B_int1 with B_str1 = B.int1.str1.getObj
//  val bc: B_RefC_[B_int1]                         = A.RefB.int1.getObj
//  val bb: B_int1 with A_RefB_[Nothing with B_int1] = A.int.RefB.int1.getObj


  //  val ac: A_0[Nothing with A_int with A_str with A_refB] = A.int.str.refB
  //
  //  val ac: B_1[A_, Nothing with A_int, A_RefB, Nothing]             = A.int.RefB
  //  val ac: B_1[A_, Nothing with A_int, A_RefB, Nothing with B_int1] = A.int.RefB.int1
  //
  //  val a2a: B_1[A_, Nothing, A_RefB, Nothing]             = A.RefB
  //  val a1c: B_1[A_, Nothing, A_RefB, Nothing with B_int1] = A.RefB.int1
  //  val a2b: B_1[A_, Nothing, A_RefsBB, Nothing]           = A.RefsBB
  //
  //  // A_init --> B_0
  //  val a1c: B_1[A_, Nothing, A_RefB, Nothing with B_int1 with B_str1] = A.RefB.int1.str1
  //  val a1c: B_1[A_, Nothing with A_int, A_RefB, Nothing]              = A.int.RefB
  //  val a3 : B_1[A_, Nothing, A_RefB, Nothing with B_int1]             = A.RefB.int1
  //
  //  val a4a: Composite[A_, Nothing with A_int with B_[Nothing with B_int1 with B_RefC[Nothing with C_int2]]] = A.int.+(B.int1.RefC.int2)
  //  val a4b: Nothing with A_int with B_[Nothing with B_int1 with B_RefC[Nothing with C_int2]]                = A.int.+(B.int1.RefC.int2).getObj
  //
  //  val a5a: Composite[A_, Nothing with A_int with B_[Nothing with B_int1]] = A.int.+(B.int1)
  //  val a5b: Nothing with A_int with B_[Nothing with B_int1]                = A.int.+(B.int1).getObj
  //  val a5c: Nothing with A_int with B_[Nothing with B_int1]                = A.int.+(B.int1).getObj

}





























































