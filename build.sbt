lazy val root = (project in file("."))
  .settings(
    name := "sbt-molecule",
    description := "sbt plugin to generate and package Molecule boilerplate code",
    version := "0.1.0",
    organization in ThisBuild := "org.scalamolecule",
    sbtPlugin := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    licenses := Seq("MIT License" -> url("https://github.com/scalamolecule/sbt-molecule/blob/master/LICENSE"))
  )