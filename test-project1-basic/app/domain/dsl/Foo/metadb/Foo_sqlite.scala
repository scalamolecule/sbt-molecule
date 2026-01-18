// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Foo.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Foo_sqlite() extends Foo_ with MetaDb_sqlite {

  override val schemaResourcePath: String = "db/schema/app/domain/Foo/Foo_sqlite.sql"
}