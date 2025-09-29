
name := "test-project2-segments"
version := "1.21.0"
organization := "org.scalamolecule"
scalaVersion := "3.7.3"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-db-h2" % "0.26.0",
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)