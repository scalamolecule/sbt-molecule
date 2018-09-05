
// depends on the moleculePluginProject
lazy val moleculePluginProject = RootProject(file("..").getAbsoluteFile.toURI)

lazy val root = project.in(file(".")).dependsOn(moleculePluginProject)
