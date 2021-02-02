package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait B_2[o0[_], p0, A, B] extends B_ with Api_0_02[o0, p0, B_2, B_3, D03, D04, A, B]

trait B_2_L0[o0[_], p0, A, B] extends B_2[o0, p0, A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_3_L0[o0, p0 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with B_3_L0[o0, p0 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_2_L0[o0, p0          , A, B     ], D02[o0,_,_,_]  ] with B_2_L0[o0, p0          , A, B     ]

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

  final def RefC   : OneRef [B_, C_] with C_2_L1[o0, p0, B_RefC_  , Nothing, A, B] = ???
  final def RefsCC : ManyRef[B_, C_] with C_2_L1[o0, p0, B_RefsCC_, Nothing, A, B] with Nested02[B_RefsCC_, p0, C_3, D04, D05, D06, A, B] = ???

  final def Self: B_2[o0, p0, A, B] with SelfJoin = ???
}

trait B_2_L1[o0[_], p0, o1[_], p1, A, B] extends B_2[o0, p0 with o1[p1], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with B_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_2_L1[o0, p0, o1, p1          , A, B     ], D02[o0,_,_,_]  ] with B_2_L1[o0, p0, o1, p1          , A, B     ]

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

  final def RefC   : OneRef [B_, C_] with C_2_L2[o0, p0, o1, p1, B_RefC_  , Nothing, A, B] = ???
  final def RefsCC : ManyRef[B_, C_] with C_2_L2[o0, p0, o1, p1, B_RefsCC_, Nothing, A, B] with Nested02[B_RefsCC_, p0 with o1[p1], C_3, D04, D05, D06, A, B] = ???
  final def _A     : A_2_L0[o0, p0 with o1[p1], A, B] = ???

  final def Self: B_2[o0, p0 with o1[p1], A, B] with SelfJoin = ???
}

trait B_2_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B] extends B_2[o0, p0 with o1[p1 with o2[p2]], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with B_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ], D02[o0,_,_,_]  ] with B_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ]

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

//  final def RefC   : OneRef [B_, C_] with C_2_L3[o0, p0, o1, p1, o2, p2, B_RefC_  , Nothing, A, B] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_2_L3[o0, p0, o1, p1, o2, p2, B_RefsCC_, Nothing, A, B] with Nested02[C_3, p0 with o1[p1 with o2[p2]], D04, D05, D06, A, B] = ???
  final def _A     : A_2_L1[o0, p0, o1, p1 with o2[p2], A, B] = ???

  final def Self: B_2[o0, p0 with o1[p1 with o2[p2]], A, B] with SelfJoin = ???
}

