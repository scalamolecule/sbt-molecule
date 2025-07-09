
name := "test-project6-mixed"

inThisBuild(
  List(
    version := "1.19.1",
    organization := "org.scalamolecule",
    scalaVersion := "3.7.1",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-h2" % "0.24.0",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val crossFull = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .enablePlugins(MoleculePlugin)

lazy val crossPure = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(MoleculePlugin)

lazy val jvmOnly = project
  .enablePlugins(MoleculePlugin)

