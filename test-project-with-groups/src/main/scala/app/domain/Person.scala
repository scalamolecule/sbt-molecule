package app.domain

import molecule.Domain

object Person extends Domain(5) {

  object male {
    trait Character {
      val name   = oneString
      val mood   = oneString.enums("good", "bad")
      val answer = one[story.Conversation]
    }
  }

  object female {
    trait Character {
      val name     = oneString
      val mood     = oneString
      val question = one[story.Conversation]
    }
  }

  object story {
    trait Conversation {
      val says = oneString.noHistory.descr("hi there")
    }
  }
}
