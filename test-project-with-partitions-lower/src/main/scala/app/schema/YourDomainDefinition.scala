package app.schema

import molecule.schema.definition._


@InOut(0, 5)
object YourDomainDefinition {

  object male {
    trait character {
      val name   = oneString
      val mood   = oneString
      val answer = one[story.conversation]
    }
  }

  object female {
    trait character {
      val name     = oneString
      val mood     = oneString
      val question = one[story.conversation]
    }
  }

  object story {
    trait conversation {
      val says = oneString
    }
  }
}
