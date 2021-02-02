package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds

trait A_0_0[o0[_], p0] extends A_[p0] with Api_0_00[o0, p0, A_0_0, A_0_1, D01, D02]

trait A_0_0_L0[o0[_], p0] extends A_0_0[o0, p0] {



  final lazy val int     : OneInt      [A_0_1_L0[o0, p0 with A_int    , Int              ], D01[o0,_,_]] with A_0_1_L0[o0, p0 with A_int    , Int              ] with Indexed = ???
  final lazy val str     : OneString   [A_0_1_L0[o0, p0 with A_str    , String           ], D01[o0,_,_]] with A_0_1_L0[o0, p0 with A_str    , String           ] with Indexed = ???
  final lazy val refB    : OneRefAttr  [A_0_1_L0[o0, p0 with A_refB   , Long             ], D01[o0,_,_]] with A_0_1_L0[o0, p0 with A_refB   , Long             ] with Indexed = ???
  final lazy val refsBB  : ManyRefAttr [A_0_1_L0[o0, p0 with A_refsBB , Set[Long]        ], D01[o0,_,_]] with A_0_1_L0[o0, p0 with A_refsBB , Set[Long]        ] with Indexed = ???

  final lazy val int$    : OneInt$     [A_0_1_L0[o0, p0 with A_int$   , Option[Int]      ]          ] with A_0_1_L0[o0, p0 with A_int$   , Option[Int]      ] with Indexed = ???
  final lazy val str$    : OneString$  [A_0_1_L0[o0, p0 with A_str$   , Option[String]   ]          ] with A_0_1_L0[o0, p0 with A_str$   , Option[String]   ] with Indexed = ???
  final lazy val refB$   : OneRefAttr$ [A_0_1_L0[o0, p0 with A_refB$  , Option[Long]     ]          ] with A_0_1_L0[o0, p0 with A_refB$  , Option[Long]     ] with Indexed = ???
  final lazy val refsBB$ : ManyRefAttr$[A_0_1_L0[o0, p0 with A_refsBB$, Option[Set[Long]]]          ] with A_0_1_L0[o0, p0 with A_refsBB$, Option[Set[Long]]] with Indexed = ???

  final lazy val int_    : OneInt      [A_0_0_L0[o0, p0                                  ], D00[o0,_]  ] with A_0_0_L0[o0, p0                                  ] with Indexed = ???
  final lazy val str_    : OneString   [A_0_0_L0[o0, p0                                  ], D00[o0,_]  ] with A_0_0_L0[o0, p0                                  ] with Indexed = ???
  final lazy val refB_   : OneRefAttr  [A_0_0_L0[o0, p0                                  ], D00[o0,_]  ] with A_0_0_L0[o0, p0                                  ] with Indexed = ???
  final lazy val refsBB_ : ManyRefAttr [A_0_0_L0[o0, p0                                  ], D00[o0,_]  ] with A_0_0_L0[o0, p0                                  ] with Indexed = ???

  final def RefB   : OneRef [A_[p0], B_] with B_0_L1[o0, p0, A_RefB_  , Nothing] = ???
  final def RefsBB : ManyRef[A_[p0], B_] with B_0_L1[o0, p0, A_RefsBB_, Nothing] with Nested00[A_RefsBB_, p0, B_1, D02, D03, D04] = ???


}

trait A_0_0_L1[o0[_], p0, o1[_], p1] extends A_0_0[o0, p0 with o1[p1]] {



  final lazy val int     : OneInt      [A_0_1_L1[o0, p0, o1, p1 with A_int    , Int              ], D01[o0,_,_]] with A_0_1_L1[o0, p0, o1, p1 with A_int    , Int              ] with Indexed = ???
  final lazy val str     : OneString   [A_0_1_L1[o0, p0, o1, p1 with A_str    , String           ], D01[o0,_,_]] with A_0_1_L1[o0, p0, o1, p1 with A_str    , String           ] with Indexed = ???
  final lazy val refB    : OneRefAttr  [A_0_1_L1[o0, p0, o1, p1 with A_refB   , Long             ], D01[o0,_,_]] with A_0_1_L1[o0, p0, o1, p1 with A_refB   , Long             ] with Indexed = ???
  final lazy val refsBB  : ManyRefAttr [A_0_1_L1[o0, p0, o1, p1 with A_refsBB , Set[Long]        ], D01[o0,_,_]] with A_0_1_L1[o0, p0, o1, p1 with A_refsBB , Set[Long]        ] with Indexed = ???

  final lazy val int$    : OneInt$     [A_0_1_L1[o0, p0, o1, p1 with A_int$   , Option[Int]      ]          ] with A_0_1_L1[o0, p0, o1, p1 with A_int$   , Option[Int]      ] with Indexed = ???
  final lazy val str$    : OneString$  [A_0_1_L1[o0, p0, o1, p1 with A_str$   , Option[String]   ]          ] with A_0_1_L1[o0, p0, o1, p1 with A_str$   , Option[String]   ] with Indexed = ???
  final lazy val refB$   : OneRefAttr$ [A_0_1_L1[o0, p0, o1, p1 with A_refB$  , Option[Long]     ]          ] with A_0_1_L1[o0, p0, o1, p1 with A_refB$  , Option[Long]     ] with Indexed = ???
  final lazy val refsBB$ : ManyRefAttr$[A_0_1_L1[o0, p0, o1, p1 with A_refsBB$, Option[Set[Long]]]          ] with A_0_1_L1[o0, p0, o1, p1 with A_refsBB$, Option[Set[Long]]] with Indexed = ???

  final lazy val int_    : OneInt      [A_0_0_L1[o0, p0, o1, p1                                  ], D00[o0,_]  ] with A_0_0_L1[o0, p0, o1, p1                                  ] with Indexed = ???
  final lazy val str_    : OneString   [A_0_0_L1[o0, p0, o1, p1                                  ], D00[o0,_]  ] with A_0_0_L1[o0, p0, o1, p1                                  ] with Indexed = ???
  final lazy val refB_   : OneRefAttr  [A_0_0_L1[o0, p0, o1, p1                                  ], D00[o0,_]  ] with A_0_0_L1[o0, p0, o1, p1                                  ] with Indexed = ???
  final lazy val refsBB_ : ManyRefAttr [A_0_0_L1[o0, p0, o1, p1                                  ], D00[o0,_]  ] with A_0_0_L1[o0, p0, o1, p1                                  ] with Indexed = ???

  final def RefB   : OneRef [A_[p0], B_] with B_0_L2[o0, p0, o1, p1, A_RefB_  , Nothing] = ???
  final def RefsBB : ManyRef[A_[p0], B_] with B_0_L2[o0, p0, o1, p1, A_RefsBB_, Nothing] with Nested00[A_RefsBB_, p0 with o1[p1], B_1, D02, D03, D04] = ???



}

trait A_0_0_L2[o0[_], p0, o1[_], p1, o2[_], p2] extends A_0_0[o0, p0 with o1[p1 with o2[p2]]] {



  final lazy val int     : OneInt      [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_int    , Int      ], D01[o0,_,_]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_int    , Int      ] with Indexed = ???
  final lazy val str     : OneString   [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_str    , String   ], D01[o0,_,_]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_str    , String   ] with Indexed = ???
  final lazy val refB    : OneRefAttr  [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refB   , Long     ], D01[o0,_,_]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refB   , Long     ] with Indexed = ???
  final lazy val refsBB  : ManyRefAttr [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refsBB , Set[Long]], D01[o0,_,_]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refsBB , Set[Long]] with Indexed = ???

  final lazy val int$    : OneInt$     [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_int$   , Option[Int]      ]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_int$   , Option[Int]      ] with Indexed = ???
  final lazy val str$    : OneString$  [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_str$   , Option[String]   ]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_str$   , Option[String]   ] with Indexed = ???
  final lazy val refB$   : OneRefAttr$ [A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refB$  , Option[Long]     ]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refB$  , Option[Long]     ] with Indexed = ???
  final lazy val refsBB$ : ManyRefAttr$[A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refsBB$, Option[Set[Long]]]] with A_0_1_L2[o0, p0, o1, p1, o2, p2 with A_refsBB$, Option[Set[Long]]] with Indexed = ???

  final lazy val int_    : OneInt      [A_0_0_L2[o0, p0, o1, p1, o2, p2], D00[o0,_]] with A_0_0_L2[o0, p0, o1, p1, o2, p2] with Indexed = ???
  final lazy val str_    : OneString   [A_0_0_L2[o0, p0, o1, p1, o2, p2], D00[o0,_]] with A_0_0_L2[o0, p0, o1, p1, o2, p2] with Indexed = ???
  final lazy val refB_   : OneRefAttr  [A_0_0_L2[o0, p0, o1, p1, o2, p2], D00[o0,_]] with A_0_0_L2[o0, p0, o1, p1, o2, p2] with Indexed = ???
  final lazy val refsBB_ : ManyRefAttr [A_0_0_L2[o0, p0, o1, p1, o2, p2], D00[o0,_]] with A_0_0_L2[o0, p0, o1, p1, o2, p2] with Indexed = ???

//  final def RefB   : OneRef [A_, B_] with B_0_L3[o0, p0, o1, p1, o2, p2, A_RefB_  , Nothing] = ???
//  final def RefsBB : ManyRef[A_, B_] with B_0_L3[o0, p0, o1, p1, o2, p2, A_RefsBB_, Nothing] with Nested00[B_1, p0 with o1[p1 with o2[p2]], D02, D03, D04] = ???



}
