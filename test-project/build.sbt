import sbt.Keys._

lazy val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.1.0",
  organization := "org.scalamolecule",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
  resolvers ++= Seq(
    "datomic" at "http://files.datomic.com/maven",
    "clojars" at "http://clojars.org/repo",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
  ),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule" % "0.6.2",
    "com.datomic" % "datomic-free" % "0.9.5359"
  )
)


lazy val root = project.in(file("."))
  .aggregate(app)
  .settings(commonSettings)


lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sbt-molecule-example-app",
    version := "1.0",
    moleculeSchemas := Seq("app")
  )

