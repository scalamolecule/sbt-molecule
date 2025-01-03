import sbt.Keys.{exportJars, testFrameworks, _}

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "sbt-molecule-test-project-with-modules",
  version := "1.11.2",
  organization := "org.scalamolecule",
  scalaVersion := "2.13.15",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule-sql-h2" %  "0.15.2",
    "org.scalameta" %% "munit" % "1.0.3" % Test,
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  Test / parallelExecution := false,
  Test / fork := true,
)

lazy val root = project.in(file("."))
  .aggregate(app)
  .settings(commonSettings)

lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(commonSettings)
  .settings(
    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDomainPaths := Seq("app/domain"),
  )