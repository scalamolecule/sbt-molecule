package app.schema

import molecule.schema.definition._


@InOut(0, 5)
object YourDomainDefinition {

  object male {
    trait Character {
      val name   = oneString
      val mood   = oneString
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
      val says = oneString
    }
  }
}
