version := "0.6.2"
organization := "org.scalamolecule"
scalaVersion := "2.12.7"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  "datomic" at "http://files.datomic.com/maven",
  "clojars" at "http://clojars.org/repo",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.15.1",
  "com.datomic" % "datomic-free" % "0.9.5697"
)

// Molecule
enablePlugins(MoleculePlugin)
moleculeSchemas := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // Optional, default: true