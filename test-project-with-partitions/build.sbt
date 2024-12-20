
name := "sbt-molecule-test-project-with-partitions"
version := "1.11.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.15"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.15.0",
  "com.lihaoyi" %% "utest" % "0.8.4",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app/dataModel") // Mandatory
moleculeMakeJars := false // Optional, default: true