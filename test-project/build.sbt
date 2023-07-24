
name := "sbt-molecule-test-project"
version := "1.1.0"
organization := "org.scalamolecule"
//scalaVersion := "2.13.10"
scalaVersion := "3.2.1"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
resolvers += "clojars" at "https://clojars.org/repo",
libraryDependencies ++= Seq(
//  "org.scalamolecule" %% "molecule-boilerplate" % "1.2.0-SNAPSHOT",
//  "org.scalamolecule" %% "molecule-base" % "0.1.0-SNAPSHOT",
//  "org.scalamolecule" %% "molecule-base" % "0.1.0-SNAPSHOT",
  "org.scalamolecule" %% "molecule-datomic" % "0.1.0-SNAPSHOT",
  "com.lihaoyi" %% "utest" % "0.8.1" % Test,
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
// or have it on/off for all sbt compilations:
//moleculePluginActive := true
moleculeDataModelPaths := Seq("app")
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // created jars from generated sources (default)
//moleculeMakeJars := false

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
