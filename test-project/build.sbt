
name := "sbt-molecule-test-project"
version := "1.0.2"
organization := "org.scalamolecule"
scalaVersion := "2.13.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "1.1.0",
  "com.lihaoyi" %% "utest" % "0.7.10",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Let IntelliJ detect sbt-molecule-created jars in unmanaged lib directory
exportJars := true

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
// or have it on/off for all sbt compilations:
//moleculePluginActive := true
moleculeDataModelPaths := Seq("app")
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
