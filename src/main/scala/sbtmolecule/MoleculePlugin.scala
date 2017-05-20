package sbtmolecule

import sbt.Keys._
import sbt._

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin

  object autoImport {
    lazy val moleculeSchemas         = settingKey[Seq[String]]("Seq of paths to directories having a schema directory with Molecule Schema definition files.")
    lazy val moleculeSeparateInFiles = settingKey[Boolean]("Whether boilerplate code for input files should reside in separate files (good for very large namespaces) - default: false")
    lazy val moleculeAllIndexed      = settingKey[Boolean]("Whether all attributes have the index flag in schema creation file - default: true")
    lazy val moleculeBoilerplate     = taskKey[Seq[File]]("Task that generates Molecule boilerplate code.")
    lazy val moleculeJars            = taskKey[Unit]("Task that packages the boilerplate code and then removes it.")
  }
  import autoImport._

  def moleculeScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    moleculeBoilerplate := {

      // Optional settings
      val separateInFiles = moleculeSeparateInFiles.?.value getOrElse false
      val allIndexed = moleculeAllIndexed.?.value getOrElse true

      // generate source files
      val sourceFiles = MoleculeBoilerplate(scalaSource.value, sourceManaged.value, moleculeSchemas.value, separateInFiles, allIndexed)

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
    //    moleculeJars := Def.taskDyn { makeJars()}.value
    moleculeJars <<= makeJars()
    //    moleculeJars := makeJars().value
  ))

  override def projectSettings: Seq[Def.Setting[_]] = moleculeScopedSettings(Compile)


  def makeJars() = Def.task {
    val domainDirs = moleculeSchemas.value
    val sourceDir = (sourceManaged in Compile).value
    val targetDir = (classDirectory in Compile).value
    val moduleDirName = baseDirectory.value.toString.split("/").last
    val transferDirs = moleculeSchemas.value.flatMap(dir => Seq(s"$dir/dsl/", s"$dir/schema"))

    // Create jar from generated source files
    val srcJar = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + "-sources.jar/")
    val srcFilesData = files2TupleRec("", sourceDir, ".scala", transferDirs)
    sbt.IO.jar(srcFilesData, srcJar, new java.util.jar.Manifest)

    // Create jar from class files compiled from generated source files
    val targetJar = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + ".jar/")
    val targetFilesData = files2TupleRec("", targetDir, ".class", transferDirs)
    sbt.IO.jar(targetFilesData, targetJar, new java.util.jar.Manifest)

    // Cleanup now obsolete generated code
    domainDirs.foreach { dir =>
      sbt.IO.delete(sourceDir / dir)
      sbt.IO.delete(targetDir / dir)
    }
  }.triggeredBy(compile in Compile)


  def files2TupleRec(path: String, directory: File, tpe: String, transferDirs: Seq[String]): Seq[Tuple2[File, String]] = {
    sbt.IO.listFiles(directory) flatMap {
      case file if file.isFile &&
        file.name.endsWith(tpe) &&
        transferDirs.exists(path.startsWith(_)) &&
        !file.name.endsWith(s"Definition$tpe") &&
        !file.name.endsWith(s"Definition$$$tpe")
      => Seq((file, s"$path${file.getName}"))
      case otherFile if otherFile.isFile        => Nil
      case dir                                  => files2TupleRec(s"$path${dir.getName}/", dir, tpe, transferDirs)
    }
  }
}
