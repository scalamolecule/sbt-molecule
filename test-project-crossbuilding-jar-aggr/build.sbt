import sbt.Keys.{testFrameworks, version}

lazy val scala212               = "2.12.20"
lazy val scala213               = "2.13.15"
lazy val scala3                 = "3.3.4"
lazy val supportedScalaVersions = List(scala212, scala213, scala3)

inThisBuild(
  List(
    organization := "com.example",
    version := "1.11.0",
    scalaVersion := scala3,
    crossScalaVersions := supportedScalaVersions,
  )
)

//lazy val root = (project in file("."))
lazy val root = project
  .aggregate(app)

//lazy val app = (project in file("app"))
lazy val app = project
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "sbt-molecule-test-project-crossbuilding-jar-aggr",
    version := "1.11.0",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-sql-h2" % "0.15.0",
      "com.lihaoyi" %% "utest" % "0.8.4" % Test,
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),

    // Find scala version specific jars in respective libs
    unmanagedBase := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => file(unmanagedBase.value.getPath ++ "/2.12")
        case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
        case _             => file(unmanagedBase.value.getPath ++ "/3.3")
      }
    },

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDataModelPaths := Seq("app/dataModel"), // Mandatory
    moleculeMakeJars := true, // Optional, default: true
  )
