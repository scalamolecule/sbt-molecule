
name := "test-project1-basic"
version := "1.17.0"
organization := "org.scalamolecule"
scalaVersion := "3.7.1"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-db-sql-h2" % "0.22.0",
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)