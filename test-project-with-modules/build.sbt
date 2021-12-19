import sbt.Keys.{exportJars, _}

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "sbt-molecule-test-project-with-modules",
  version := "1.0.1",
  organization := "org.scalamolecule",
  scalaVersion := "2.13.7",
  scalacOptions := Seq("-feature", "-language:implicitConversions", "-Yrangepos"),
  resolvers ++= Seq(
    "clojars" at "https://clojars.org/repo",
  ),
  libraryDependencies ++= Seq(
    "org.scalamolecule" %% "molecule" % "1.0.1",
    "com.datomic" % "datomic-free" % "0.9.5697",
    "org.specs2" %% "specs2-core" % "4.10.6"
  )
)


lazy val root = project.in(file("."))
  .aggregate(app)
  .settings(commonSettings)


lazy val app = (project in file("app"))
  .enablePlugins(MoleculePlugin)
  .settings(commonSettings)
  .settings(
    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    moleculeDataModelPaths := Seq("app"), // Mandatory
    moleculeAllIndexed := true, // Optional, default: true
    moleculeMakeJars := true, // Optional, default: true

    // Let IDE detect created jars in unmanaged lib directory
    exportJars := true
  )