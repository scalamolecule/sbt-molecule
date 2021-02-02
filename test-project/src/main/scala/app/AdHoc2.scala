//package app
//
//object AdHoc2 extends App {
//
//
//  trait Ns_
//
//  trait Ns_str {
//    val str: String
//  }
//  trait Ns_int {
//    val int: Int
//  }
//  trait Ns_ref1 {
//    val ref1: Long
//  }
//
//  trait NoProp
//
//  trait Ns_Ref1_0 {
//    def Ref1: NoProp
//  }
//  trait Ns_Ref1_[a] {
//    def Ref1: a
//  }
//  trait Ns_Ref2_[a] {
//    def Ref2: a
//  }
//
//  trait Ns_Refs1_0
//  trait Ns_Refs1_flat_1[a] {
//    def Refs1: a
//  }
//  trait Ns_Refs1_flat_2[a, b] {
//    def Refs1: a with b
//  }
//  trait Ns_Refs1_nested_1[a] {
//    def Refs1: List[a]
//  }
//  trait Ns_Refs1_nested_2[a, b] {
//    def Refs1: List[a with b]
//  }
//
//  //  trait Ref1_Ref1_0
//  //  trait Ref1_Ref1_1[a] {
//  //    def Ref1: a
//  //  }
//  //  trait Ref1_Ref1_2[a, b] {
//  //    def Ref1: a with b
//  //  }
//
//  trait Ref1_str1 {
//    val str1: String
//  }
//  trait Ref1_int1 {
//    val int1: Int
//  }
//
//  trait Ref1_Ref2_0
//  trait Ref1_Ref2_[a] {
//    def Ref2: a
//  }
//  trait Ref1_Ref2_2[a, b] {
//    def Ref2: a with b
//  }
//
//  trait Ref1_Refs2_0
//  trait Ref1_Refs2_flat_1[a] {
//    def Refs2: a
//  }
//  trait Ref1_Refs2_flat_2[a, b] {
//    def Refs2: a with b
//  }
//  trait Ref1_Refs2_nested_1[a] {
//    def Refs2: List[a]
//  }
//  trait Ref1_Refs2_nested_2[a, b] {
//    def Refs2: List[a with b]
//  }
//
//
//  trait Ref2_0
//  trait Ref2_1[a] {
//    def Ref2: a
//  }
//  trait Ref2_2[a, b] {
//    def Ref2: a with b
//  }
//
//  trait Ref2_str2 {
//    val str2: String
//  }
//  trait Ref2_int2 {
//    val int2: Int
//  }
//
//
//  object attrs {
//
//    // tacit attrs not part of type build-up
//    // Ns.int._str
//    val o1 = new Ns_str {
//      val str = "a"
//    }
//    o1.str
//
//    // Ns.int.str
//    // (A, B)
//    // a with b
//    val o2 = new Ns_str with Ns_int {
//      val str = "a"
//      val int = 1
//    }
//    o2.str
//    o2.int
//
//    // Ns.int.ref1
//    val o3 = new Ns_str with Ns_ref1 {
//      val str  = "a"
//      val ref1 = 42L
//    }
//    o3.str
//    o3.ref1 // ref attribute
//  }
//
//
//  object cardOne {
//    // Ns.int.Ref1 or Ns.int.Ref1._int1
//    val o1 = new Ns_int with Ns_Ref1_0 {
//      val int = 1
//      def Ref1: NoProp = new NoProp {}
//    }
//    o1.int
//
//    // Ns.int.Ref1.int1
//    // (A, B)
//    // a with X[b]
//    val o2 = new Ns_int with Ns_Ref1_[Ref1_int1] {
//      val int = 1
//      def Ref1: Ref1_int1 = new Ref1_int1 {
//        val int1 = 11
//      }
//    }
//    o2.int
//    o2.Ref1.int1 // card-one
//
//    // Ns.int.Ref1.int1.str1
//    // (A, B, C)
//    // a with X[b, c]
//    val o3 = new Ns_int with Ns_Ref1_[Ref1_int1 with Ref1_str1] {
//      val int = 1
//      def Ref1: Ref1_int1 with Ref1_str1 = new Ref1_int1 with Ref1_str1 {
//        val int1 = 11
//        val str1 = "aa"
//      }
//    }
//    o3.int
//    o3.Ref1.int1 // card-one
//    o3.Ref1.str1
//
//    // Ns.int.Ref1.int1.str1.Ref2._int2
//    // (A, B, C)
//    // a with X[b, c]
//    val o4 = new Ns_int with Ns_Ref1_[Ref1_int1 with Ref1_str1] {
//      val int = 1
//      def Ref1: Ref1_int1 with Ref1_str1 = new Ref1_int1 with Ref1_str1 {
//        val int1 = 11
//        val str1 = "aa"
//      }
//    }
//    o4.int
//    o4.Ref1.int1 // card-one:
//    o4.Ref1.str1
//
//    // Ns.int.Ref1.int1.str1.Ref2.int2
//    // (A, B, C, D)
//    // a with X[b with c with Y[d]]
//    val o5 = new Ns_int with Ns_Ref1_[Ref1_int1 with Ref1_str1 with Ref1_Ref2_[Ref2_int2]] {
//      val int = 1
//      def Ref1: Ref1_int1 with Ref1_str1 with Ref1_Ref2_[Ref2_int2] = new Ref1_int1 with Ref1_str1 with Ref1_Ref2_[Ref2_int2] {
//        val int1 = 11
//        val str1 = "aa"
//        def Ref2: Ref2_int2 = new Ref2_int2 {
//          val int2 = 111
//        }
//      }
//    }
//    o5.int
//    o5.Ref1 // what about this??
//    o5.Ref1.int1 // card-one
//    o5.Ref1.str1
//    o5.Ref1.Ref2
//    o5.Ref1.Ref2.int2
//  }
//
//
//  object composite {
//    // Ns.int.+(Ref2._int2)
//    // A
//    // a
//    val o1 = new Ns_int with Ref2_0 {
//      val int = 1
//    }
//    o1.int
//
//    // Ns.int.+(Ref2.int2)
//    // (a, b)
//    // a with X[b]
//    val o2 = new Ns_int with Ref2_1[Ref2_int2] {
//      val int = 1
//      def Ref2: Ref2_int2 = new Ref2_int2 {
//        val int2 = 11
//      }
//    }
//    o2.int
//    o2.Ref2.int2 // composite - same as card-one
//
//    // Ns.int.Refs1.int1.str1
//    // (a, (b, c))
//    // a with X[b with c]
//    val o3 = new Ns_int with Ref2_2[Ref2_int2, Ref2_str2] {
//      val int = 1
//      def Ref2: Ref2_int2 with Ref2_str2 = new Ref2_int2 with Ref2_str2 {
//        val int2 = 11
//        val str2 = "aa"
//      }
//    }
//    o3.int
//    o3.Ref2.int2 // composite - same as card-one
//    o3.Ref2.str2
//  }
//
//
//  object cardManyFlat {
//    // Ns.int.Refs1 or Ns.int.Refs1._int1
//    val o1 = new Ns_int with Ns_Refs1_0 {
//      val int = 1
//    }
//    o1.int
//
//    // Ns.int.Refs1.int1
//    val o2 = new Ns_int with Ns_Refs1_flat_1[Ref1_int1] {
//      val int = 1
//      def Refs1: Ref1_int1 = new Ref1_int1 {
//        val int1 = 11
//      }
//    }
//    o2.int
//    o2.Refs1.int1 // card-many flat
//
//    // Ns.int.Refs1.int1.str1
//    val o3 = new Ns_int with Ns_Refs1_flat_2[Ref1_int1, Ref1_str1] {
//      val int = 1
//      def Refs1: Ref1_int1 with Ref1_str1 = new Ref1_int1 with Ref1_str1 {
//        val int1 = 11
//        val str1 = "aa"
//      }
//    }
//    o3.int
//    o3.Refs1.int1 // card-many flat
//    o3.Refs1.str1
//  }
//
//
//  object cardManyNested {
//    // Ns.int.Refs1
//    val o1 = new Ns_int with Ns_Refs1_0 {
//      val int = 1
//    }
//    o1.int
//
//    // Ns.int.Refs1.*(Ref1.int1)
//    val o2 = new Ns_int with Ns_Refs1_nested_1[Ref1_int1] {
//      val int = 1
//      def Refs1: List[Ref1_int1] = List(new Ref1_int1 {
//        val int1 = 11
//      })
//    }
//    o2.int
//    o2.Refs1.head.int1 // card-many nested
//
//    // Ns.int.Refs1.*(Ref1.int1.str1)
//    val o3 = new Ns_int with Ns_Refs1_nested_2[Ref1_int1, Ref1_str1] {
//      val int = 1
//      def Refs1: List[Ref1_int1 with Ref1_str1] = List(new Ref1_int1 with Ref1_str1 {
//        val int1 = 11
//        val str1 = "aa"
//      })
//    }
//    o3.int
//    o3.Refs1.head.int1 // card-many nested
//    o3.Refs1.head.str1
//  }
//
//
//  {
//    trait Obj[props]{
//      def getProps: props = ???
//    }
//
//
//    object Ns extends Ns_0[Nothing]
//
//    trait Ns_0[p0] extends Obj[p0] {
//      val int: Ns_0[p0 with Ns_int] = ???
//      val str: Ns_0[p0 with Ns_str] = ???
//
//      def Ref1: Ref1_1[p0, Ns_Ref1_, Nothing] = ???
//      def Ref2: Ref2_1[p0, Ns_Ref2_, Nothing] = ???
//
//      def Refs1: Ref1_1[p0, Ns_Refs1_flat_1, Nothing] = ???
//    }
//
//    object Ref1 extends Ref1_0[Nothing]
//
//    trait Ref1_0[p0] extends Obj[p0] {
//      val int1: Ref1_0[p0 with Ref1_int1] = ???
//      val str1: Ref1_0[p0 with Ref1_str1] = ???
//      def Ref2: Ref2_1[p0, Ref1_Ref2_, Nothing] = ???
////      def +[p](next: Obj[p]):
//    }
//    trait Ref1_1[p0, o1[_], p1] extends Obj[p0 with o1[p1]] {
//      val int1: Ref1_1[p0, o1, p1 with Ref1_int1] = ???
//      val str1: Ref1_1[p0, o1, p1 with Ref1_str1] = ???
//
//      def Ref2: Ref2_2[p0, o1, p1, Ref1_Ref2_, Nothing] = ???
//
//      def *[p](nested: Obj[p]): Ref1_1[p0, o1, Seq[p]] = ???
////      def *[p](nested: Obj[p]): Ref1_1[p0, o1, Seq[p]] = ???
//
//      def _Ns: Ns_0[p0 with o1[p1]] = ???
//    }
//
//
//    object Ref2 extends Ref2_0[Nothing]
//
//    trait Ref2_0[p0] extends Obj[p0] {
//      val int2: Ref2_0[p0 with Ref2_int2] = ???
//      val str2: Ref2_0[p0 with Ref2_str2] = ???
//    }
//    trait Ref2_1[p0, o1[_], p1] extends Obj[p0 with o1[p1]] {
//      val int2: Ref2_1[p0, o1, p1 with Ref2_int2] = ???
//      val str2: Ref2_1[p0, o1, p1 with Ref2_str2] = ???
////      def _Ref1: Ref1_1[p0, o1[p1]] = ???
//    }
//    trait Ref2_2[p0, o1[_], p1, o2[_], p2] extends Obj[p0 with o1[p1 with o2[p2]]] {
//      val int2: Ref2_2[p0, o1, p1, o2, p2 with Ref2_int2] = ???
//      val str2: Ref2_2[p0, o1, p1, o2, p2 with Ref2_str2] = ???
//      def _Ref1: Ref1_1[p0, o1, p1 with o2[p2]] = ???
//    }
//
//
//    def get[p0](obj: Obj[p0]): p0 = obj.getProps
//
//    val o1 = get(Ns.int.str.Ref1.int1.str1)
//    o1.int
//    o1.str
//    o1.Ref1.int1
//    o1.Ref1.str1
//
//    val o2 = get(Ns.int.Ref1.int1._Ns.str)
//    o2.int
//    o2.Ref1.int1
//    o2.str
//
//    val o3 = get(Ns.int.Ref1.int1.Ref2.int2._Ref1.str1._Ns.str.Ref2.str2)
//    o3.int
//    o3.Ref1.int1
//    o3.Ref1.Ref2.int2
//    o3.Ref1.str1
//    o3.str
//    o3.Ref2.str2
//
//
//    // card-many flat
//    val o4 = get(Ns.int.Refs1.int1)
//    o4.int
//    o4.Refs1.int1
//
//    // card-many nested
//    val o5 = get(Ns.int.Refs1.*(Ref1.int1.str1))
//    o5.int
//    o5. Refs1.head.int1
//    o5. Refs1.head.str1
//
//    // card-many nested
//    val o6 = get(Ns.int.Refs1.*(Ref1.int1.str1.Ref2.str2))
//    o6.int
//    o6.Refs1.head.int1
//    o6.Refs1.head.str1. Ref2.str2
//
//
//    val o7 = get(Ref1.int1.str1.Ref2.str2)
//    o7.int1
//    o7.str1
//    o7.Ref2.str2
//
//
//
//  }
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
