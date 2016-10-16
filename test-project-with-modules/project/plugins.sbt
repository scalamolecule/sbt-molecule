
// depends on the moleculePluginProject
lazy val moleculePluginProject = file("..").getAbsoluteFile.toURI

//println(moleculePluginProject.toString)
lazy val root = project.in(file("."))
  .dependsOn(moleculePluginProject)
