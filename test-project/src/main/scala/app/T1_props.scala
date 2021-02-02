package app

trait T1_props {

  trait Obj[props] {
    def getObj: props = ???
  }


  trait A_[o0[_], p0] extends Composite[o0, p0] {
    def A: p0 = ???
  }
  trait A_str {
    lazy val str: String = ???
  }
  trait A_int {
    lazy val int: Int = ???
  }
  trait A_refB {
    lazy val refB: Long = ???
  }

  trait A_RefB_[props] {
    def RefB: props = ???
  }
  trait A_RefC_[props] {
    def RefC: props = ???
  }

  trait A_RefsBB[props] {
    def RefsBB: props = ???
  }


  trait B_[o0[_], p0] extends Composite[o0, p0] {
    def B: p0 = ???
  }
  trait B_str1 {
    lazy val str1: String = ???
  }
  trait B_int1 {
    lazy val int1: Int = ???
  }

  trait B_RefC[props] {
    def RefC: props = ???
  }


  trait C_[o0[_], p0] extends Composite[o0, p0] {
    def C: p0 = ???
  }
  trait C_str2 {
    lazy val str2: String = ???
  }
  trait C_int2 {
    lazy val int2: Int = ???
  }


trait Composite[o0[_], p0] extends Obj[p0] {
  def +[o1[_], p](next: Composite[o1, p]): Composite[o0, p0 with o1[p]] = ???
}

  trait Nested[o0[_], p0] {
    //    def *[p](nested: Obj[p]): A_1[o0, p0, o1, Seq[p]] = ???
  }
}
