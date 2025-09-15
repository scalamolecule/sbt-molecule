package sbtmolecule.parse

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.render.RenderDomain.types
import sbtmolecule.{GenerateSourceFiles_db, ParseAndGenerate}
import utest.*

object Company extends DomainStructure {

  trait Employee {
    val name = oneString

    // Developer.name.Assignments.role
    // Developer.name.Assignments.role.Project.title
    // Developer.name.Assignments.*(Assignment.role.Project.title)


    // Possible future sugar bridges (no access to join table properties)

    // Developer.name.Projects.title
    // Developer.name.Projects.*(Project.title)
  }

  trait Project {
    val title   = oneString

    // Project.title.Assignments.role // title/role (titles repeat redundantly)
    // Project.title.Assignments.*(Assignment.role) // roles

    // Project.title.Assignments.role.Employee.name
    // Project.title.Assignments.*(Assignment.role.Employee.name)


    // Possible future sugar bridges (no access to join table properties)

    // Project.title.Employees.name
    // Project.title.Employees.*(Employee.name)
  }
//  trait Zzz {
//    val name = oneString
//  }

  trait Assignment extends Join {
//    val zzz  = manyToOne[Zzz]
    val employee = manyToOne[Employee]
    val project  = manyToOne[Project]
    val role     = oneString

    // Assignment.employee.project.role.insert(
    //   (1, 7, "Manager"),
    //   (2, 7, "Support"),
    // ).transact
  }
//  trait Assignment2 extends Join {
//    val zzz  = manyToOne[Zzz]
//    val employee = manyToOne[Employee]
//    val project  = manyToOne[Project]
//    val role     = oneString
//
//    // Assignment.employee.project.role.insert(
//    //   (1, 7, "Manager"),
//    //   (2, 7, "Support"),
//    // ).transact
//  }
}


object manyToMany extends TestSuite {

  override def tests: Tests = Tests {

    "DSL" - {
      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
      val generator = ParseAndGenerate(path + "manyToMany.scala").generator
      generator.metaDomain ==>
        MetaDomain("sbtmolecule.parse", "Company", List(
          MetaSegment("", List(
            MetaEntity("Employee", List(
              MetaAttribute("id"         , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("name"       , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Assignments", SetValue, "ID"    , Nil, Some("Assignment"), Some("employee"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Projects"   , SetValue, "ID"    , Nil, Some("Assignment"), Some("Project"), Some("ManyToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("Assignment"), List(), List(), false, None),

            MetaEntity("Project", List(
              MetaAttribute("id"         , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("title"      , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Assignments", SetValue, "ID"    , Nil, Some("Assignment"), Some("project"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Employees"  , SetValue, "ID"    , Nil, Some("Assignment"), Some("Employee"), Some("ManyToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("Assignment"), List(), List(), false, None),

            MetaEntity("Assignment", List(
              MetaAttribute("id"      , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("employee", OneValue, "ID"    , Nil, Some("Employee"), Some("Assignments"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("project" , OneValue, "ID"    , Nil, Some("Project"), Some("Assignments"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("role"    , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None)
            ), List(), List(), List(), true, None)
          ))
        ))


      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(2))
    }
  }
}
