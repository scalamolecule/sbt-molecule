package app.dataModel

import molecule.core.data.model._


@InOut(0, 5)
object YourDomainDataModel {

  object male {
    trait Character {
      val name   = oneString
      val mood   = oneEnum("good", "bad")
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
      val says = oneString.fulltext.noHistory.doc("hi there")
    }
  }
}
