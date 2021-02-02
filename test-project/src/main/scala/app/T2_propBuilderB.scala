//package app
//
//trait T2_propBuilderB extends T1_props {
//
//
//  object A extends A_init
//
//  trait A_init {
//    val int  : A_0[A_int]   = ???
//    val str  : A_0[A_str]  = ???
//    val ref_b: A_0[A_refB] = ???
//
//    def Ref_B: B_1ref[A_RefB] = ???
//    def Ref_C: C_1ref[A_RefC] = ???
//
//    def Refs_BB: B_0ref[A_RefsBB] = ???
//  }
//
//  trait A_0[p0] extends Obj[p0] {
//    val int  : A_0[p0 with A_int] = ???
//    val str  : A_0[p0 with A_str]  = ???
//    val ref_b: A_0[p0 with A_refB] = ???
//
//    def Ref_B: B_1ref[p0, A_RefB] = ???
//    def Ref_C: C_1[p0, A_RefC, Nothing] = ???
//
//    def Refs_BB: B_1[p0, A_RefsBB, Nothing] = ???
//  }
//
//
//  object B extends B_init
//
//  trait B_[p0] extends Obj[p0]
//
//  trait B_init {
//    val int1: B_0[B_int1] = ???
//    val str1: B_0[B_str1] = ???
//    def Ref_C: C_0ref[B_RefC] = ???
//    //      def +[p](next: Obj[p]):
//  }
//  trait B_0ref[o0[_]] {
//    val int1: B_1[o0, B_int1] = ???
//    val str1: B_[B_str1]  = ???
//    def Ref_C: C_0ref[B_RefC] = ???
//    //      def +[p](next: Obj[p]):
//  }
//  trait B_0[p0] extends B_[p0] {
//    val int1: B_0[p0 with B_int1] = ???
//    val str1: B_0[p0 with B_str1] = ???
//    def Ref_C: C_0ref[B_RefC] = ???
//    //      def +[p](next: Obj[p]):
//  }
//
//  trait B_1ref[o0[_]] {
//    val int1: B_1[o0, o1, B_int1] = ???
//    val str1: B_1[o0, o1, B_str1] = ???
//
//    def Ref_C: C_2[p0, o1, p1, B_RefC, Nothing] = ???
//
//    //    def *[p](nested: Obj[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    //    def *[T[_] <: Ref1_[_], p](nested: T[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    def *[T[_] <: B_[_], p](nested: T[_]): B_1[p0, o1, Seq[p]] = ???
//
//    def _A: A_0[p0 with o1[p1]] = ???
//  }
//
//  trait B_1ref[o1[_]] {
//    val int1: B_1[p0, o1, B_int1] = ???
//    val str1: B_1[p0, o1, B_str1] = ???
//
//    def Ref_C: C_2[p0, o1, p1, B_RefC, Nothing] = ???
//
//    //    def *[p](nested: Obj[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    //    def *[T[_] <: Ref1_[_], p](nested: T[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    def *[T[_] <: B_[_], p](nested: T[_]): B_1[p0, o1, Seq[p]] = ???
//
//    def _A: A_0[p0 with o1[p1]] = ???
//  }
//
//  trait B_1[p0, o1[_], p1] extends B_[p0 with o1[p1]] {
//    val int1: B_1[p0, o1, p1 with B_int1] = ???
//    val str1: B_1[p0, o1, p1 with B_str1] = ???
//
//    def Ref_C: C_2[p0, o1, p1, B_RefC, Nothing] = ???
//
//    //    def *[p](nested: Obj[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    //    def *[T[_] <: Ref1_[_], p](nested: T[p]): Ref1_1[p0, o1, Seq[p]] = ???
//    def *[T[_] <: B_[_], p](nested: T[_]): B_1[p0, o1, Seq[p]] = ???
//
//    def _A: A_0[p0 with o1[p1]] = ???
//  }
//
//
//  object C extends C_init
//
//  trait C_[p0] extends Obj[p0]
//
//  trait C_init {
//    val int2: C_0[C_int2] = ???
//    val str2: C_0[C_str2] = ???
//  }
//  trait C_0ref[o1[_]] {
//    val int2: C_0[C_int2] = ???
//    val str2: C_0[C_str2] = ???
//  }
//  trait C_0[p0] extends C_[p0] {
//    val int2: C_0[p0 with C_int2] = ???
//    val str2: C_0[p0 with C_str2] = ???
//  }
//  trait C_1[p0, o1[_], p1] extends C_[p0 with o1[p1]] {
//    val int2: C_1[p0, o1, p1 with C_int2] = ???
//    val str2: C_1[p0, o1, p1 with C_str2] = ???
//    //      def _Ref1: Ref1_1[p0, o1[p1]] = ???
//  }
//  trait C_2[p0, o1[_], p1, o2[_], p2] extends C_[p0 with o1[p1 with o2[p2]]] {
//    val int2: C_2[p0, o1, p1, o2, p2 with C_int2] = ???
//    val str2: C_2[p0, o1, p1, o2, p2 with C_str2] = ???
//
//    def *[p](nested: C_[p]): B_1[p0, o1, Seq[p]] = ???
//
//
//    def _B: B_1[p0, o1, p1 with o2[p2]] = ???
//  }
//
//
//  //  trait Ref3_1[p0, o1[_], p1] extends Obj[p0 with o1[p1]] {
//  //    val int3: Ref3_1[p0, o1, p1 with Ref2_int2] = ???
//  //    val str3: Ref3_1[p0, o1, p1 with Ref2_str2] = ???
//  //    //      def _Ref1: Ref1_1[p0, o1[p1]] = ???
//  //  }
//
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
