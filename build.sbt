import sbt.url

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-molecule",
    description := "sbt plugin to generate and package Molecule boilerplate code",
    version := "1.2.0-SNAPSHOT",
    organization := "org.scalamolecule",
    libraryDependencies += "org.scalamolecule" %% "molecule-base" % "0.1.0-SNAPSHOT"
  )
  .settings(publishSettings)


lazy val snapshots = "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
lazy val releases  = "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

lazy val publishSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
  publishTo := (if (isSnapshot.value) Some(snapshots) else Some(releases)),
  Test / publishArtifact := false,
  pomIncludeRepository := (_ => false),
  homepage := Some(url("http://scalamolecule.org")),
  description := "sbt-molecule",
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/scalamolecule/sbt-molecule"),
    "scm:git:git@github.com:scalamolecule/sbt-molecule.git"
  )),
  developers := List(
    Developer(
      id = "marcgrue",
      name = "Marc Grue",
      email = "marcgrue@gmail.com",
      url = url("http://marcgrue.com")
    )
  )
)