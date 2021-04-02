import sbt.Keys.version
import sbtmolecule.MoleculePlugin.autoImport.{moleculeAllIndexed, moleculeDataModelPaths}


lazy val scala213               = "2.13.5"
lazy val scala212               = "2.12.13"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala213


lazy val root = (project in file("."))
  .aggregate(app)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
  )

lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    // other settings
    name := "sbt-molecule-test-project-crossbuilding-src-aggr",
    version := "0.14.0-SNAPSHOT",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),

    resolvers ++= Seq(
      "clojars" at "https://clojars.org/repo"
    ),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "0.25.2-SNAPSHOT",
      "com.datomic" % "datomic-free" % "0.9.5697",
      "org.specs2" %% "specs2-core" % "4.10.6"
    ),


    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := false // Optional, default: true
  )
