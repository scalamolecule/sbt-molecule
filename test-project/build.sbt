
name := "sbt-molecule-test-project"
version := "1.7.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.13"
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.1",
  "org.scalamolecule" %% "molecule-sql-h2" % "0.8.0-SNAPSHOT",
)
testFrameworks += new TestFramework("utest.runner.Framework")
Test / parallelExecution := false

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
// or have it on/off for all sbt compilations:
//moleculePluginActive := true

moleculeDataModelPaths := Seq("app")

// Optionally generate source files instead of jars.
//moleculeMakeJars := false

// Let IDE detect created jars in unmanaged lib directory
exportJars := true

