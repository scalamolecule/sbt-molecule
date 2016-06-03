package sbtmolecule

import sbt.Keys._
import sbt._

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin

  object autoImport {
    lazy val moleculeSchemas     = settingKey[Seq[String]]("Seq of paths to directories having a schema directory with Molecule Schema definition files.")
    lazy val moleculeBoilerplate = taskKey[Seq[File]]("Task that generates Molecule boilerplate code.")
    lazy val moleculeJars        = taskKey[Unit]("Task that packages the boilerplate code and then removes it.")
  }
  import autoImport._

  def moleculeScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    moleculeBoilerplate := MoleculeBoilerplate(scalaSource.value, sourceManaged.value, moleculeSchemas.value),
    sourceGenerators += moleculeBoilerplate.taskValue,
    moleculeJars <<= makeJars()
  ))

  override def projectSettings: Seq[Def.Setting[_]] = moleculeScopedSettings(Compile)


  def makeJars() = Def.task {
    val domainDirs = moleculeSchemas.value
    val sourceDir = (sourceManaged in Compile).value
    val targetDir = (classDirectory in Compile).value
    val moduleDirName = baseDirectory.value.toString.split("/").last

    // Create jar from generated source files
    val srcJar = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + "-sources.jar/")
    val srcFilesData = files2TupleRec("", sourceDir, ".scala")
    sbt.IO.jar(srcFilesData, srcJar, new java.util.jar.Manifest)

    // Create jar from class files compiled from generated source files
    val targetJar = new File(baseDirectory.value + "/lib/molecule-" + moduleDirName + ".jar/")
    val targetFilesData = files2TupleRec("", targetDir, ".class")
    sbt.IO.jar(targetFilesData, targetJar, new java.util.jar.Manifest)

    // Cleanup now obsolete generated code
    domainDirs.foreach { dir =>
      sbt.IO.delete(sourceDir / dir)
      sbt.IO.delete(targetDir / dir)
    }
  }.triggeredBy(compile in Compile)


  def files2TupleRec(pathPrefix: String, dir: File, tpe: String): Seq[Tuple2[File, String]] = {
    sbt.IO.listFiles(dir) flatMap { f =>
      if (f.isFile && f.name.endsWith(tpe))
        Seq((f, s"${pathPrefix}${f.getName}"))
      else
        files2TupleRec(s"${pathPrefix}${f.getName}/", f, tpe)
    }
  }
}
