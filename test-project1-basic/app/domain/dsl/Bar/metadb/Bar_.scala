// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Bar.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.MetaDb

trait Bar_ extends MetaDb {

  /** entity -> List[mandatory-attribute] */
  val mandatoryAttrs: Map[String, List[String]] = Map()

  /** entity -> List[(entity.attr, mandatory refEntity)] */
  val mandatoryRefs: Map[String, List[(String, String)]] = Map()

  /** attr -> (Value, Scala type, required attributes) */
  val attrData: Map[String, (Value, String, List[String])] = Map(
    "Person.id"   -> (OneValue, "ID"    , List()),
    "Person.name" -> (OneValue, "String", List()),
    "Person.age"  -> (OneValue, "Int"   , List())
  )

  /** Attributes requiring unique values */
  val uniqueAttrs: List[String] = List()


  // Access control -------------------------------------------------------

  /** Role name to bit index (0-31) */
  override val roleIndex: Map[String, Int] = Map.empty[String, Int]

  /** Bitmask of roles that have query action */
  override val roleQueryAction: Int = 0

  /** Bitmask of roles that have save action */
  override val roleSaveAction: Int = 0

  /** Bitmask of roles that have insert action */
  override val roleInsertAction: Int = 0

  /** Bitmask of roles that have update action */
  override val roleUpdateAction: Int = 0

  /** Bitmask of roles that have delete action */
  override val roleDeleteAction: Int = 0

  /** Bitmask of roles that have rawQuery action */
  override val roleRawQueryAction: Int = 0

  /** Bitmask of roles that have rawTransact action */
  override val roleRawTransactAction: Int = 0

  /** Bitwise role access for entities on query action */
  override val queryAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on query action */
  override val queryAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on save action */
  override val saveAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on save action */
  override val saveAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on insert action */
  override val insertAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on insert action */
  override val insertAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on update action */
  override val updateAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on update action */
  override val updateAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on delete action */
  override val deleteAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on delete action */
  override val deleteAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on rawQuery action */
  override val rawQueryAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on rawQuery action */
  override val rawQueryAccessAttributes: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for entities on rawTransact action */
  override val rawTransactAccessEntities: IArray[Int] = IArray.empty[Int]

  /** Bitwise role access for attributes on rawTransact action */
  override val rawTransactAccessAttributes: IArray[Int] = IArray.empty[Int]
}