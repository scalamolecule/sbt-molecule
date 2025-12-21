package sbtmolecule.db

import molecule.DomainStructure

trait Segments extends DomainStructure {

  // General segment
  object gen {
    trait Profession {
      val name   = oneString
      val person = manyToOne[Person]
    }

    trait Person {
      val name         = oneString
      val gender       = oneString.allowedValues("male", "female")
      val reviewedBook = manyToOne[lit.Book].oneToMany("Reviewers")
    }
  }


  // Literature segment
  object lit {
    trait Book {
      val title  = oneString
      val author = manyToOne[gen.Person] //.oneToMany("Authors")

      // To avoid attr/partition name clashes we can prepend the definition object name
      // (in case we would have needed an attribute named `gen` for instance)
      val editor = manyToOne[gen.Person] //.oneToMany("Editors")
      val cat    = oneString.allowedValues("good", "bad")
    }
  }


  // Accounting segment
  object accounting {
    trait Invoice {
      val no          = oneInt
      val mainProduct = manyToOne[warehouse.Item]
    }
    trait InvoiceLine {
      val text    = oneString
      val qty     = oneInt
      val product = manyToOne[warehouse.Item]
      val invoice = manyToOne[Invoice].oneToMany("Lines")
    }
  }

  object warehouse {
    trait Item {
      val name = oneString
    }
  }
}