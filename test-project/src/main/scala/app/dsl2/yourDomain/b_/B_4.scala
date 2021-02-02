package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait B_4[o0[_], p0, A, B, C, D] extends B_ with Api_0_04[o0, p0, B_4, D05, D05, D06, A, B, C, D]

trait B_4_L0[o0[_], p0, A, B, C, D] extends B_4[o0, p0, A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[B_4_L0[o0, p0, A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with B_4_L0[o0, p0          , A, B, C, D     ]











  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

  final def RefC   : OneRef [B_, C_] with C_4_L1[o0, p0, B_RefC_  , Nothing, A, B, C, D] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_4_L1[o0, p0, B_RefsCC_, Nothing, A, B, C, D] with Nested04[p0, o0, B_RefsCC_, C_4, D05, D06, D07, A, B, C, D] = ???

  final def Self: B_4[o0, p0, A, B, C, D] with SelfJoin = ???
}

trait B_4_L1[o0[_], p0, o1[_], p1, A, B, C, D] extends B_4[o0, p0 with o1[p1], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[B_4_L1[o0, p0, o1, p1          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with B_4_L1[o0, p0, o1, p1          , A, B, C, D     ]











  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

  final def RefC   : OneRef [B_, C_] with C_4_L2[o0, p0, o1, p1, B_RefC_  , Nothing, A, B, C, D] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_4_L2[o0, p0, o1, p1, B_RefsCC_, Nothing, A, B, C, D] with Nested04[p0 with o1[p1], o0, B_RefsCC_, C_4, D05, D06, D07, A, B, C, D] = ???
  final def _A     : A_4_L0[o0, p0 with o1[p1], A, B, C, D] = ???

  final def Self: B_4[o0, p0 with o1[p1], A, B, C, D] with SelfJoin = ???
}

trait B_4_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B, C, D] extends B_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[B_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with B_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ]











  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

//  final def RefC   : OneRef [B_, C_] with C_4_L3[o0, p0, o1, p1, o2, p2, B_RefC_  , Nothing, A, B, C, D] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_4_L3[o0, p0, o1, p1, o2, p2, B_RefsCC_, Nothing, A, B, C, D] with Nested04[p0 with o1[p1 with o2[p2]], o0, C_4, D05, D06, D07, A, B, C, D] = ???
  final def _A     : A_4_L1[o0, p0, o1, p1 with o2[p2], A, B, C, D] = ???

  final def Self: B_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] with SelfJoin = ???
}


