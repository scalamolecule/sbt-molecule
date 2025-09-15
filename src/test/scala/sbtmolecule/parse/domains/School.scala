package sbtmolecule.parse.domains

import molecule.DomainStructure

object School extends DomainStructure {

  trait Teacher {
    val name = oneString
    // Many-to-many bridges
    // Teacher.name.Courses.subject
    // Teacher.name.Students.name

    // One-to-many-to-One using a property of the join table
    // Teacher.Enrollments.startYear.Course.subject
    // Teacher.Enrollments.startYear.Student.name
  }

  trait Course {
    val subject = oneString
    // Many-to-many bridges
    // Course.subject.Teachers.name
    // Course.subject.Students.name

    // One-to-many-to-One using a property of the join table
    // Course.Enrollments.startYear.Teacher.name
    // Course.Enrollments.startYear.Student.name
  }

  trait Student {
    val name = oneString
    val age  = oneInt
    // Many-to-many bridges
    // Student.name.Teachers.name
    // Student.name.Course.name

    // One-to-many-to-One using a property of the join table
    // Student.Enrollments.startYear.Teacher.name
    // Student.Enrollments.startYear.Course.subject
  }

  // Join table with a property
  trait Enrollment extends Join {
    val teacher   = manyToOne[Teacher]
    val course    = manyToOne[Course]
    val student   = manyToOne[Student]
    val startYear = oneInt
  }
}