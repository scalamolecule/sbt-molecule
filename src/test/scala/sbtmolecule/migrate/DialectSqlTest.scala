package sbtmolecule.migrate

import molecule.DomainStructure
import sbtmolecule.db.sqlDialect.*
import sbtmolecule.migration.MigrationSqlGenerator
import utest.*

// Test domain structures
trait DialectTest_1 extends DomainStructure {
  trait Person {
    val name = oneString.rename("fullName")
    val age  = oneInt.remove
  }
}

trait DialectTest_2 extends DomainStructure {
  trait Person {
    val fullName = oneString
    val email    = oneString // New attribute
  }
}

object DialectSqlTest extends BaseTest {
  val (before, after) = structures("")

  override def tests: Tests = Tests {
    "H2 - column operations" - {
      MigrationSqlGenerator.generateMigrationSql(before, after, H2) ==>
        """ALTER TABLE Person ALTER COLUMN name RENAME TO fullName;
          |ALTER TABLE Person DROP COLUMN age;
          |ALTER TABLE Person ADD COLUMN email VARCHAR;""".stripMargin
    }

    "PostgreSQL - column operations" - {
      MigrationSqlGenerator.generateMigrationSql(before, after, PostgreSQL) ==>
        """ALTER TABLE Person RENAME COLUMN name TO fullName;
          |ALTER TABLE Person DROP COLUMN age;
          |ALTER TABLE Person ADD COLUMN email TEXT COLLATE ucs_basic;""".stripMargin
    }

    "MySQL - column operations" - {
      // MySQL adds underscore to reserved words like "name"
      MigrationSqlGenerator.generateMigrationSql(before, after, MySQL) ==>
        """ALTER TABLE Person CHANGE name_ fullName LONGTEXT COLLATE utf8mb4_0900_as_cs;
          |ALTER TABLE Person DROP COLUMN age;
          |ALTER TABLE Person ADD COLUMN email LONGTEXT COLLATE utf8mb4_0900_as_cs;""".stripMargin
    }

    "MariaDB - column operations" - {
      MigrationSqlGenerator.generateMigrationSql(before, after, MariaDB) ==>
        """ALTER TABLE Person CHANGE name fullName LONGTEXT;
          |ALTER TABLE Person DROP COLUMN age;
          |ALTER TABLE Person ADD COLUMN email LONGTEXT;""".stripMargin
    }

    "SQLite - column operations" - {
      MigrationSqlGenerator.generateMigrationSql(before, after, SQlite) ==>
        """ALTER TABLE Person RENAME COLUMN name TO fullName;
          |ALTER TABLE Person DROP COLUMN age;
          |ALTER TABLE Person ADD COLUMN email TEXT;""".stripMargin
    }
  }
}
