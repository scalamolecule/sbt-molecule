
lazy val root = project.in(file("."))
  .dependsOn(
    // MoleculePlugin
    RootProject(file("..").getAbsoluteFile.toURI)
  )

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.19.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
