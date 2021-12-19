
name := "sbt-molecule-test-project-crossbuilding-src"
version := "1.0.1"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.15", "2.13.7")
scalaVersion in ThisBuild := "2.13.7"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "1.0.1",
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
