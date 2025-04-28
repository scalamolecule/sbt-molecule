
name := "test-project3-multi-project"

inThisBuild(
  List(
    version := "1.14.0",
    organization := "org.scalamolecule",
    scalaVersion := "3.6.4",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-sql-h2" % "0.19.0",
      "org.scalameta" %% "munit" % "1.0.3" % Test,
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val bar = project.enablePlugins(MoleculePlugin)
lazy val foo = project.enablePlugins(MoleculePlugin)
