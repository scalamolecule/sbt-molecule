
name := "sbt-molecule-test-project-3"
version := "1.9.0"
organization := "org.scalamolecule"
scalaVersion := "3.5.1-RC2"
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.3",
  "org.scalamolecule" %% "molecule-sql-h2" % "0.10.1",
)
testFrameworks += new TestFramework("utest.runner.Framework")
//Test / parallelExecution := false

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
// or have it on/off for all sbt compilations:
//moleculePluginActive := true

moleculeDataModelPaths := Seq("app")

// Optionally generate source files instead of jars.
moleculeMakeJars := false
