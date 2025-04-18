package sbtmolecule

import java.util.jar.Manifest
import sbt.Keys.*
import sbt.plugins.JvmPlugin
import sbt.{Def, *}


object MoleculePlugin extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  object autoImport {
    // api
    lazy val moleculeDomainPaths  = settingKey[Seq[String]](
      "Seq of paths to directories with Domain definition files."
    )
    lazy val moleculePluginActive = settingKey[Boolean](
      "Only generate sources/jars if true. Defaults to false to avoid re-generating on all project builds."
    )
    lazy val moleculeMakeJars     = settingKey[Boolean](
      "Whether jars are created from generated source files."
    )

    // Internal
    lazy val moleculeBoilerplate = taskKey[Seq[File]](
      "Internal task that generates Molecule boilerplate code."
    )
    lazy val moleculeJars        = taskKey[Unit](
      "Internal task that packages the boilerplate code and then removes it."
    )
  }

  import autoImport.*

  private def moleculeScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    moleculeBoilerplate := {
      if (moleculePluginActive.?.value.getOrElse(false)) {

        // generate source files
        val baseDir = baseDirectory.value.toString
        val last    = baseDir.split('/').last
        val isJvm   = last != ".js" && last != "js"
        val srcDir  = last match {
          case ".js" | ".jvm" =>
            // todo: hack - is there a way to get this correctly from sbt?
            // ScalaJS project, use data model in shared `src` folder on top level:
            // <project-path/foo/.jvm|.js/src/main/scala  // nothing here to generate from. So we change to..
            // <project-path/foo/src/main/scala           // our data models should be here
            file(baseDir.split('/').init.mkString("/") + "/src/main/scala")
          case "js" | "jvm"   =>
            // todo: hack - is there a way to get this correctly from sbt?
            // ScalaJS project, use data model in `shared` folder on top level:
            // <project-path/foo/jvm|js/src/main/scala  // nothing here to generate from. So we change to..
            // <project-path/foo/shared/src/main/scala  // our data models should be here
            file(baseDir.split('/').init.mkString("/") + "/shared/src/main/scala")
          case _              =>
            // Non-ScalaJS project
            scalaSource.value
        }

        val paths = moleculeDomainPaths.?.value.getOrElse(Nil)
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
          s"""----------------------------------------------------------------------------
             |Generating Molecule DSL $platform $codeOrJars for Domain structure definitions in:
             |${paths.mkString("\n")}
             |----------------------------------------------------------------------------""".stripMargin

        )

        val sourceFiles = FileBuilder(srcDir, sourceManaged.value, moleculeDomainPaths.value)

        // Avoid re-generating boilerplate if nothing has changed when running `sbt compile`
        val cacheDir = streams.value.cacheDirectory / "moleculeBoilerplateTesting"
        val cache    = FileFunction.cached(
          cacheDir,
          inStyle = FilesInfo.lastModified,
          outStyle = FilesInfo.hash
        ) {
          (in: Set[File]) => sourceFiles.toSet
        }
        cache(sourceFiles.toSet).toSeq

      } else {
        // Plugin not active - do nothing
        Seq.empty[File]
      }
    },

    Compile / sourceGenerators += (Compile / moleculeBoilerplate).taskValue,

    // Make sure generated source are discoverable for jar creations
    Compile / unmanagedSourceDirectories += sourceManaged.value,

    Compile / moleculeJars := Def.taskDyn {
      if (moleculePluginActive.?.value.getOrElse(false) && moleculeMakeJars.?.value.getOrElse(true)) {
        makeJars()
      } else {
        // Make no jars
        Def.task {}
      }
    }.triggeredBy(Compile / compile).value,
  ))


  override def projectSettings: Seq[Def.Setting[?]] = moleculeScopedSettings(Compile)


  private def makeJars(): Def.Initialize[Task[Unit]] = Def.task {
    val pathSegments      = baseDirectory.value.toString.split("/")
    val last              = pathSegments.last
    val jarFileIdentifier = last match {
      case ".js" | ".jvm" => pathSegments.init.last + "-" + last.replace(".", "")
      case "js" | "jvm"   => pathSegments.init.last + "-" + last
      case _              => last
    }
    val transferDirs      = moleculeDomainPaths.value.flatMap(path => Seq(s"$path/dsl/", s"$path/schema"))

    // Create source jar from generated source files
    val src_managedDir = sourceManaged.value
    val srcJar         = new File(baseDirectory.value + s"/lib/molecule-$jarFileIdentifier-sources.jar")
    val srcFilesData   = files2TupleRec("", src_managedDir, ".scala", transferDirs)
    sbt.IO.jar(srcFilesData, srcJar, new Manifest, None)

    // Create jar from class files compiled from generated source files
    val classesDir      = classDirectory.value
    val targetJar       = new File(baseDirectory.value + s"/lib/molecule-$jarFileIdentifier.jar")
    val targetFilesData = files2TupleRec("", classesDir, ".class", transferDirs)
    sbt.IO.jar(targetFilesData, targetJar, new Manifest, None)

    // Hack to allow the above jars to be created in parallel before source code is deleted
    // todo: how can we determine when it's safe to delete the generated source files?
    Thread.sleep(5000)

    // Cleanup now obsolete generated/compiled code
    moleculeDomainPaths.value.foreach { path =>
      // Delete class files compiled from generated source files
      // Leave other class files in paths untouched
      sbt.IO.delete(classesDir / path / "dsl")
      sbt.IO.delete(classesDir / path / "schema")

      // Delete all generated source files
      sbt.IO.delete(src_managedDir / path)
    }
  }

  private def files2TupleRec(
    path: String, directory: File, tpe: String, transferDirs: Seq[String]
  ): Seq[(File, String)] = {
    sbt.IO.listFiles(directory) flatMap {
      case file if file.isFile &&
        (file.name.endsWith(tpe) || file.name.endsWith(".sjsir") || file.name.endsWith(".tasty")) &&
        transferDirs.exists(path.startsWith) &&
        !file.name.endsWith(s"Domain$tpe") => Seq((file, s"$path${file.getName}"))
      case otherFile if otherFile.isFile   => Nil
      case dir                             => files2TupleRec(s"$path${dir.getName}/", dir, tpe, transferDirs)
    }
  }
}
