import sbt.Keys.{exportJars, version}


lazy val scala213               = "2.13.7"
lazy val scala212               = "2.12.15"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala213


lazy val root = (project in file("."))
  .aggregate(app)

lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    // other settings
    name := "sbt-molecule-test-project-crossbuilding-src-aggr",
    version := "1.0.0",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),

    resolvers ++= Seq(
      "clojars" at "https://clojars.org/repo"
    ),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "1.0.0",
      "com.datomic" % "datomic-free" % "0.9.5697",
      "org.specs2" %% "specs2-core" % "4.10.6"
    ),


    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := true, // Optional, default: true


    // Let IntelliJ detect sbt-molecule-created jars in unmanaged lib directory
    exportJars := true,
  )
