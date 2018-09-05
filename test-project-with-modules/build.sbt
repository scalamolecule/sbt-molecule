import sbt.Keys._

lazy val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.5.0",
  organization := "org.scalamolecule",
  scalaVersion := "2.12.6",
  scalacOptions := Seq("-feature", "-language:implicitConversions", "-Yrangepos"),
  resolvers ++= Seq(
    "my.datomic.com" at "https://my.datomic.com/repo",
    "clojars" at "http://clojars.org/repo",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule" % "0.14.0",
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
    moleculeSeparateInFiles := false, // Optional to set
    moleculeAllIndexed := true // Optional to set
  )