package app.dsl2.yourDomain

import molecule.core._2_dsl.boilerplate.api._
import molecule.core._2_dsl.boilerplate.attributes._
import molecule.core._2_dsl.boilerplate.base.FirstNS
import molecule.core._2_dsl.boilerplate.dummyTypes.{D01, D02, _}
import molecule.core._2_dsl.composition.nested._
import scala.language.higherKinds


object C extends C_0_L0[C_Obj, Nothing] with FirstNS {
  final override def apply(eid: Long, eids: Long*): C_0_L0[C_Obj, Nothing] = ???
  final override def apply(eids: Iterable[Long])  : C_0_L0[C_Obj, Nothing] = ???
}

trait C_ {
  final class int2   [Ns, In] extends OneInt     [Ns, In] with Indexed
  final class str2   [Ns, In] extends OneString  [Ns, In] with Indexed



  final class int2$  [Ns, In] extends OneInt$     [Ns] with Indexed
  final class str2$  [Ns, In] extends OneString$  [Ns] with Indexed


}



trait C_Obj[p0] {
  def C: p0 = ???
}

trait C_int2    { lazy val int2   : Int    = ??? }
trait C_str2    { lazy val str2   : String = ??? }



trait C_int2$   { lazy val int2$  : Option[Int]    = ??? }
trait C_str2$   { lazy val str2$  : Option[String] = ??? }


