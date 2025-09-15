//package sbtmolecule.parse
//
//import molecule.DomainStructure
//import molecule.base.metaModel.*
//import molecule.core.dataModel.*
//import sbtmolecule.render.RenderDomain.types
//import sbtmolecule.{GenerateSourceFiles_db, ParseAndGenerate}
//import utest.*
//
//object WebShop extends DomainStructure {
//
//  trait Developer {
//    val name = oneString
//
//    // Note how "Project" is used for both joins
//    //
//    // Developer.name.Frontends.Project.title
//    // Developer.name.Frontends.*(Frontend.Project.title)
//
//    // Developer.name.Backends.Project.title
//    // Developer.name.Backends.*(Backend.Project.title)
//
//
//    // Possible future sugar bridges (no access to join table properties)
//
//    // This would be ambiguous - is it via Frontend or via Backend? Would need explicit reverse naming
//    // Developer.name.Projects.title
//    // Developer.name.Projects.*(Project.title)
//  }
//
//  trait Project {
//    val title  = oneString
//
//    // via join table...........
//    // Note how "Designer"/"Engineer" is now used for respective joins
//    //
//    // Project.title.Frontends.Designer.name
//    // Project.title.Frontends.*(Frontend.Designer.name)
//
//    // Project.title.Backends.Engineer.name
//    // Project.title.Backends.*(Backend.Engineer.name)
//
//
//    // Possible future sugar bridges (no access to join table properties)
//
//    // Project.title.Designers.name
//    // Project.title.Designers.*(Designer.name)
//
//    // Project.title.Engineers.name
//    // Project.title.Engineers.*(Engineer.name)
//  }
//
//
//  // Explicit join table entities
//
//  trait Frontend {
//    val designer = manyToOne[Developer] // (optional explicit plural reverse name)
//    val project  = manyToOne[Project] // ("FrontendProject") needed if generating bridges (or some other name)
//    val role     = oneDate
//
//    // Direct access to join table
//    // Frontend.designer.project.role.insert(
//    //   (1, 7, "Lead"),
//    //   (2, 7, "Graphics"),
//    // ).transact
//  }
//
//  trait Backend {
//    val engineer = manyToOne[Developer]
//    val project  = manyToOne[Project] // ("BackendProject") needed if generating bridges (or some other name)
//    val role     = oneDate
//  }
//}
//
//
//object manyToMany2 extends TestSuite {
//
//  override def tests: Tests = Tests {
//
//    "DSL" - {
//      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
//      val generator = ParseAndGenerate(path + "manyToMany2.scala").generator
//      generator.metaDomain ==>
//        MetaDomain("sbtmolecule.parse", "Company", List(
//          MetaSegment("", List(
//            MetaEntity("Employee", List(
//              MetaAttribute("id", OneValue, "ID", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("name", OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("Employees", SetValue, "ID", Nil, Some("Assignment_"), None, List("one-to-many"), None, Nil, Nil, Nil, Some("employee"))
//            ), List("Assignment_"), List(), List(), false, None),
//
//            MetaEntity("Project", List(
//              MetaAttribute("id", OneValue, "ID", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("name", OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("budget", OneValue, "Int", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("Projects", SetValue, "ID", Nil, Some("Assignment_"), None, List("one-to-many"), None, Nil, Nil, Nil, Some("project"))
//            ), List("Assignment_"), List(), List(), false, None),
//
//            MetaEntity("Assignment_", List(
//              MetaAttribute("id", OneValue, "ID", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("role", OneValue, "String", Nil, None, None, Nil, None, Nil, Nil, Nil, None),
//              MetaAttribute("employee", OneValue, "ID", Nil, Some("Employee"), None, Nil, None, Nil, Nil, Nil, Some("Employees")),
//              MetaAttribute("project", OneValue, "ID", Nil, Some("Project"), None, Nil, None, Nil, Nil, Nil, Some("Projects"))
//            ), List(), List(), List(), false, None)
//          ))
//        ))
//
//
//      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
//      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
//      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
//      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(2))
//    }
//  }
//}
