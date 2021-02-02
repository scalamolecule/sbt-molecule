package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait B_3[o0[_], p0, A, B, C] extends B_ with Api_0_03[o0, p0, B_3, B_4, D04, D05, A, B, C]

trait B_3_L0[o0[_], p0, A, B, C] extends B_3[o0, p0, A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_4_L0[o0, p0 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with B_4_L0[o0, p0 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_3_L0[o0, p0          , A, B, C     ], D03[o0,_,_,_,_]  ] with B_3_L0[o0, p0          , A, B, C     ]

  final lazy val int1    : Next[int1   , B_int1   , Int      ] = ???
  final lazy val str1    : Next[str1   , B_str1   , String   ] = ???
  final lazy val refC    : Next[refC   , B_refC   , Long     ] = ???
  final lazy val refsCC  : Next[refsCC , B_refsCC , Set[Long]] = ???

  final lazy val int1$   : Next[int1$  , B_int1$  , Option[Int]      ] = ???
  final lazy val str1$   : Next[str1$  , B_str1$  , Option[String]   ] = ???
  final lazy val refC$   : Next[refC$  , B_refC$  , Option[Long]     ] = ???
  final lazy val refsCC$ : Next[refsCC$, B_refsCC$, Option[Set[Long]]] = ???

  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

  final def RefC   : OneRef [B_, C_] with C_3_L1[o0, p0, B_RefC_  , Nothing, A, B, C] = ???
  final def RefsCC : ManyRef[B_, C_] with C_3_L1[o0, p0, B_RefsCC_, Nothing, A, B, C] with Nested03[B_RefsCC_, p0, C_4, D05, D06, D07, A, B, C] = ???

  final def Self: B_3[o0, p0, A, B, C] with SelfJoin = ???
}

trait B_3_L1[o0[_], p0, o1[_], p1, A, B, C] extends B_3[o0, p0 with o1[p1], A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_4_L1[o0, p0, o1, p1 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with B_4_L1[o0, p0, o1, p1 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_3_L1[o0, p0, o1, p1          , A, B, C     ], D03[o0,_,_,_,_]  ] with B_3_L1[o0, p0, o1, p1          , A, B, C     ]

  final lazy val int1    : Next[int1   , B_int1   , Int      ] = ???
  final lazy val str1    : Next[str1   , B_str1   , String   ] = ???
  final lazy val refC    : Next[refC   , B_refC   , Long     ] = ???
  final lazy val refsCC  : Next[refsCC , B_refsCC , Set[Long]] = ???

  final lazy val int1$   : Next[int1$  , B_int1$  , Option[Int]      ] = ???
  final lazy val str1$   : Next[str1$  , B_str1$  , Option[String]   ] = ???
  final lazy val refC$   : Next[refC$  , B_refC$  , Option[Long]     ] = ???
  final lazy val refsCC$ : Next[refsCC$, B_refsCC$, Option[Set[Long]]] = ???

  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

  final def RefC   : OneRef [B_, C_] with C_3_L2[o0, p0, o1, p1, B_RefC_  , Nothing, A, B, C] = ???
  final def RefsCC : ManyRef[B_, C_] with C_3_L2[o0, p0, o1, p1, B_RefsCC_, Nothing, A, B, C] with Nested03[B_RefsCC_, p0 with o1[p1], C_4, D05, D06, D07, A, B, C] = ???
  final def _A     : A_3_L0[o0, p0 with o1[p1], A, B, C] = ???

  final def Self: B_3[o0, p0 with o1[p1], A, B, C] with SelfJoin = ???
}

trait B_3_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B, C] extends B_3[o0, p0 with o1[p1 with o2[p2]], A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_4_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with B_4_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_3_L2[o0, p0, o1, p1, o2, p2          , A, B, C     ], D03[o0,_,_,_,_]  ] with B_3_L2[o0, p0, o1, p1, o2, p2          , A, B, C     ]

  final lazy val int1    : Next[int1   , B_int1   , Int      ] = ???
  final lazy val str1    : Next[str1   , B_str1   , String   ] = ???
  final lazy val refC    : Next[refC   , B_refC   , Long     ] = ???
  final lazy val refsCC  : Next[refsCC , B_refsCC , Set[Long]] = ???

  final lazy val int1$   : Next[int1$  , B_int1$  , Option[Int]      ] = ???
  final lazy val str1$   : Next[str1$  , B_str1$  , Option[String]   ] = ???
  final lazy val refC$   : Next[refC$  , B_refC$  , Option[Long]     ] = ???
  final lazy val refsCC$ : Next[refsCC$, B_refsCC$, Option[Set[Long]]] = ???

  final lazy val int1_   : Stay[int1  ] = ???
  final lazy val str1_   : Stay[str1  ] = ???
  final lazy val refC_   : Stay[refC  ] = ???
  final lazy val refsCC_ : Stay[refsCC] = ???

//  final def RefC   : OneRef [B_, C_] with C_3_L3[o0, p0, o1, p1, o2, p2, B_RefC_  , Nothing, A, B, C] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_3_L3[o0, p0, o1, p1, o2, p2, B_RefsCC_, Nothing, A, B, C] with Nested03[C_4, p0 with o1[p1 with o2[p2]], D05, D06, D07, A, B, C] = ???
  final def _A     : A_3_L1[o0, p0, o1, p1 with o2[p2], A, B, C] = ???

  final def Self: B_3[o0, p0 with o1[p1 with o2[p2]], A, B, C] with SelfJoin = ???
}




