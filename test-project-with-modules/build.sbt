import sbt.Keys.{exportJars, testFrameworks, _}

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "sbt-molecule-test-project-with-modules",
  version := "1.8.0",
  organization := "org.scalamolecule",
  scalaVersion := "2.13.14",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule-sql-h2" % "0.9.0",
    "com.lihaoyi" %% "utest" % "0.9.0",
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
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeMakeJars := true, // Optional, default: true

    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )