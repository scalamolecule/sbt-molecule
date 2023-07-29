import sbt.Keys.{mainClass, testFrameworks, _}

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "sbt-molecule-test-project-with-modules-deep",
  version := "1.1.0",
  organization := "org.scalamolecule",
  scalaVersion := "2.13.8",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
  resolvers ++= Seq(
    "clojars" at "https://clojars.org/repo",
  ),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule-datalog-datomic" % "0.1.0",
    "com.lihaoyi" %% "utest" % "0.8.1",
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
      "app/domains",
      "app/domains/nested",
    ), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := true, // Optional, default: true

    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )