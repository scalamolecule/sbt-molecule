package sbtmolecule.parse.domains

import molecule.DomainStructure


object FriendsOfFriends extends DomainStructure {

  trait Person {
    val name = oneString

    // Knows - "initiator" of friendship
    // KnownBy - "befriend"
  }

  trait Knows {
    val person1 = manyToOne[Person].oneToMany("knows")
    val person2 = manyToOne[Person].oneToMany("knownBy")
  }

  "Bob knows Liz"

  //  Person.name_("Bob").Knows.friend // Liz
  //  Person.name.Knows.friend_("Liz") // Bob
  //
  //  Person.name_("Liz").KnownBy.befriended // Bob
  //  Person.name.KnownBy.befriended_("Bob") // Liz
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