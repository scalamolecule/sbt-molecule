package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait C_0[o0[_], p0] extends C_ with Api_0_00[o0, p0, C_0, C_1, D01, D02]

trait C_0_L0[o0[_], p0] extends C_0[o0, p0] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_1_L0[o0, p0 with Prop, Tpe], D01[o0,_,_]] with C_1_L0[o0, p0 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_0_L0[o0, p0               ], D00[o0,_]  ] with C_0_L0[o0, p0               ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???







}

trait C_0_L1[o0[_], p0, o1[_], p1] extends C_0[o0, p0 with o1[p1]] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_1_L1[o0, p0, o1, p1 with Prop, Tpe], D02[o0,_,_,_]] with C_1_L1[o0, p0, o1, p1 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_0_L1[o0, p0, o1, p1               ], D01[o0,_,_]  ] with C_0_L1[o0, p0, o1, p1               ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_0_L0[o0, p0 with o1[p1]] = ???


}

trait C_0_L2[o0[_], p0, o1[_], p1, o2[_], p2] extends C_0[o0, p0 with o1[p1 with o2[p2]]] {
  type Next[Attr[_, _], Prop, Tpe] = Attr[C_1_L2[o0, p0, o1, p1, o2, p2 with Prop, Tpe], D02[o0,_,_,_]] with C_1_L2[o0, p0, o1, p1, o2, p2 with Prop, Tpe]
  type Stay[Attr[_, _]           ] = Attr[C_0_L2[o0, p0, o1, p1, o2, p2               ], D01[o0,_,_]  ] with C_0_L2[o0, p0, o1, p1, o2, p2               ]

  final lazy val int2    : Next[int2   , C_int2   , Int      ] = ???
  final lazy val str2    : Next[str2   , C_str2   , String   ] = ???



  final lazy val int2$   : Next[int2$  , C_int2$  , Option[Int]      ] = ???
  final lazy val str2$   : Next[str2$  , C_str2$  , Option[String]   ] = ???



  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_0_L1[o0, p0, o1, p1 with o2[p2]] = ???


}






