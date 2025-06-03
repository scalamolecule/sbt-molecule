import sbt.url

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-molecule",
    description := "sbt plugin to generate and package Molecule boilerplate code",
    version := "1.16.1",
    organization := "org.scalamolecule",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.9.0",
      "org.scalamolecule" %% "molecule-db-base" % "0.21.0",
      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
  .settings(publishSettings)

lazy val publishSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
  // pomIncludeRepository := (_ => false), // necessary for local snapshots?
  publishTo := {
    val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
    if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
    else localStaging.value
  },
  versionScheme := Some("early-semver"),
  Test / publishArtifact := false,
  homepage := Some(url("http://scalamolecule.org")),
  description := "sbt-molecule",
  licenses := List(License.Apache2),
  scmInfo := Some(ScmInfo(
    url("https://github.com/scalamolecule/sbt-molecule"),
    "scm:git:git@github.com:scalamolecule/sbt-molecule.git"
  )),
  developers := List(
    Developer(
      "marcgrue",
      "Marc Grue",
      "marcgrue@gmail.com",
      url("http://marcgrue.com")
    )
  )
)