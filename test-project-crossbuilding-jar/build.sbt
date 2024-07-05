
name := "sbt-molecule-test-project-crossbuilding-jar"
version := "1.8.0"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.19", "2.13.14")
ThisBuild / scalaVersion := "2.13.14"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.9.0",
  "com.lihaoyi" %% "utest" % "0.9.0",
)

testFrameworks += new TestFramework("utest.runner.Framework")

// Ensure clojure loads correctly for async tests run from sbt
Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

// Find scala version specific jars in respective libs
unmanagedBase := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
    case _             => file(unmanagedBase.value.getPath ++ "/2.12")
  }
}

// Molecule
enablePlugins(MoleculePlugin)

// Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
moleculePluginActive := sys.props.get("molecule").contains("true")
moleculeDataModelPaths := Seq("app") // Mandatory
moleculeMakeJars := true // Optional, default: true

// Let IDE detect created jars in unmanaged lib directory
exportJars := true
