package sbtmolecule

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{CrossVersion, Def, _}

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  object autoImport {
    // api
    lazy val moleculeDataModelPaths    = settingKey[Seq[String]]("Seq of paths to directories having a `dataModel` directory with data model files.")
    lazy val moleculePluginActive      = settingKey[Boolean]("Only generate sources/jars if true. Defaults to false to avoid re-generating on all project builds.")
    lazy val moleculeMakeJars          = settingKey[Boolean]("Whether jars are created from generated source files.")
    lazy val moleculeSchemaConversions = settingKey[Boolean]("Generate schema to lower/upper conversions. Default false.")
    lazy val moleculeAllIndexed        = settingKey[Boolean]("Whether all attributes have the index flag in schema creation file - default: true")
    lazy val moleculeGenericPkg        = settingKey[String]("Generate special generic interfaces in certain pkg. Not for public use.")

    // Internal
    lazy val moleculeBoilerplate = taskKey[Seq[File]]("Internal task that generates Molecule boilerplate code.")
    lazy val moleculeJars        = taskKey[Unit]("Internal task that packages the boilerplate code and then removes it.")
  }

  import autoImport._

  def moleculeScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    moleculeBoilerplate := {
      //      println(
      //        s"""-------------------------
      //           |${unmanagedBase.value}
      //           |${unmanagedClasspath.value}
      //           |${managedClasspath.value}
      //           |//{fullClasspath.value}
      //           |-------------------------""".stripMargin
      //      )

      if (moleculePluginActive.?.value.getOrElse(false)) {
        // Optional settings
        val allIndexed = moleculeAllIndexed.?.value getOrElse true
        val genericPkg = moleculeGenericPkg.?.value getOrElse ""

        // generate source files
        val baseDir = baseDirectory.value.toString
        val last    = baseDir.split('/').last
        val isJvm   = last != ".js" && last != "js"
        val srcDir  = last match {
          case ".js" | ".jvm" =>
            // todo: Ugly hack - is there a way to get this correctly from sbt?
            // ScalaJS project, use data model in shared `src` folder on top level:
            // <project-path/foo/.jvm|.js/src/main/scala  // nothing here to generate from. So we change to..
            // <project-path/foo/src/main/scala           // our data models should be here
            file(baseDir.split('/').init.mkString("/") + "/src/main/scala")
          case "js" | "jvm"   =>
            // todo: Ugly hack - is there a way to get this correctly from sbt?
            // ScalaJS project, use data model in `shared` folder on top level:
            // <project-path/foo/jvm|js/src/main/scala  // nothing here to generate from. So we change to..
            // <project-path/foo/shared/src/main/scala  // our data models should be here
            file(baseDir.split('/').init.mkString("/") + "/shared/src/main/scala")
          case _              =>
            // Non-ScalaJS project
            scalaSource.value
        }

        val paths = moleculeDataModelPaths.?.value.getOrElse(Nil)
        paths.foreach { path =>
          val fullPath = srcDir + "/" + path
          val dir      = new java.io.File(fullPath)
          if (!dir.isDirectory)
            throw new RuntimeException(
              "[sbt-molecule plugin] Data model path defined in sbt build file is not a directory:\n" + fullPath
            )
        }

        val platform   = if (isJvm) "jvm" else "js"
        val codeOrJars = if (moleculeMakeJars.?.value.getOrElse(true)) "jars" else "source code"
        println(
          s"""------------------------------------------------------------------------
             |Generating Molecule DSL $platform $codeOrJars for data models in:
             |${paths.mkString("\n")}
             |------------------------------------------------------------------------""".stripMargin
        )

        //        println(
        //          s"""-------------------------
        //             |${baseDirectory.value}
        //             |${sourceDirectory.value}
        //             |${scalaSource.value}
        //             |
        //             |$last
        //             |$srcDir
        //             |$isJvm
        //             |-------------------------""".stripMargin
        //        )

        val sourceFiles = FileBuilder(
          srcDir,
          sourceManaged.value,
          moleculeDataModelPaths.value,
          allIndexed,
          isJvm,
          genericPkg,
          moleculeSchemaConversions.?.value.getOrElse(false),
        )

        //        println(
        //          s"""${sourceFiles.mkString("\n")}
        //             |-------------------------""".stripMargin
        //        )

        // Avoid re-generating boilerplate if nothing has changed when running `sbt compile`
        val cacheDir = streams.value.cacheDirectory / "moleculeBoilerplateTesting"
        val cache    = FileFunction.cached(
          cacheDir,
          inStyle = FilesInfo.lastModified,
          outStyle = FilesInfo.hash
        ) {
          in: Set[File] => sourceFiles.toSet
        }
        cache(sourceFiles.toSet).toSeq

      } else {
        // Plugin not active - do nothing
        Seq.empty[File]
      }
    },

    Compile / sourceGenerators += moleculeBoilerplate.taskValue,

    Compile / moleculeJars := Def.taskDyn {
      if (moleculePluginActive.?.value.getOrElse(false) && moleculeMakeJars.?.value.getOrElse(true)) {
        makeJars()
      } else {
        // Make no jars
        Def.task {}
      }
    }.value
  ))


  override def projectSettings: Seq[Def.Setting[_]] = moleculeScopedSettings(Compile)


  def makeJars(): Def.Initialize[Task[Unit]] = Def.task {
    val moduleDirName: String      = baseDirectory.value.toString.split("/").last.replace(".", "")
    val transferDirs : Seq[String] = moleculeDataModelPaths.value.flatMap(path => Seq(s"$path/dsl/", s"$path/schema"))
    val cross        : String      = if (crossScalaVersions.value.size == 1) "" else {
      val v = CrossVersion.partialVersion(scalaVersion.value).get
      s"/${v._1}.${v._2}"
    }

    // Create source jar from generated source files
    val src_managedDir: File                = (Compile / sourceManaged).value
    val srcJar        : File                = new File(baseDirectory.value + s"/lib$cross/molecule-$moduleDirName-sources.jar/")
    val srcFilesData  : Seq[(File, String)] = files2TupleRec("", src_managedDir, ".scala", transferDirs)
    sbt.IO.jar(srcFilesData, srcJar, new java.util.jar.Manifest, None)

    // Create jar from class files compiled from generated source files
    val classesDir     : File                = (Compile / classDirectory).value
    val targetJar      : File                = new File(baseDirectory.value + s"/lib$cross/molecule-$moduleDirName.jar/")
    val targetFilesData: Seq[(File, String)] = files2TupleRec("", classesDir, ".class", transferDirs)
    sbt.IO.jar(targetFilesData, targetJar, new java.util.jar.Manifest, None)

    // Cleanup now obsolete generated/compiled code
    moleculeDataModelPaths.value.foreach { path =>
      // Delete class files compiled from generated source files
      // Leave other class files in paths untouched
      sbt.IO.delete(classesDir / path / "dsl")
      sbt.IO.delete(classesDir / path / "schema")

      // Delete all generated source files
      sbt.IO.delete(src_managedDir / path)
    }
  }


  def files2TupleRec(path: String, directory: File, tpe: String, transferDirs: Seq[String]): Seq[(File, String)] = {
    sbt.IO.listFiles(directory) flatMap {
      case file if file.isFile &&
        (file.name.endsWith(tpe) || file.name.endsWith(".sjsir")) &&
        transferDirs.exists(path.startsWith) &&
        !file.name.endsWith(s"DataModel$tpe") &&
        !file.name.endsWith(s"DataModel$$$tpe") => Seq((file, s"$path${file.getName}"))
      case otherFile if otherFile.isFile        => Nil
      case dir                                  => files2TupleRec(s"$path${dir.getName}/", dir, tpe, transferDirs)
    }
  }
}
