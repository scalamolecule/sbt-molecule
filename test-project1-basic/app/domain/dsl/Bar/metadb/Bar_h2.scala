// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Bar.metadb

import molecule.core.dataModel.*
import molecule.db.common.api.*


case class Bar_h2() extends Bar_ with MetaDb_h2 {

  /** Resource path to SQL schema file to create the database */
  override val schemaResourcePath: String = "db/schema/app/domain/Bar/Bar_h2.sql"
}