package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait C_1[o0[_], p0, A] extends C_ with Api_0_01[o0, p0, C_1, C_2, D02, D03, A]

trait C_1_L0[o0[_], p0, A] extends C_1[o0, p0, A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_2_L0[o0, p0 with Prop, A, Tpe], D02[o0,_,_,_]] with C_2_L0[o0, p0 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_1_L0[o0, p0          , A     ], D01[o0,_,_]  ] with C_1_L0[o0, p0          , A     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???






  final def Self: C_1[o0, p0, A] with SelfJoin = ???
}

trait C_1_L1[o0[_], p0, o1[_], p1, A] extends C_1[o0, p0 with o1[p1], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_2_L1[o0, p0, o1, p1 with Prop, A, Tpe], D02[o0,_,_,_]] with C_2_L1[o0, p0, o1, p1 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_1_L1[o0, p0, o1, p1          , A     ], D01[o0,_,_]  ] with C_1_L1[o0, p0, o1, p1          , A     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_1_L0[o0, p0 with o1[p1], A] = ???

  final def Self: C_1[o0, p0 with o1[p1], A] with SelfJoin = ???
}

trait C_1_L2[o0[_], p0, o1[_], p1, o2[_], p2, A] extends C_1[o0, p0 with o1[p1 with o2[p2]], A] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe], D02[o0,_,_,_]] with C_2_L2[o0, p0, o1, p1, o2, p2 with Prop, A, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_1_L2[o0, p0, o1, p1, o2, p2          , A     ], D01[o0,_,_]  ] with C_1_L2[o0, p0, o1, p1, o2, p2          , A     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_1_L1[o0, p0, o1, p1 with o2[p2], A] = ???

  final def Self: C_1[o0, p0 with o1[p1 with o2[p2]], A] with SelfJoin = ???
}






