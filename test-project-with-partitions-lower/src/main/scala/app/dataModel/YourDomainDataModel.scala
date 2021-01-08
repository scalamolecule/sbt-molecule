package app.dataModel

import molecule.core.data.model._


@InOut(0, 5)
object YourDomainDataModel {

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
