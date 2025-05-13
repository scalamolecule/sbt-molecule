
name := "test-project3-multi-project"

inThisBuild(
  List(
    version := "1.15.0-SNAPSHOT",
    organization := "org.scalamolecule",
    scalaVersion := "3.6.4",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-sql-h2" % "0.20.0-SNAPSHOT",
      "org.scalameta" %% "munit" % "1.1.1" % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework"),
  )
)

lazy val bar = project.enablePlugins(MoleculePlugin)
lazy val foo = project.enablePlugins(MoleculePlugin)
