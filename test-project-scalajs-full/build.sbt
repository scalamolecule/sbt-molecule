

name := "sbt-molecule-test-project-scalajs-full"

scalaVersion in ThisBuild := "2.13.4"

lazy val moleculeVersion = "0.24.0-SNAPSHOT"

lazy val foo = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := "foo",
    version := "0.2",
    resolvers ++= Seq(
      ("clojars" at "http://clojars.org/repo").withAllowInsecureProtocol(true),
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.7.4" % Test,
      "com.lihaoyi" %%% "scalatags" % "0.9.1",
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jsSettings(
    // Turn project into an application that can be `run`
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      ("org.scalamolecule" %%% "molecule" % moleculeVersion)
        .exclude("com.datomic", "datomic-free")
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalamolecule" %% "molecule" % moleculeVersion,
      "com.datomic" % "datomic-free" % "0.9.5697",
      "org.specs2" %% "specs2-core" % "4.10.5"
    )
  )
  .enablePlugins(MoleculePlugin)
  .settings(
    // Generate Molecule boilerplate code with `sbt clean compile -Dmolecule=true`
    moleculePluginActive := sys.props.get("molecule") == Some("true"),
    moleculeDataModelPaths := Seq("app"),
    moleculeMakeJars := true,

//    // Find scala version specific jars in respective libs
//    unmanagedBase := {
//      CrossVersion.partialVersion(scalaVersion.value) match {
//        case Some((2, 13)) => file(unmanagedBase.value.getPath ++ "/2.13")
//        case _             => file(unmanagedBase.value.getPath ++ "/2.12")
//      }
//    }
  )

lazy val fooJS  = foo.js
lazy val fooJVM = foo.jvm


// Let IDE detect created jars in unmanaged lib directory
exportJars := true