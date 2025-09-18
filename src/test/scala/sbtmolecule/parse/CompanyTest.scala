package sbtmolecule.parse

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.render.RenderDomain.types
import sbtmolecule.{GenerateSourceFiles_db, ParseAndGenerate}
import utest.*

object Company extends DomainStructure {

  trait Department {
    val name = oneString
  }

  trait Employee {
    val name       = oneString
    val department = manyToOne[Department]
  }

  trait Project {
    val name   = oneString
    val budget = oneInt
  }

  trait Assignment extends Join {
    val employee = manyToOne[Employee] // .oneToMany("Xxx")
    val project  = manyToOne[Project]
    val role     = oneString
  }
}


object CompanyTest extends TestSuite {

  override def tests: Tests = Tests {

    "DSL" - {
      val path = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
      val generator = ParseAndGenerate(path + getClass.getSimpleName.dropRight(1) + ".scala").generator
      generator.metaDomain ==>
        MetaDomain("sbtmolecule.parse", "Company", List(
          MetaSegment("", List(
            MetaEntity("Department", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Employees", SetValue, "ID", Nil, Some("Employee"), Some("department"), Some(OneToMany), None, Nil, None, Nil, Nil, Nil, None)
            ), List("Employee"), List(), List(), false, None),

            MetaEntity("Employee", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("department", OneValue, "ID", Nil, Some("Department"), Some("Employees"), Some(ManyToOne), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Assignments", SetValue, "ID", Nil, Some("Assignment"), Some("employee"), Some(OneToMany), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Projects", SetValue, "ID", Nil, Some("Project"), Some("Employee-Assignments-Assignment-employee-project-Project"), Some(ManyToMany), None, Nil, None, Nil, Nil, Nil, None)
            ), List("Assignment"), List(), List(), false, None),

            MetaEntity("Project", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("name", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("budget", OneValue, "Int", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Assignments", SetValue, "ID", Nil, Some("Assignment"), Some("project"), Some(OneToMany), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Employees", SetValue, "ID", Nil, Some("Employee"), Some("Project-Assignments-Assignment-project-employee-Employee"), Some(ManyToMany), None, Nil, None, Nil, Nil, Nil, None)
            ), List("Assignment"), List(), List(), false, None),

            MetaEntity("Assignment", List(
              MetaAttribute("id", OneValue, "ID", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("employee", OneValue, "ID", Nil, Some("Employee"), Some("Assignments"), Some(ManyToOne), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("project", OneValue, "ID", Nil, Some("Project"), Some("Assignments"), Some(ManyToOne), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("role", OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None)
            ), List(), List(), List(), true, None)
          ))
        ))

      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      //      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
      //      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(2))
    }
  }
}
