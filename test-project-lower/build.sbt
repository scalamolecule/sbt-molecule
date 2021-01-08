
name := "sbt-molecule-test-project-lower"
version := "0.12.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.4"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true),
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.24.0-SNAPSHOT",
  "com.datomic" % "datomic-free" % "0.9.5697",
  "org.specs2" %% "specs2-core" % "4.10.5"
)

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule") == Some("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // Optional, default: true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
