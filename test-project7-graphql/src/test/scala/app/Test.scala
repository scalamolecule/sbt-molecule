package app

import utest.*

object Test extends TestSuite {


  override def tests: Tests = Tests {

    "bar" - {
//      import app.domain.dsl.Bar.*

      //      Person.name("Bob").age(42).save.transact
      //      Person.name.age.query.get ==> List(("Bob", 42))

//      hero.name.query.get.head ==> "R2-D2"

      1 ==> 1
    }
  }
}