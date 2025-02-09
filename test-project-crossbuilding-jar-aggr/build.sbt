import sbt.Keys.{testFrameworks, version}

lazy val scala212               = "2.12.20"
lazy val scala213               = "2.13.16"
lazy val scala3                 = "3.3.5"
lazy val supportedScalaVersions = List(scala212, scala213, scala3)

inThisBuild(
  List(
    organization := "com.example",
    version := "1.11.3-SNAPSHOT",
    scalaVersion := scala3,
    crossScalaVersions := supportedScalaVersions,
  )
)

lazy val root = project
  .aggregate(app)

lazy val app = project
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "sbt-molecule-test-project-crossbuilding-jar-aggr",
    version := "1.11.3-SNAPSHOT",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-sql-h2" %  "0.15.3-SNAPSHOT",
      "org.scalameta" %% "munit" % "1.0.3" % Test,
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Test / parallelExecution := false,
    Test / fork := true,

    // Find scala version specific jars in respective libs
    unmanagedBase := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => file(unmanagedBase.value.getPath ++ "/2.12")
        case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
        case _             => file(unmanagedBase.value.getPath ++ "/3.3")
      }
    },

    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDomainPaths := Seq("app/domain"),
  )
