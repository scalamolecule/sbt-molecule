
name := "test-project8-migration"
version := "1.24.2"
organization := "org.scalamolecule"
scalaVersion := "3.7.4"
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-db-h2" % "0.29.0",
  "org.flywaydb" % "flyway-core" % "10.4.1" % Test,
  "com.lihaoyi" %% "utest" % "0.8.5" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(MoleculePlugin)