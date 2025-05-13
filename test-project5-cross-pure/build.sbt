
name := "test-project5-cross-pure"

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

lazy val app = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(MoleculePlugin)
