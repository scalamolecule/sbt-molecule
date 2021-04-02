
name := "sbt-molecule-test-project"
version := "0.14.0-SNAPSHOT"
organization := "org.scalamolecule"
scalaVersion := "2.13.5"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.25.2-SNAPSHOT",
  "com.datomic" % "datomic-free" % "0.9.5697",
  "org.specs2" %% "specs2-core" % "4.10.6"
)

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
//moleculePluginActive := true
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := false
//moleculeGenericPkg := "molecule.core.generic" // turn this on for generics, otherwise off!

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
