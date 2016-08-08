lazy val root = (project in file("."))
  .settings(
    name := "sbt-molecule",
    description := "sbt plugin to generate and package Molecule boilerplate code",
    version := "0.2.0",
    organization in ThisBuild := "org.scalamolecule",
    //    scalaVersion := "2.11.8",
    sbtPlugin := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    licenses := Seq("MIT License" -> url("https://github.com/scalamolecule/sbt-molecule/blob/master/LICENSE.txt"))
  )
  .settings(publishSettings)


lazy val snapshots = "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
lazy val releases = "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo <<= version((v: String) => Some(if (v.trim endsWith "SNAPSHOT") snapshots else releases)),
  publishArtifact in Test := false,
  pomIncludeRepository := (_ => false),
  homepage := Some(url("http://scalamolecule.org")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://github.com/scalamolecule/sbt-molecule"), "scm:git:git@github.com:scalamolecule/sbt-molecule.git")),
  pomExtra :=
    <developers>
      <developer>
        <id>marcgrue</id>
        <name>Marc Grue</name>
        <url>http://marcgrue.com</url>
      </developer>
    </developers>
)
