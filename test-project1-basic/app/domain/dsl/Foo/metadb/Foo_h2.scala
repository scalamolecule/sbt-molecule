// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Foo.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Foo_h2() extends Foo_ with MetaDb_h2 {

  /** Resource path to SQL schema file to create the database */
  override val schemaResourcePath: String = "db/schema/app/domain/Foo/Foo_h2.sql"
}