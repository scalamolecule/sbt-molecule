version := "0.4.4"
organization := "org.scalamolecule"
scalaVersion := "2.12.4"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
resolvers ++= Seq(
  "datomic" at "http://files.datomic.com/maven",
  "clojars" at "http://clojars.org/repo",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.13.1",
  "com.datomic" % "datomic-free" % "0.9.5561.62"
)

// Molecule
enablePlugins(MoleculePlugin)
moleculeSchemas := Seq("app") // Mandatory
moleculeSeparateInFiles := false // Optional to set
moleculeAllIndexed := true // Optional to set