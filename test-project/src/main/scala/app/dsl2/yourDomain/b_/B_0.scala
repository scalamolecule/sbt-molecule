package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait B_0[o0[_], p0] extends B_ with Api_0_00[o0, p0, B_0, B_1, D01, D02]

trait B_0_L0[o0[_], p0] extends B_0[o0, p0] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_1_L0[o0, p0 with Prop, Tpe], D01[o0,_,_]] with B_1_L0[o0, p0 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_0_L0[o0, p0               ], D00[o0,_]  ] with B_0_L0[o0, p0               ]

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

  final def RefC   : OneRef [B_, C_] with C_0_L1[o0, p0, B_RefC_  , Nothing] = ???
  final def RefsCC : ManyRef[B_, C_] with C_0_L1[o0, p0, B_RefsCC_, Nothing] with Nested00[B_RefsCC_, p0, C_1, D02, D03, D04] = ???


}

trait B_0_L1[o0[_], p0, o1[_], p1] extends B_0[o0, p0 with o1[p1]] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_1_L1[o0, p0, o1, p1 with Prop, Tpe], D02[o0,_,_,_]] with B_1_L1[o0, p0, o1, p1 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_0_L1[o0, p0, o1, p1               ], D01[o0,_,_]  ] with B_0_L1[o0, p0, o1, p1               ]

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

  final def RefC   : OneRef [B_, C_] with C_0_L2[o0, p0, o1, p1, B_RefC_  , Nothing] = ???
  final def RefsCC : ManyRef[B_, C_] with C_0_L2[o0, p0, o1, p1, B_RefsCC_, Nothing] with Nested00[B_RefsCC_, p0 with o1[p1], C_1, D02, D03, D04] = ???
  final def _A     : A_0_0_L0[o0, p0 with o1[p1]] = ???


}

trait B_0_L2[o0[_], p0, o1[_], p1, o2[_], p2] extends B_0[o0, p0 with o1[p1 with o2[p2]]] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[B_1_L2[o0, p0, o1, p1, o2, p2 with Prop, Tpe], D02[o0,_,_,_]] with B_1_L2[o0, p0, o1, p1, o2, p2 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[B_0_L2[o0, p0, o1, p1, o2, p2               ], D01[o0,_,_]  ] with B_0_L2[o0, p0, o1, p1, o2, p2               ]

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

//  final def RefC   : OneRef [B_, C_] with C_0_L3[o0, p0, o1, p1, o2, p2, B_RefC_  , Nothing] = ???
//  final def RefsCC : ManyRef[B_, C_] with C_0_L3[o0, p0, o1, p1, o2, p2, B_RefsCC_, Nothing] with Nested00[C_1, p0 with o1[p1 with o2[p2]], D02, D03, D04] = ???
  final def _A     : A_0_0_L1[o0, p0, o1, p1 with o2[p2]] = ???


}



