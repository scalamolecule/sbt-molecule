package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait A_0_1[o0[_], p0, A] extends A_OLD with A_[p0] with Api_0_01[o0, p0, A_0_1, A_2, D02, D03, A]

trait A_0_1_L0[o0[_], p0, A] extends A_0_1[o0, p0, A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_2_L0[o0, p0 with Prop, A, Tpe], D02[o0,_,_,_]] with A_2_L0[o0, p0 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_0_1_L0[o0, p0          , A     ], D01[o0,_,_]  ] with A_0_1_L0[o0, p0          , A     ]

  final lazy val int     : Next[int    , A_int    , Int      ] = ???
  final lazy val str     : Next[str    , A_str    , String   ] = ???
  final lazy val refB    : Next[refB   , A_refB   , Long     ] = ???
  final lazy val refsBB  : Next[refsBB , A_refsBB , Set[Long]] = ???

  final lazy val int$    : Next[int$   , A_int$   , Option[Int]      ] = ???
  final lazy val str$    : Next[str$   , A_str$   , Option[String]   ] = ???
  final lazy val refB$   : Next[refB$  , A_refB$  , Option[Long]     ] = ???
  final lazy val refsBB$ : Next[refsBB$, A_refsBB$, Option[Set[Long]]] = ???

  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

  final def RefB   : OneRef [A_[p0], B_] with B_1_L1[o0, p0, A_RefB_  , Nothing, A] = ???
  final def RefsBB : ManyRef[A_[p0], B_] with B_1_L1[o0, p0, A_RefsBB_, Nothing, A] with Nested01[A_RefsBB_, p0, B_2, D03, D04, D05, A] = ???

  final def Self: A_0_1[o0, p0, A] with SelfJoin = ???
}

trait A_0_1_L1[o0[_], p0, o1[_], p1, A] extends A_0_1[o0, p0 with o1[p1], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_2_L1[o0, p0, o1, p1 with Prop, A, Tpe], D02[o0,_,_,_]] with A_2_L1[o0, p0, o1, p1 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_0_1_L1[o0, p0, o1, p1          , A     ], D01[o0,_,_]  ] with A_0_1_L1[o0, p0, o1, p1          , A     ]

  final lazy val int     : Next[int    , A_int    , Int      ] = ???
  final lazy val str     : Next[str    , A_str    , String   ] = ???
  final lazy val refB    : Next[refB   , A_refB   , Long     ] = ???
  final lazy val refsBB  : Next[refsBB , A_refsBB , Set[Long]] = ???

  final lazy val int$    : Next[int$   , A_int$   , Option[Int]      ] = ???
  final lazy val str$    : Next[str$   , A_str$   , Option[String]   ] = ???
  final lazy val refB$   : Next[refB$  , A_refB$  , Option[Long]     ] = ???
  final lazy val refsBB$ : Next[refsBB$, A_refsBB$, Option[Set[Long]]] = ???

  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

  final def RefB   : OneRef [A_[p0], B_] with B_1_L2[o0, p0, o1, p1, A_RefB_  , Nothing, A] = ???
  final def RefsBB : ManyRef[A_[p0], B_] with B_1_L2[o0, p0, o1, p1, A_RefsBB_, Nothing, A] with Nested01[A_RefsBB_, p0 with o1[p1], B_2, D03, D04, D05, A] = ???


  final def Self: A_0_1[o0, p0 with o1[p1], A] with SelfJoin = ???
}

trait A_0_1_L2[o0[_], p0, o1[_], p1, o2[_], p2, A] extends A_0_1[o0, p0 with o1[p1 with o2[p2]], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe], D02[o0,_,_,_]] with A_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_0_1_L2[o0, p0, o1, p1, o2, p2          , A     ], D01[o0,_,_]  ] with A_0_1_L2[o0, p0, o1, p1, o2, p2          , A     ]

  final lazy val int     : Next[int    , A_int    , Int      ] = ???
  final lazy val str     : Next[str    , A_str    , String   ] = ???
  final lazy val refB    : Next[refB   , A_refB   , Long     ] = ???
  final lazy val refsBB  : Next[refsBB , A_refsBB , Set[Long]] = ???

  final lazy val int$    : Next[int$   , A_int$   , Option[Int]      ] = ???
  final lazy val str$    : Next[str$   , A_str$   , Option[String]   ] = ???
  final lazy val refB$   : Next[refB$  , A_refB$  , Option[Long]     ] = ???
  final lazy val refsBB$ : Next[refsBB$, A_refsBB$, Option[Set[Long]]] = ???

  final lazy val int_    : Stay[int   ] = ???
  final lazy val str_    : Stay[str   ] = ???
  final lazy val refB_   : Stay[refB  ] = ???
  final lazy val refsBB_ : Stay[refsBB] = ???

//  final def RefB   : OneRef [A_, B_] with B_1_L3[o0, p0, o1, p1, o2, p2, A_RefB_  , Nothing, A] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_1_L3[o0, p0, o1, p1, o2, p2, A_RefsBB_, Nothing, A] with Nested01x[p0 with o1[p1 with o2[p2]] with A_RefsBB_, B_2, D03, D04, D05, A] = ???


  final def Self: A_0_1[o0, p0 with o1[p1 with o2[p2]], A] with SelfJoin = ???
}






