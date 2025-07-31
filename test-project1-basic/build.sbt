
name := "test-project1-basic"
version := "1.19.6"
organization := "org.scalamolecule"
scalaVersion := "3.7.1"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-db-h2" % "0.24.2",
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)