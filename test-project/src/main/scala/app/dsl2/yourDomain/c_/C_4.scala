package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait C_4[o0[_], p0, A, B, C, D] extends C_ with Api_0_04[o0, p0, C_4, D05, D05, D06, A, B, C, D]

trait C_4_L0[o0[_], p0, A, B, C, D] extends C_4[o0, p0, A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[C_4_L0[o0, p0, A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with C_4_L0[o0, p0          , A, B, C, D     ]











  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???






  final def Self: C_4[o0, p0, A, B, C, D] with SelfJoin = ???
}

trait C_4_L1[o0[_], p0, o1[_], p1, A, B, C, D] extends C_4[o0, p0 with o1[p1], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[C_4_L1[o0, p0, o1, p1          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with C_4_L1[o0, p0, o1, p1          , A, B, C, D     ]











  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???





  final def _B     : B_4_L0[o0, p0 with o1[p1], A, B, C, D] = ???

  final def Self: C_4[o0, p0 with o1[p1], A, B, C, D] with SelfJoin = ???
}

trait C_4_L2[o0[_], p0, o1[_], p1, o2[_], p2, A, B, C, D] extends C_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] {

  type Stay[Attr[_, _]           ] = Attr[C_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ], D04[o0,_,_,_,_,_]  ] with C_4_L2[o0, p0, o1, p1, o2, p2          , A, B, C, D     ]












  final lazy val int2_   : Stay[int2  ] = ???
  final lazy val str2_   : Stay[str2  ] = ???




  final def _B     : B_4_L1[o0, p0, o1, p1 with o2[p2], A, B, C, D] = ???

  final def Self: C_4[o0, p0 with o1[p1 with o2[p2]], A, B, C, D] with SelfJoin = ???
}





