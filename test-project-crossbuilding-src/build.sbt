
name := "sbt-molecule-test-project-crossbuilding-src"
version := "1.11.3-SNAPSHOT"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.20", "2.13.16", "3.3.5")
ThisBuild / scalaVersion := "2.13.16"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" %  "0.16.0",
  "org.scalameta" %% "munit" % "1.0.3" % Test,
)
testFrameworks += new TestFramework("utest.runner.Framework")
Test / parallelExecution := false
Test / fork := true

unmanagedBase := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => file(unmanagedBase.value.getPath ++ "/2.12")
    case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
    case _             => file(unmanagedBase.value.getPath ++ "/3.3")
  }
}

enablePlugins(MoleculePlugin)
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDomainPaths := Seq("app/domain")
moleculeMakeJars := false
