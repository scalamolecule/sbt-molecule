
//// depends on the moleculePluginProject
//lazy val moleculePluginProject = RootProject(file("..").getAbsoluteFile.toURI)
//
////println(moleculePluginProject.toString)
//lazy val root = project.in(file(".")).dependsOn(moleculePluginProject)


addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "1.2.0-SNAPSHOT")

