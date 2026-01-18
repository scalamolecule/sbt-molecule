// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Foo.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Foo_mysql() extends Foo_ with MetaDb_mysql {

  override val schemaResourcePath: String = "db/schema/app/domain/Foo/Foo_mysql.sql"

  /** Indexes to lookup if entity names collide with db keyword */
  override val reservedEntities: IArray[Byte] = IArray(
    0
  )

  /** Indexes to lookup if attribute names collide with db keyword */
  override val reservedAttributes: IArray[Byte] = IArray(
    /* Person */   0, 1, 0
  )
}