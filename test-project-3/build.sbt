
name := "sbt-molecule-test-project-3"
version := "1.11.3-SNAPSHOT"
organization := "org.scalamolecule"
scalaVersion := "3.3.5"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" %  "0.15.3-SNAPSHOT",
  "org.scalameta" %% "munit" % "1.0.3" % Test,
)
testFrameworks += new TestFramework("utest.runner.Framework")
Test / parallelExecution := false
Test / fork := true

enablePlugins(MoleculePlugin)
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDomainPaths := Seq("app/domain")
moleculeMakeJars := false
