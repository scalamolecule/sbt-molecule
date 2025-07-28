import sbt.url

val calibanVersion = "2.10.0"

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-molecule",
    description := "sbt plugin to generate and package Molecule boilerplate code",
    version := "1.19.4",
    organization := "org.scalamolecule",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.9.0",
      "org.scalamolecule" %% "molecule-base" % "0.24.2",

      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "upickle" % "4.2.1",

      "dev.zio" %% "zio" % "2.1.17",
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-tools" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-client" % calibanVersion,

      "com.lihaoyi" %% "utest" % "0.8.5" % Test
    ),

    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
  .settings(publishSettings)

lazy val publishSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
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