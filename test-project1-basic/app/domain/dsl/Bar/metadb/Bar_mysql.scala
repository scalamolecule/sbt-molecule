// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Bar.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Bar_mysql() extends Bar_ with MetaDb_mysql {

  override val schemaResourcePath: String = "db/schema/app/domain/Bar/Bar_mysql.sql"

  /** Indexes to lookup if entity names collide with db keyword */
  override val reservedEntities: IArray[Byte] = IArray(
    0
  )

  /** Indexes to lookup if attribute names collide with db keyword */
  override val reservedAttributes: IArray[Byte] = IArray(
    /* Person */   0, 1, 0
  )
}