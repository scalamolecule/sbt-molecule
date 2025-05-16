
name := "test-project1-basic"
version := "1.15.0"
organization := "org.scalamolecule"
scalaVersion := "3.6.4"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-db-sql-h2" % "0.20.0",
  "com.lihaoyi" %% "utest" % "0.8.4" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)