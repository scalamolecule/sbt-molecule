
name := "sbt-molecule-test-project-with-partitions-lower"
version := "1.3.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.12"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-datalog-datomic" % "0.3.0",
  "com.lihaoyi" %% "utest" % "0.8.1",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // Optional, default: true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true