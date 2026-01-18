// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Bar

import molecule.core.dataModel.{AttrOneTacID as _AttrOneTacID, DataModel as _DataModel, Eq as _Eq, OptEntity as _OptEntity}
import molecule.db.common.api.*
import molecule.db.common.ops.ModelTransformations_.*


object Person extends ops.Person_0(_DataModel(Nil, firstEntityIndex = 0)) {
  final def apply(id: Long, ids: Long*) = new ops.Person_0(
    _DataModel(List(_AttrOneTacID("Person", "id", _Eq, id +: ids, coord = List(0, 0))), firstEntityIndex = 0)
  )
  final def apply(ids: Iterable[Long]) = new ops.Person_0(
    _DataModel(List(_AttrOneTacID("Person", "id", _Eq, ids.toSeq, coord = List(0, 0))), firstEntityIndex = 0)
  )
}
