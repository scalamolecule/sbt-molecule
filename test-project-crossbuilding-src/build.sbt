
name := "sbt-molecule-test-project-crossbuilding-src"
version := "1.9.0"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.19", "2.13.14")
ThisBuild / scalaVersion := "2.13.14"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.10.1",
  "com.lihaoyi" %% "utest" % "0.8.3",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Ensure clojure loads correctly for async tests run from sbt
Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app/dataModel") // Mandatory
moleculeMakeJars := false // Optional, default: true
