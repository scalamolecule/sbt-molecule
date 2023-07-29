import sbt.Keys.{exportJars, testFrameworks, version}


lazy val scala212               = "2.12.17"
lazy val scala213               = "2.13.10"
lazy val scala3                 = "3.2.1"
lazy val supportedScalaVersions = List(scala212, scala213, scala3)

ThisBuild / organization := "com.example"
ThisBuild / version := "1.1.0"
ThisBuild / scalaVersion := scala3


lazy val root = (project in file("."))
  .aggregate(app)

lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    // other settings
    name := "sbt-molecule-test-project-crossbuilding-src-aggr",
    version := "1.1.0",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    resolvers += "clojars" at "https://clojars.org/repo",
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule-datomic" % "0.1.0-SNAPSHOT",
      "com.lihaoyi" %% "utest" % "0.8.1" % Test,
    ),

    testFrameworks += new TestFramework("utest.runner.Framework"),

    // Ensure clojure loads correctly for async tests run from sbt
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := true, // Optional, default: true


    // Let IntelliJ detect sbt-molecule-created jars in unmanaged lib directory
    exportJars := true,
  )
