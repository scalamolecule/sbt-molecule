package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait C_2[o0[_], p0, A, B] extends C_ with Api_0_02[o0, p0, C_2, C_3, D03, D04, A, B]

trait C_2_L0[o0[_], p0, A, B] extends C_2[o0, p0, A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_3_L0[o0, p0 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with C_3_L0[o0, p0 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_2_L0[o0, p0          , A, B     ], D02[o0,_,_,_]  ] with C_2_L0[o0, p0          , A, B     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???






  final def Self: C_2[o0, p0, A, B] with SelfJoin = ???
}

trait C_2_L1[o0[_], p0, o1[_], p1, A, B] extends C_2[o0, p0 with o1[p1], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with C_3_L1[o0, p0, o1, p1 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_2_L1[o0, p0, o1, p1          , A, B     ], D02[o0,_,_,_]  ] with C_2_L1[o0, p0, o1, p1          , A, B     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_2_L0[o0, p0 with o1[p1], A, B] = ???

  final def Self: C_2[o0, p0 with o1[p1], A, B] with SelfJoin = ???
}

trait C_2_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B] extends C_2[o0, p0 with o1[p1 with o2[p2]], A, B] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe], D03[o0,_,_,_,_]] with C_3_L2[o0, p0, o1, p1, o2, p2 with Prop, A, B, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ], D02[o0,_,_,_]  ] with C_2_L2[o0, p0, o1, p1, o2, p2          , A, B     ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_2_L1[o0, p0, o1, p1 with o2[p2], A, B] = ???

  final def Self: C_2[o0, p0 with o1[p1 with o2[p2]], A, B] with SelfJoin = ???
}



