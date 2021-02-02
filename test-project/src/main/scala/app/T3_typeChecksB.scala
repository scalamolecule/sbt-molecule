//package app
//
//trait T3_typeChecksB extends T2_propBuilderB {
//
//
//  // A_init --> A_0
//  val aa: A_0[A_int] = A.int
//  val ab: A_0[A_str]  = A.str
//  val ab: A_0[A_refB] = A.ref_b
//
//  val ac: A_0[A_int with A_str]             = A.int.str
//  val ac: A_0[A_int with A_str with A_refB] = A.int.str.ref_b
//
//
//  val a2a: B_1ref[A_RefB] = A.Ref_B
//
//  val a1c: B_1[Any, A_RefB, B_int1] = A.Ref_B.int1
//
//
//
//
//  val ac: B_1ref[A_int, A_RefB] = A.int.Ref_B
//
//  val ac: B_1[Any, A_int, B_int1] = A.int.Ref_B.int1
//
//  val ac: Any with A_int[B_int1]  = obj(A.int.Ref_B.int1)
//
//
//
//
//
//  val a2b: B_0ref[A_RefsBB] = A.Refs_BB
//
//  // A_init --> B_0
//  val a1c: B_1[Any, A_RefB, B_int1 with B_str1] = A.Ref_B.int1.str1
//
//
//  val a1c: B_1ref[A_int, A_RefB] = A.int.Ref_B
//
//  val a3: B_1[Any, A_RefB, B_int1] = A.Ref_B.int1
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
