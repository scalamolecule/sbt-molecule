import sbt.Keys.version
import sbtmolecule.MoleculePlugin.autoImport.{moleculeAllIndexed, moleculeDataModelPaths}


lazy val scala213               = "2.13.4"
lazy val scala212               = "2.12.12"
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
    version := "0.12.0",
    organization := "org.scalamolecule",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),

    resolvers ++= Seq(
      ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true)
    ),
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "0.24.0-SNAPSHOT",
      "com.datomic" % "datomic-free" % "0.9.5697",
      "org.specs2" %% "specs2-core" % "4.10.5"
    ),


    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := false // Optional, default: true
  )
