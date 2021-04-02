
name := "sbt-molecule-test-project-scalajs-full"

lazy val foo = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "foo",
    version := "0.2",
    scalaVersion in ThisBuild := "2.13.5",
    resolvers ++= Seq(
      "clojars" at "https://clojars.org/repo",
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.7.7" % Test,
      "com.lihaoyi" %%% "scalatags" % "0.9.3",
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),

    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule").contains("true"),
    moleculeDataModelPaths := Seq("app"),

    // Let IDE detect generated jars in unmanaged lib directory
    exportJars := true
  )
  .jsSettings(
    // Molecule without non-ScalaJS-compatible Datomic dependencies
    libraryDependencies ++= Seq(
      ("org.scalamolecule" %%% "molecule" % "0.25.2-SNAPSHOT")
        .exclude("com.datomic", "datomic-free")
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % "0.25.2-SNAPSHOT",
      "com.datomic" % "datomic-free" % "0.9.5697"
    )
  )

lazy val fooJS  = foo.js
lazy val fooJVM = foo.jvm