package sbtmolecule.parse.domains

import molecule.DomainStructure


object CourseScheduling extends DomainStructure {

  trait Student {
    val name = oneString
  }

  trait Course {
    val name   = oneString
    val budget = oneInt
  }

  trait Professor {
    val name   = oneString
    val budget = oneInt
  }

  trait Semester {
    val name   = oneString
    val budget = oneInt
  }

  trait Enrollment {
    val student   = manyToOne[Student]
    val course    = manyToOne[Course]
    val professor = manyToOne[Professor]
    val semester  = manyToOne[Semester]
    val grade     = oneString
  }
}

//Student.Courses.*(Course.name) // "Courses" coming from the text string "courses"
//Student.Professors.*(Professor.name) // etc.

//  // direct join table query
//  Assignments.employee.role.project.insert(
//    (bob, "lead", scala),
//    (bob, "engineer", java),
//    (liz, "manager", scala)
//  ).transact
//
//  // Using the Assignment join table as any other referenced table (no need for `Project_`
//  Employee.name.Assignment.role_("lead").Project.name.query.get ==> List(
//    ("Bob", "Scala")
//  )
//
//  // or `Projects` to query the joined data (transparently using the join table)
//  Employee.name.Projects.name.query.get ==> List(
//    ("Bob", "Scala"),
//    ("Bob", "Java"),
//    ("Liz", "Scala")
//  )
//
//  // or nested without join property access
//  Employee.name.Projects.*(Project.name).query.get ==> List(
//    ("Bob", List("Scala", "Java")),
//    ("Liz", List("Scala"))
//  )