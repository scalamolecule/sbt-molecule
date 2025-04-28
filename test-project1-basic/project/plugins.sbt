
lazy val root = project.in(file("."))
  .dependsOn(
    // MoleculePlugin
    RootProject(file("..").getAbsoluteFile.toURI)
  )
