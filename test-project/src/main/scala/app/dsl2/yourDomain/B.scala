package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.base.FirstNS
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds


object B extends B_0_L0[B_Obj, Nothing] with FirstNS {
  final override def apply(eid: Long, eids: Long*): B_0_L0[B_Obj, Nothing] = ???
  final override def apply(eids: Iterable[Long])  : B_0_L0[B_Obj, Nothing] = ???
}

trait B_ {
  final class int1   [Ns, In] extends OneInt     [Ns, In] with Indexed
  final class str1   [Ns, In] extends OneString  [Ns, In] with Indexed
  final class refC   [Ns, In] extends OneRefAttr [Ns, In] with Indexed
  final class refsCC [Ns, In] extends ManyRefAttr[Ns, In] with Indexed

  final class int1$  [Ns, In] extends OneInt$     [Ns] with Indexed
  final class str1$  [Ns, In] extends OneString$  [Ns] with Indexed
  final class refC$  [Ns, In] extends OneRefAttr$ [Ns] with Indexed
  final class refsCC$[Ns, In] extends ManyRefAttr$[Ns] with Indexed
}


trait B_Obj[p0] {
  def B: p0 = ???
}

trait B_int1    { lazy val int1   : Int       = ??? }
trait B_str1    { lazy val str1   : String    = ??? }
trait B_refC    { lazy val refC   : Long      = ??? }
trait B_refsCC  { lazy val refsCC : Set[Long] = ??? }

trait B_int1$   { lazy val int1$  : Option[Int]       = ??? }
trait B_str1$   { lazy val str1$  : Option[String]    = ??? }
trait B_refC$   { lazy val refC$  : Option[Long]      = ??? }
trait B_refsCC$ { lazy val refsCC$: Option[Set[Long]] = ??? }

trait B_RefC_  [props] { def RefC  : props = ??? }
trait B_RefsCC_[props] { def RefsCC: props = ??? }
