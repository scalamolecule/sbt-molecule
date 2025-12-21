package sbtmolecule.domains

import molecule.DomainStructure
import sbt.Join


trait Company extends DomainStructure {

  trait Department {
    val name = oneString
    // .Employees (plural of Employee)
  }

  trait Employee {
    val name       = oneString
    val department = manyToOne[Department]
    // .Projects via Assignment
  }

  trait Project {
    val name      = oneString
    val budget    = oneInt
  }

  trait Assignment {
    val employee = manyToOne[Employee]
    val project  = manyToOne[Project]
    val role     = oneString
  }
}

//Employee.name.Assignment.role.Project.name
//Employee.name.Projects.name
//Employee.name.Projects.*(Project.name)
//
//Employee.name.Assignment.role.Projects.name
//Employee.name.Projects.name
//Employee.name.Projects.*(Project.name)


////// direct join table query
////Assignments.employee.role.project.insert(
////  (bob, "lead", scala),
////  (bob, "engineer", java),
////  (liz, "manager", scala)
////).transact
//
//// Using the Assignment join table as any other referenced table (no need for `Project_`
//Employee.name.Assignment.role_("lead").Project.name.query.get ==> List(
//  ("Bob", "Scala")
//)
//
//// or `Projects` to query the joined data (transparently using the join table)
//Employee.name.Projects.name.query.get ==> List(
//  ("Bob", "Scala"),
//  ("Bob", "Java"),
//  ("Liz", "Scala")
//)
//
//// or nested without join property access
//Employee.name.Projects.*(Project.name).query.get ==> List(
//  ("Bob", List("Scala", "Java")),
//  ("Liz", List("Scala"))
//)
//
//
//
//
//
////// direct join table query
////Assignments.employee.role.projects.insert(
////  (bob, "lead", scala),
////  (bob, "engineer", java),
////  (liz, "manager", scala)
////).transact
//
//// Using the Assignment join table as any other referenced table (no need for `Project_`
//Employee.name.Assignment.role_("lead").Projects.name.query.get ==> List(
//  ("Bob", "Scala")
//)
//
//// or `Projects` to query the joined data (transparently using the join table)
//Employee.name.Projects.name.query.get ==> List(
//  ("Bob", "Scala"),
//  ("Bob", "Java"),
//  ("Liz", "Scala")
//)
//
//// or nested without join property access
//Employee.name.Projects.*(Project.name).query.get ==> List(
//  ("Bob", List("Scala", "Java")),
//  ("Liz", List("Scala"))
//)