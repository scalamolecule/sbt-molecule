
version := "0.11.0"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.12", "2.13.4")
scalaVersion in ThisBuild := "2.13.4"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

resolvers ++= Seq(
  ("datomic" at "http://files.datomic.com/maven").withAllowInsecureProtocol(true),
  ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.mavenLocal
)
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule" % "0.23.0-SNAPSHOT",
  "com.datomic" % "datomic-free" % "0.9.5697",
)

// Molecule
enablePlugins(MoleculePlugin)
moleculeSchemas := Seq("app") // Mandatory
moleculeAllIndexed := true // Optional, default: true
moleculeMakeJars := true // Optional, default: true


unmanagedBase := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
    case _             => file(unmanagedBase.value.getPath ++ "/2.12")
  }
}
// Let IntelliJ detect created jars in unmanaged lib directory
exportJars := true
