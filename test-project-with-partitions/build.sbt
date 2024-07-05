
name := "sbt-molecule-test-project-with-partitions"
version := "1.8.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.14"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.9.0",
  "com.lihaoyi" %% "utest" % "0.9.0",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeMakeJars := false // Optional, default: true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true