package sbtmolecule.db

import molecule.DomainStructure


object SocialApp extends DomainStructure {

  trait Guest extends Role with query
  trait Member extends Role with read
  trait Moderator extends Role with read
  trait Admin extends Role with all


  // Entity-level update grant to single role
  // Base: Member has read, Admin has all (all 6 actions covered)
  // Grant: Member can also update all attributes
  trait Post extends Member with Admin
    with updating[Member] {
    val content = oneString
    val title   = oneString
  }


  // Entity-level delete grant to single role
  // Base: Moderator has read, Admin has all (all 6 actions covered)
  // Grant: Moderator can also delete
  trait Comment extends Moderator with Admin
    with deleting[Moderator] {
    val text = oneString
  }


  // Multiple role grants at entity level
  // Both Member and Moderator can update, Admin provides all actions
  trait Article extends Member with Moderator with Admin
    with updating[(Member, Moderator)] {
    val title   = oneString
    val content = oneString
  }


  // Combining updating and deleting grants
  // Moderator can update, both Moderator and Admin can delete
  trait ModLog extends Admin with Moderator
    with updating[Moderator]
    with deleting[(Moderator, Admin)] {
    val action    = oneString
    val timestamp = oneLong
  }


  // Different roles with different grants
  trait UserProfile extends Member with Admin
    with updating[Member]
    with deleting[Admin] {
    val displayName = oneString
    val bio         = oneString
  }
}
