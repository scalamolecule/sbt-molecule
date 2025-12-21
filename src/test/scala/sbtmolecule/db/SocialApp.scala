package sbtmolecule.db

import molecule.DomainStructure


trait SocialApp extends DomainStructure {

  trait Guest extends Role with query
  trait Member extends Role with query
  trait Moderator extends Role with query
  trait Admin extends Role with query with save with insert with update with delete


  // Delete-by-ID with attribute restrictions
  // Tests that delete checks both entity AND attribute permissions
  // Both Member and Admin can delete at entity level, but secretNotes is restricted to Admin only
  trait Document extends Member with Admin
    with deleting[(Member, Admin)] {
    val title = oneString // All roles (Member, Admin)

    // Only Admin can access this attribute
    val secretNotes = oneString.only[Admin]
  }
}
