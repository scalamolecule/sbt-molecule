
name := "sbt-molecule-test-project-3"
version := "1.10.0"
organization := "org.scalamolecule"
scalaVersion := "3.3.4"
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.4",
  "org.scalamolecule" %% "molecule-sql-h2" % "0.13.0",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
// or have it on/off for all sbt compilations:
//moleculePluginActive := true

moleculeDataModelPaths := Seq("app/dataModel")

// Optionally generate source files instead of jars.
moleculeMakeJars := false
