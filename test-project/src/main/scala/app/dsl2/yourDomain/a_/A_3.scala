package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait A_3[o0[_], p0, A, B, C] extends A_OLD with Api_0_03[o0, p0, A_3, A_4, D04, D05, A, B, C]

trait A_3_L0[o0[_], p0, A, B, C] extends A_3[o0, p0, A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_4_L0[o0, p0 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with A_4_L0[o0, p0 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_3_L0[o0, p0          , A, B, C     ], D03[o0,_,_,_,_]  ] with A_3_L0[o0, p0          , A, B, C     ]

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

  final def RefB   : OneRef [A_OLD, B_] with B_3_L1[o0, p0, A_RefB_  , Nothing, A, B, C] = ???
  final def RefsBB : ManyRef[A_OLD, B_] with B_3_L1[o0, p0, A_RefsBB_, Nothing, A, B, C] with Nested03[A_RefsBB_, p0, B_4, D05, D06, D07, A, B, C] = ???

  final def Self: A_3[o0, p0, A, B, C] with SelfJoin = ???
}

trait A_3_L1[o0[_], p0, o1[_], p1, A, B, C] extends A_3[o0, p0 with o1[p1], A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_4_L1[o0, p0, o1, p1 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with A_4_L1[o0, p0, o1, p1 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_3_L1[o0, p0, o1, p1          , A, B, C     ], D03[o0,_,_,_,_]  ] with A_3_L1[o0, p0, o1, p1          , A, B, C     ]

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

  final def RefB   : OneRef [A_OLD, B_] with B_3_L2[o0, p0, o1, p1, A_RefB_  , Nothing, A, B, C] = ???
  final def RefsBB : ManyRef[A_OLD, B_] with B_3_L2[o0, p0, o1, p1, A_RefsBB_, Nothing, A, B, C] with Nested03[A_RefsBB_, p0 with o1[p1], B_4, D05, D06, D07, A, B, C] = ???


  final def Self: A_3[o0, p0 with o1[p1], A, B, C] with SelfJoin = ???
}

trait A_3_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B, C] extends A_3[o0, p0 with o1[p1 with o2[p2]], A, B, C] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[A_4_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, C, Tpe], D04[o0,_,_,_,_,_]] with A_4_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, C, Tpe]
  type Stay[Attr[_, _]           ] = Attr[A_3_L2[o0, p0, o1, p1, o2, p2          , A, B, C     ], D03[o0,_,_,_,_]  ] with A_3_L2[o0, p0, o1, p1, o2, p2          , A, B, C     ]

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

//  final def RefB   : OneRef [A_, B_] with B_3_L3[o0, p0, o1, p1, o2, p2, A_RefB_  , Nothing, A, B, C] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_3_L3[o0, p0, o1, p1, o2, p2, A_RefsBB_, Nothing, A, B, C] with Nested03[B_4, p0 with o1[p1 with o2[p2]], D05, D06, D07, A, B, C] = ???

  
  final def Self: A_3[o0, p0 with o1[p1 with o2[p2]], A, B, C] with SelfJoin = ???
}




