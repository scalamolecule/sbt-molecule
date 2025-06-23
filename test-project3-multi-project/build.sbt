
name := "test-project3-multi-project"

inThisBuild(
  List(
    version := "1.18.0-SNAPSHOT",
    organization := "org.scalamolecule",
    scalaVersion := "3.7.1",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-sql-h2" % "0.23.0-SNAPSHOT",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val bar = project.enablePlugins(MoleculePlugin)
lazy val foo = project.enablePlugins(MoleculePlugin)
