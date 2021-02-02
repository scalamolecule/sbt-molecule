package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait A_4[o0[_], p0, A, B, C, D] extends A_OLD with Api_0_04[o0, p0, A_4, D05, D05, D06, A, B, C, D]

trait A_4_L0[o0[_], p0, A, B, C, D] extends A_4[o0, p0, A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[A_4_L0[o0, p0, A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with A_4_L0[o0, p0          , A, B, C, D     ]











  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

  final def RefB   : OneRef [A_OLD, B_] with B_4_L1[o0, p0, A_RefB_  , Nothing, A, B, C, D] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_4_L1[o0, p0, A_RefsBB_, Nothing, A, B, C, D] with Nested04[p0, o0, A_RefsBB_, B_4, D05, D06, D07, A, B, C, D] = ???

  final def Self: A_4[o0, p0, A, B, C, D] with SelfJoin = ???
}

trait A_4_L1[o0[_], p0, o1[_], p1, A, B, C, D] extends A_4[o0, p0 with o1[p1], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[A_4_L1[o0, p0, o1, p1          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with A_4_L1[o0, p0, o1, p1          , A, B, C, D     ]











  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

  final def RefB   : OneRef [A_OLD, B_] with B_4_L2[o0, p0, o1, p1, A_RefB_  , Nothing, A, B, C, D] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_4_L2[o0, p0, o1, p1, A_RefsBB_, Nothing, A, B, C, D] with Nested04[p0 with o1[p1], o0, A_RefsBB_, B_4, D05, D06, D07, A, B, C, D] = ???


  final def Self: A_4[o0, p0 with o1[p1], A, B, C, D] with SelfJoin = ???
}

trait A_4_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B, C, D] extends A_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[A_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with A_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ]











  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

//  final def RefB   : OneRef [A_, B_] with B_4_L3[o0, p0, o1, p1, o2, p2, A_RefB_  , Nothing, A, B, C, D] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_4_L3[o0, p0, o1, p1, o2, p2, A_RefsBB_, Nothing, A, B, C, D] with Nested04[p0 with o1[p1 with o2[p2]], o0, B_4, D05, D06, D07, A, B, C, D] = ???


  final def Self: A_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] with SelfJoin = ???
}

