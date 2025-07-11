
name := "test-project5-cross-pure"

inThisBuild(
  List(
    version := "1.19.4-SNAPSHOT",
    organization := "org.scalamolecule",
    scalaVersion := "3.7.1",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-db-h2" % "0.24.1-SNAPSHOT",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
)

lazy val app = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(MoleculePlugin)
