
name := "sbt-molecule-test-project"
version := "1.3.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.1",
  "org.scalamolecule" %% "molecule-datalog-datomic" % "0.3.0",
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

// For some reason, Scala 3.3 can't find generated classes in jars in lib.
// So we generate source files here instead of jars.
//moleculeMakeJars := false

// Let IDE detect created jars in unmanaged lib directory
exportJars := true

