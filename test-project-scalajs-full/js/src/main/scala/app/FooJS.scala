package app

object FooJS {

  def hello(name: String): String = {
    Shared.confirm("(js) Hello " + name)
  }
}
