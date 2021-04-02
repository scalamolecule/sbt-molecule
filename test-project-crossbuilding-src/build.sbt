
name := "sbt-molecule-test-project-crossbuilding-src"
version := "0.14.0-SNAPSHOT"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.13", "2.13.5")
scalaVersion in ThisBuild := "2.13.5"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.25.2-SNAPSHOT",
  "com.datomic" % "datomic-free" % "0.9.5697",
  "org.specs2" %% "specs2-core" % "4.10.6"
)

// todo can we tell sbt to find compiled classes so that we can say `sbt +test` ?

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule") == Some("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := false // Optional, default: true
