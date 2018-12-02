package sbtmolecule

import sbt.Keys._
import sbt._

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin

  object autoImport {
    lazy val moleculeSchemas     = settingKey[Seq[String]]("Seq of paths to directories having a schema directory with Molecule Schema definition files.")
    lazy val moleculeAllIndexed  = settingKey[Boolean]("Whether all attributes have the index flag in schema creation file - default: true")
    lazy val moleculeMakeJars    = settingKey[Boolean]("Whether jars are created from generated source files.")
    lazy val moleculeBoilerplate = taskKey[Seq[File]]("Task that generates Molecule boilerplate code.")
    lazy val moleculeJars        = taskKey[Unit]("Task that packages the boilerplate code and then removes it.")
  }
  import autoImport._

  def moleculeScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    moleculeBoilerplate := {

      // Optional settings
      val allIndexed = moleculeAllIndexed.?.value getOrElse true

      // generate source files
      val sourceFiles = FileBuilder(scalaSource.value, sourceManaged.value, moleculeSchemas.value, allIndexed)

      // Avoid re-generating boilerplate if nothing has changed when running `sbt compile`
      val cache = FileFunction.cached(
        streams.value.cacheDirectory / "moleculeBoilerplateTesting",
        inStyle = FilesInfo.lastModified,
        outStyle = FilesInfo.hash
      ) {
        in: Set[File] => sourceFiles.toSet
      }
      cache(sourceFiles.toSet).toSeq
    },
    sourceGenerators += moleculeBoilerplate.taskValue,
    moleculeJars := Def.taskDyn {
      if (moleculeMakeJars.?.value getOrElse true)
        makeJars()
      else
        Def.task {}
    }.triggeredBy(compile in Compile).value
  ))


  override def projectSettings: Seq[Def.Setting[_]] = moleculeScopedSettings(Compile)


  def makeJars() = Def.task {
    val moduleDirName: String = baseDirectory.value.toString.split("/").last
    val transferDirs: Seq[String] = moleculeSchemas.value.flatMap(dir => Seq(s"$dir/dsl/", s"$dir/schema"))

    // Create source jar from generated source files
    val sourceDir: File = (sourceManaged in Compile).value
    val srcJar: File = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + "-sources.jar/")
    val srcFilesData: Seq[(File, String)] = files2TupleRec("", sourceDir, ".scala", transferDirs)
    sbt.IO.jar(srcFilesData, srcJar, new java.util.jar.Manifest)

    // Create jar from class files compiled from generated source files
    val targetDir: File = (classDirectory in Compile).value
    val targetJar: File = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + ".jar/")
    val targetFilesData: Seq[(File, String)] = files2TupleRec("", targetDir, ".class", transferDirs)
    sbt.IO.jar(targetFilesData, targetJar, new java.util.jar.Manifest)

    // Cleanup now obsolete generated/compiled code
    moleculeSchemas.value.foreach { dir =>
      sbt.IO.delete(sourceDir / dir)
      sbt.IO.delete(targetDir / dir)
    }
  }


  def files2TupleRec(path: String, directory: File, tpe: String, transferDirs: Seq[String]): Seq[(File, String)] = {
    sbt.IO.listFiles(directory) flatMap {
      case file if file.isFile &&
        file.name.endsWith(tpe) &&
        transferDirs.exists(path.startsWith(_)) &&
        !file.name.endsWith(s"Definition$tpe") &&
        !file.name.endsWith(s"Definition$$$tpe") => Seq((file, s"$path${file.getName}"))
      case otherFile if otherFile.isFile         => Nil
      case dir                                   => files2TupleRec(s"$path${dir.getName}/", dir, tpe, transferDirs)
    }
  }
}
