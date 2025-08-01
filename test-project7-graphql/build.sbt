
name := "test-project7-graphql"
version := "1.20.0"
organization := "org.scalamolecule"
scalaVersion := "3.7.1"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-graphql-client" % "0.25.0",
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)