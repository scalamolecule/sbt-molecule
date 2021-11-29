
// depends on the moleculePluginProject
lazy val moleculePluginProject = RootProject(file("..").getAbsoluteFile.toURI)

//println(moleculePluginProject.toString)
lazy val root = project.in(file(".")).dependsOn(moleculePluginProject)


val pluginVersion = "1.0.0"
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % pluginVersion)
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % pluginVersion)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.7.1")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding",
  "utf8"
)
