
// depends on the moleculePluginProject
lazy val moleculePluginProject = RootProject(file("..").getAbsoluteFile.toURI)

//println(moleculePluginProject.toString)
lazy val root = project.in(file(".")).dependsOn(moleculePluginProject)

//addSbtPlugin("net.virtual-void" % "sbt-optimizer" % "0.1.2")
