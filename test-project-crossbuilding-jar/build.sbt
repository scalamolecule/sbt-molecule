
name := "sbt-molecule-test-project-crossbuilding-jar"
version := "1.9.1"
organization := "org.scalamolecule"
crossScalaVersions := Seq("2.12.20", "2.13.14", "3.3.3")
ThisBuild / scalaVersion := "2.13.14"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

libraryDependencies ++= Seq(
  "org.scalamolecule" %% "molecule-sql-h2" % "0.10.1",
  "com.lihaoyi" %% "utest" % "0.8.3",
)

testFrameworks += new TestFramework("utest.runner.Framework")

// Find scala version specific jars in respective libs
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
moleculeMakeJars := true // Optional, default: true
