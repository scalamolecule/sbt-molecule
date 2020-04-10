
version := "0.9.0"
organization := "org.scalamolecule"
scalaVersion := "2.13.1"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  ("datomic" at "http://files.datomic.com/maven").withAllowInsecureProtocol(true),
  ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.22.0",
  "com.datomic" % "datomic-free" % "0.9.5697",
)

// Molecule
enablePlugins(MoleculePlugin)
moleculeSchemas := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := false // Optional, default: true