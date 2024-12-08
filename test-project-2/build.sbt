
name := "sbt-molecule-test-project-2"
version := "1.10.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.15"
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
//moleculeMakeJars := false

//resolvers += "molecule" at "file://" + baseDirectory.value
//libraryDependencies += "lib_molecule" %% name.value % "0.1.0"
