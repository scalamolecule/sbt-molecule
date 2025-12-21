package sbtmolecule.domains

import molecule.DomainStructure

trait many2many_multiple extends DomainStructure {

  trait Developer {
    val name = oneString
  }

  trait Project {
    val title  = oneString
    val budget = oneInt
  }

  trait Frontend_ {
    val designer = manyToOne[Developer] // (optional explicit plural reverse name)
    val frontend = manyToOne[Project]
    val role     = oneDate
  }

  trait Backend_ {
    val engineer = manyToOne[Developer]
    val backend  = manyToOne[Project]
    val role     = oneDate
  }
}

//// Employee --> Project
//
//// Join table property
//Developer.name.Frontend.role.Project.title
//Developer.name.Backend.role.Project.title
//
//// flat - plural names deducted from singular names
//Developer.name.Frontends.title
//Developer.name.Backends.title
//
//// nested
//Developer.name.Frontends.*(Project.title)
//Developer.name.Backends.*(Project.title)
//
//
//// Project --> Developer
//
//// Join table access
//Project.title.Frontend.role.Developer.name
//Project.title.Backend.role.Developer.name
//
//// flat
//Project.title.Designers.name
//Project.title.Engineers.name
//
//// nested
//Project.title.Designers.*(Developer.name)
//Project.title.Engineers.*(Developer.name)
