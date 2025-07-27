
name := "test-project7-graphql"
version := "1.19.4"
organization := "org.scalamolecule"
scalaVersion := "3.7.1"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-graphql-client" % "0.24.1",
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)