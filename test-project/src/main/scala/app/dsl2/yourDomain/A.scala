package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.base.FirstNS
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds


object A extends A_0_0_L0[A_, Nothing] with FirstNS {
  final override def apply(eid: Long, eids: Long*): A_0_0_L0[A_, Nothing] = ???
  final override def apply(eids: Iterable[Long])  : A_0_0_L0[A_, Nothing] = ???
}

trait A_OLD {
  final class int    [Ns, In] extends OneInt     [Ns, In] with Indexed
  final class str    [Ns, In] extends OneString  [Ns, In] with Indexed
  final class refB   [Ns, In] extends OneRefAttr [Ns, In] with Indexed
  final class refsBB [Ns, In] extends ManyRefAttr[Ns, In] with Indexed

  final class int$   [Ns, In] extends OneInt$     [Ns] with Indexed
  final class str$   [Ns, In] extends OneString$  [Ns] with Indexed
  final class refB$  [Ns, In] extends OneRefAttr$ [Ns] with Indexed
  final class refsBB$[Ns, In] extends ManyRefAttr$[Ns] with Indexed
}


// Object interface

trait A_[props] { def A: props = ??? }

trait A_int     { lazy val int    : Int       = ??? }
trait A_str     { lazy val str    : String    = ??? }
trait A_refB    { lazy val refB   : Long      = ??? }
trait A_refsBB  { lazy val refsBB : Set[Long] = ??? }

trait A_int$    { lazy val int$   : Option[Int]       = ??? }
trait A_str$    { lazy val str$   : Option[String]    = ??? }
trait A_refB$   { lazy val refB$  : Option[Long]      = ??? }
trait A_refsBB$ { lazy val refsBB$: Option[Set[Long]] = ??? }

trait A_RefB_  [props] { def RefB  : props = ??? }
trait A_RefsBB_[props] { def RefsBB: props = ??? }
