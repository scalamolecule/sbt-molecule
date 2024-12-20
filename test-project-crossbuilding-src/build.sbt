
name := "sbt-molecule-test-project-crossbuilding-src"
version := "1.11.0"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.20", "2.13.15", "3.3.4")
ThisBuild / scalaVersion := "2.13.15"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.15.0",
  "com.lihaoyi" %% "utest" % "0.8.4",
)
testFrameworks += new TestFramework("utest.runner.Framework")

// Ensure clojure loads correctly for async tests run from sbt
//Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

unmanagedBase := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => file(unmanagedBase.value.getPath ++ "/2.12")
    case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
    case _             => file(unmanagedBase.value.getPath ++ "/3.3")
  }
}

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app/dataModel") // Mandatory
moleculeMakeJars := false // Optional, default: true
