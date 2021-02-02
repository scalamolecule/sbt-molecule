package app

trait T2_propBuilderA extends T1_props {


  object A extends A_0[Nothing]

  trait A_0[p0] extends A_[A_0, p0] {
    val int : A_0[p0 with A_int]  = ???
    val str : A_0[p0 with A_str]  = ???
    val refB: A_0[p0 with A_refB] = ???

    def RefB: B_1[A_0, p0, A_RefB_, Nothing] = ???
    def RefC: C_1[A_0, p0, A_RefC_, Nothing] = ???

    def RefsBB: B_1[A_0, p0, A_RefsBB, Nothing] = ???
  }

  trait A_1[o0[_], p0, o1[_], p1] extends A_[o0, p0 with o1[p1]] {
    val int: A_1[o0, p0, o1, p1 with A_int] = ???
    val str: A_1[o0, p0, o1, p1 with A_str] = ???

    def *[p](nested: Obj[p]): A_1[o0, p0, o1, Seq[p]] = ???
    def _A: A_0[p0 with o1[p1]] = ???
  }


  object B extends B_0[Nothing]

  trait B_0[p0] extends B_[B_0, p0] {
    val int1: B_0[p0 with B_int1] = ???
    val str1: B_0[p0 with B_str1] = ???
    def RefC: C_1[B_0, p0, B_RefC, Nothing] = ???
  }


  trait B_1[o0[_], p0, o1[_], p1] extends B_[o0, p0 with o1[p1]] {
    val int1: B_1[o0, p0, o1, p1 with B_int1] = ???
    val str1: B_1[o0, p0, o1, p1 with B_str1] = ???

    def RefC: C_2[o0, p0, o1, p1, B_RefC, Nothing] = ???

    def *[p](nested: Obj[p]): B_1[o0, p0, o1, Seq[p]] = ???

    def _A: A_0[p0 with o1[p1]] = ???
  }


  object C extends C_0[Nothing]

  trait C_0[p0] extends C_[C_0, p0] {
    val int2: C_0[p0 with C_int2] = ???
    val str2: C_0[p0 with C_str2] = ???
  }

  trait C_1[o0[_], p0, o1[_], p1] extends C_[o0, p0 with o1[p1]] {
    val int2: C_1[o0, p0, o1, p1 with C_int2] = ???
    val str2: C_1[o0, p0, o1, p1 with C_str2] = ???
  }

  trait C_2[o0[_], p0, o1[_], p1, o2[_], p2] extends C_[o0, p0 with o1[p1 with o2[p2]]] {
    val int2: C_2[o0, p0, o1, p1, o2, p2 with C_int2] = ???
    val str2: C_2[o0, p0, o1, p1, o2, p2 with C_str2] = ???

//    def *[p](nested: C_[p]): B_1[o0, p0, o1, Seq[p]] = ???
    def _B: B_1[o0, p0, o1, p1 with o2[p2]] = ???
  }

  trait C_3[o0[_], p0, o1[_], p1, o2[_], p2, o3[_], p3] extends C_[o0, p0 with o1[p1 with o2[p2 with o3[p3]]]] {
    val int2: C_3[o0, p0, o1, p1, o2, p2, o3, p3 with C_int2] = ???
    val str2: C_3[o0, p0, o1, p1, o2, p2, o3, p3 with C_str2] = ???

//    def *[p](nested: C_[p]): B_1[o0, p0, o1, Seq[p]] = ???
    def _B: B_1[o0, p0, o1, p1 with o2[p2]] = ???
  }
}





























































