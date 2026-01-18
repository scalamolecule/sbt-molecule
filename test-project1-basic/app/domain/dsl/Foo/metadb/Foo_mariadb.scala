// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Foo.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Foo_mariadb() extends Foo_ with MetaDb_mariadb {

  override val schemaResourcePath: String = "db/schema/app/domain/Foo/Foo_mariadb.sql"
}