import sbt.Keys.{mainClass, testFrameworks, _}

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "sbt-molecule-test-project-with-modules-deep",
  version := "1.9.1",
  organization := "org.scalamolecule",
  scalaVersion := "2.13.14",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule-sql-h2" % "0.10.1",
    "com.lihaoyi" %% "utest" % "0.8.3",
  ),
  testFrameworks += new TestFramework("utest.runner.Framework")
)


lazy val root = project.in(file("."))
  .aggregate(app)
  .settings(commonSettings)


lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(commonSettings)
  .settings(
    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDataModelPaths := Seq(
      "app/domains/dataModel",
      "app/domains/nested/dataModel",
    ), // Mandatory
    moleculeMakeJars := true, // Optional, default: true
  )