package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait B_1[o0[_], p0, A] extends B_ with Api_0_01[o0, p0, B_1, B_2, D02, D03, A]

trait B_1_L0[o0[_], p0, A] extends B_1[o0, p0, A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_2_L0[o0, p0 with Prop, A, Tpe], D02[o0,_,_,_]] with B_2_L0[o0, p0 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_1_L0[o0, p0          , A     ], D01[o0,_,_]  ] with B_1_L0[o0, p0          , A     ]

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

  final def RefC   : OneRef [B_, C_] with C_1_L1[o0, p0, B_RefC_  , Nothing, A] = ???
  final def RefsCC : ManyRef[B_, C_] with C_1_L1[o0, p0, B_RefsCC_, Nothing, A] with Nested01[B_RefsCC_, p0, C_2, D03, D04, D05, A] = ???

  final def Self: B_1[o0, p0, A] with SelfJoin = ???
}

trait B_1_L1[o0[_], p0, o1[_], p1, A] extends B_1[o0, p0 with o1[p1], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_2_L1[o0, p0, o1, p1 with Prop, A, Tpe], D02[o0,_,_,_]] with B_2_L1[o0, p0, o1, p1 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_1_L1[o0, p0, o1, p1          , A     ], D01[o0,_,_]  ] with B_1_L1[o0, p0, o1, p1          , A     ]

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

  final def RefC   : OneRef [B_, C_] with C_1_L2[o0, p0, o1, p1, B_RefC_  , Nothing, A] = ???
  final def RefsCC : ManyRef[B_, C_] with C_1_L2[o0, p0, o1, p1, B_RefsCC_, Nothing, A] with Nested01[B_RefsCC_, p0 with o1[p1], C_2, D03, D04, D05, A] = ???
  final def _A     : A_0_1_L0[o0, p0 with o1[p1], A] = ???

  final def Self: B_1[o0, p0 with o1[p1], A] with SelfJoin = ???
}

trait B_1_L2[o0[_], p0, o1[_], p1, o2[_], p2, A] extends B_1[o0, p0 with o1[p1 with o2[p2]], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe], D02[o0,_,_,_]] with B_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_1_L2[o0, p0, o1, p1, o2, p2          , A     ], D01[o0,_,_]  ] with B_1_L2[o0, p0, o1, p1, o2, p2          , A     ]

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

//  final def RefC   : OneRef [B_, C_] with C_1_L3[o0, p0, o1, p1, o2, p2, B_RefC_  , Nothing, A] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_1_L3[o0, p0, o1, p1, o2, p2, B_RefsCC_, Nothing, A] with Nested01[C_2, p0 with o1[p1 with o2[p2]], D03, D04, D05, A] = ???
  final def _A     : A_0_1_L1[o0, p0, o1, p1 with o2[p2], A] = ???

  final def Self: B_1[o0, p0 with o1[p1 with o2[p2]], A] with SelfJoin = ???
}

