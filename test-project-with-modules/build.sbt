import sbt.Keys._

lazy val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.9.0",
  organization := "org.scalamolecule",
  scalaVersion := "2.12.10",
  scalacOptions := Seq("-feature", "-language:implicitConversions", "-Yrangepos"),
  resolvers ++= Seq(
    ("datomic" at "http://files.datomic.com/maven").withAllowInsecureProtocol(true),
    ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule" % "0.22.0",
    "com.datomic" % "datomic-free" % "0.9.5697"
  )
)


lazy val root = project.in(file("."))
  .aggregate(app)
  .settings(commonSettings)


lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(commonSettings)
  .settings(
    name := "sbt-molecule-example-app",
    version := "1.0",
    moleculeSchemas := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := true // Optional, default: true
  )