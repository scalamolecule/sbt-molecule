
name := "test-project4-cross-full"

inThisBuild(
  List(
    version := "1.16.2-SNAPSHOT",
    organization := "org.scalamolecule",
    scalaVersion := "3.7.1",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-sql-h2" % "0.22.0-SNAPSHOT",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val app = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .enablePlugins(MoleculePlugin)
