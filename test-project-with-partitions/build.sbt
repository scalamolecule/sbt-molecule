
name := "sbt-molecule-test-project-with-partitions"
version := "1.9.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.14"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.10.0",
  "com.lihaoyi" %% "utest" % "0.8.3",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeMakeJars := false // Optional, default: true