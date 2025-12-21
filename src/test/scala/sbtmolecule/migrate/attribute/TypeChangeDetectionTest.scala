package sbtmolecule.migrate.attribute

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.migrate.BaseTest
import utest.*

// Type change: Int → String
trait TypeChange_1 extends DomainStructure {
  trait Person {
    val age = oneInt
  }
}

trait TypeChange_2 extends DomainStructure {
  trait Person {
    val age = oneString // Changed type
  }
}

// Cardinality change: one → set
trait CardinalityChange_1 extends DomainStructure {
  trait Person {
    val tags = oneString
  }
}

trait CardinalityChange_2 extends DomainStructure {
  trait Person {
    val tags = setString // Changed cardinality
  }
}

// Both type and cardinality change
trait BothChange_1 extends DomainStructure {
  trait Person {
    val score = oneInt
  }
}

trait BothChange_2 extends DomainStructure {
  trait Person {
    val score = setString // Changed both type and cardinality
  }
}

// Multiple attributes with type changes
trait MultipleChanges_1 extends DomainStructure {
  trait Person {
    val age   = oneInt
    val score = oneDouble
  }
}

trait MultipleChanges_2 extends DomainStructure {
  trait Person {
    val age   = oneString // Changed type
    val score = oneBoolean // Changed type
  }
}

object TypeChangeDetectionTest extends BaseTest {
  override def tests: Tests = Tests {
    "Detect type change" - {
      intercept[Exception] {
        val allStructures = rawStructures("attribute/")
        val before        = allStructures(0)
        val after         = allStructures(1)
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Attribute type/cardinality changes are not allowed.
          |
          |The following attributes have changed their type or cardinality:
          |  - Person.age: oneInt → oneString
          |
          |Type and cardinality changes fundamentally alter the semantics of your data model
          |and cannot be automatically migrated.
          |
          |To migrate data between incompatible types, follow this process:
          |
          |1. Create a new attribute with the desired type/cardinality:
          |   val ageNew = oneString
          |
          |2. Write custom code to convert and save data from the old attribute to the new one:
          |   // Example migration code in your application:
          |   MyEntity.age.ageNew.query.get.foreach { case (id, oldValue) =>
          |     val newValue = convertOldToNew(oldValue)  // Your conversion logic
          |     MyEntity(id).ageNew(newValue).update.transact
          |   }
          |
          |3. Verify that all data has been correctly converted:
          |   // Query the new attribute and validate the results
          |   MyEntity.ageNew.query.get
          |
          |4. Remove the old attribute:
          |   val age = oneInt.remove
          |
          |5. (Optional) Rename the new attribute to the old name:
          |   val ageNew = oneString.rename("age")
          |""".stripMargin
    }

    "Detect cardinality change" - {
      intercept[Exception] {
        val allStructures = rawStructures("attribute/")
        val before        = allStructures(2)
        val after         = allStructures(3)
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Attribute type/cardinality changes are not allowed.
          |
          |The following attributes have changed their type or cardinality:
          |  - Person.tags: oneString → setString
          |
          |Type and cardinality changes fundamentally alter the semantics of your data model
          |and cannot be automatically migrated.
          |
          |To migrate data between incompatible types, follow this process:
          |
          |1. Create a new attribute with the desired type/cardinality:
          |   val tagsNew = setString
          |
          |2. Write custom code to convert and save data from the old attribute to the new one:
          |   // Example migration code in your application:
          |   MyEntity.tags.tagsNew.query.get.foreach { case (id, oldValue) =>
          |     val newValue = convertOldToNew(oldValue)  // Your conversion logic
          |     MyEntity(id).tagsNew(newValue).update.transact
          |   }
          |
          |3. Verify that all data has been correctly converted:
          |   // Query the new attribute and validate the results
          |   MyEntity.tagsNew.query.get
          |
          |4. Remove the old attribute:
          |   val tags = oneString.remove
          |
          |5. (Optional) Rename the new attribute to the old name:
          |   val tagsNew = setString.rename("tags")
          |""".stripMargin
    }

    "Detect both type and cardinality change" - {
      intercept[Exception] {
        val allStructures = rawStructures("attribute/")
        val before        = allStructures(4)
        val after         = allStructures(5)
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Attribute type/cardinality changes are not allowed.
          |
          |The following attributes have changed their type or cardinality:
          |  - Person.score: oneInt → setString
          |
          |Type and cardinality changes fundamentally alter the semantics of your data model
          |and cannot be automatically migrated.
          |
          |To migrate data between incompatible types, follow this process:
          |
          |1. Create a new attribute with the desired type/cardinality:
          |   val scoreNew = setString
          |
          |2. Write custom code to convert and save data from the old attribute to the new one:
          |   // Example migration code in your application:
          |   MyEntity.score.scoreNew.query.get.foreach { case (id, oldValue) =>
          |     val newValue = convertOldToNew(oldValue)  // Your conversion logic
          |     MyEntity(id).scoreNew(newValue).update.transact
          |   }
          |
          |3. Verify that all data has been correctly converted:
          |   // Query the new attribute and validate the results
          |   MyEntity.scoreNew.query.get
          |
          |4. Remove the old attribute:
          |   val score = oneInt.remove
          |
          |5. (Optional) Rename the new attribute to the old name:
          |   val scoreNew = setString.rename("score")
          |""".stripMargin
    }

    "Detect multiple type changes" - {
      intercept[Exception] {
        val allStructures = rawStructures("attribute/")
        val before        = allStructures(6)
        val after         = allStructures(7)
        import sbtmolecule.migration.MigrationDetector
        MigrationDetector.annotateBefore(before, after)
      }.getMessage ==>
        """-- ERROR: Attribute type/cardinality changes are not allowed.
          |
          |The following attributes have changed their type or cardinality:
          |  - Person.age: oneInt → oneString
          |  - Person.score: oneDouble → oneBoolean
          |
          |Type and cardinality changes fundamentally alter the semantics of your data model
          |and cannot be automatically migrated.
          |
          |To migrate data between incompatible types, follow this process:
          |
          |1. Create a new attribute with the desired type/cardinality:
          |   val ageNew = oneString
          |
          |2. Write custom code to convert and save data from the old attribute to the new one:
          |   // Example migration code in your application:
          |   MyEntity.age.ageNew.query.get.foreach { case (id, oldValue) =>
          |     val newValue = convertOldToNew(oldValue)  // Your conversion logic
          |     MyEntity(id).ageNew(newValue).update.transact
          |   }
          |
          |3. Verify that all data has been correctly converted:
          |   // Query the new attribute and validate the results
          |   MyEntity.ageNew.query.get
          |
          |4. Remove the old attribute:
          |   val age = oneInt.remove
          |
          |5. (Optional) Rename the new attribute to the old name:
          |   val ageNew = oneString.rename("age")
          |""".stripMargin
    }
  }
}
