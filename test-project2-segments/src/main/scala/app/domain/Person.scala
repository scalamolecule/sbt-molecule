package app.domain

import molecule.DomainStructure

object Person extends DomainStructure {

  object male {
    trait Character {
      val name   = oneString
      val mood   = oneString.allowedValues("good", "bad")
      val answer = manyToOne[story.Conversation]
    }
  }

  object female {
    trait Character {
      val name     = oneString
      val mood     = oneString
      val question = manyToOne[story.Conversation]
    }
  }

  object story {
    trait Conversation {
      val says = oneString.descr("hi there")
    }
  }
}
