package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait A_2[o0[_], p0, A, B] extends A_OLD with Api_0_02[o0, p0, A_2, A_3, D03, D04, A, B]

trait A_2_L0[o0[_], p0, A, B] extends A_2[o0, p0, A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_3_L0[o0, p0 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with A_3_L0[o0, p0 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_2_L0[o0, p0          , A, B     ], D02[o0,_,_,_]  ] with A_2_L0[o0, p0          , A, B     ]

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

  final def RefB   : OneRef [A_OLD, B_] with B_2_L1[o0, p0, A_RefB_  , Nothing, A, B] = ???
  final def RefsBB : ManyRef[A_OLD, B_] with B_2_L1[o0, p0, A_RefsBB_, Nothing, A, B] with Nested02[A_RefsBB_, p0, B_3, D04, D05, D06, A, B] = ???

  final def Self: A_2[o0, p0, A, B] with SelfJoin = ???
}

trait A_2_L1[o0[_], p0, o1[_], p1, A, B] extends A_2[o0, p0 with o1[p1], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with A_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_2_L1[o0, p0, o1, p1          , A, B     ], D02[o0,_,_,_]  ] with A_2_L1[o0, p0, o1, p1          , A, B     ]

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

  final def RefB   : OneRef [A_OLD, B_] with B_2_L2[o0, p0, o1, p1, A_RefB_  , Nothing, A, B] = ???
  final def RefsBB : ManyRef[A_OLD, B_] with B_2_L2[o0, p0, o1, p1, A_RefsBB_, Nothing, A, B] with Nested02[A_RefsBB_, p0 with o1[p1], B_3, D04, D05, D06, A, B] = ???


  final def Self: A_2[o0, p0 with o1[p1], A, B] with SelfJoin = ???
}

trait A_2_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B] extends A_2[o0, p0 with o1[p1 with o2[p2]], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with A_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ], D02[o0,_,_,_]  ] with A_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ]

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

//  final def RefB   : OneRef [A_, B_] with B_2_L3[o0, p0, o1, p1, o2, p2, A_RefB_  , Nothing, A, B] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_2_L3[o0, p0, o1, p1, o2, p2, A_RefsBB_, Nothing, A, B] with Nested02[B_3, p0 with o1[p1 with o2[p2]], D04, D05, D06, A, B] = ???


  final def Self: A_2[o0, p0 with o1[p1 with o2[p2]], A, B] with SelfJoin = ???
}

