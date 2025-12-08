
name := "test-project3-multi-project"

inThisBuild(
  List(
    version := "1.23.0",
    organization := "org.scalamolecule",
    scalaVersion := "3.7.4",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-h2" % "0.28.0",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val bar = project.enablePlugins(MoleculePlugin)
lazy val foo = project.enablePlugins(MoleculePlugin)
