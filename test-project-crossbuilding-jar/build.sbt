
name := "sbt-molecule-test-project-crossbuilding-jar"
version := "0.12.0"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.12", "2.13.4")
scalaVersion in ThisBuild := "2.13.4"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true)
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.24.0-SNAPSHOT",
  "com.datomic" % "datomic-free" % "0.9.5697",
  "org.specs2" %% "specs2-core" % "4.10.5"
)

// Find scala version specific jars in respective libs
unmanagedBase := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
    case _             => file(unmanagedBase.value.getPath ++ "/2.12")
  }
}

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule") == Some("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // Optional, default: true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
