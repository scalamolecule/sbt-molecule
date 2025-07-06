package sbtmolecule

import java.io.File
import java.util.jar.Manifest
import sbt.*
import sbt.Keys.*
import sbt.plugins.JvmPlugin

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  object autoImport {
    // Each command cleans previously generated source files and jars
    lazy val moleculeGen     = taskKey[Unit]("Generate Molecule source files.")
    lazy val moleculePackage = taskKey[Unit]("Generate Molecule source files, compile and package jars to lib/")
  }

  import autoImport.*


  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    // Make sure generated sources are discoverable
    Compile / unmanagedSourceDirectories += sourceManaged.value,

    moleculeGen := generateSources(Compile).value,

    moleculePackage := Def.taskDyn {
      val pkgs = generateSources(Compile).value.map(_._1)
      Def.sequential( // make sure generated sources are fully compiled before packaging jars
        Compile / compile,
        packJars(Compile, pkgs),
        cleanGeneratedSources(Compile),
      )
    }.value
  )

  private def generateSources(config: Configuration): Def.Initialize[Task[Seq[(String, String)]]] = Def.task {
    val log          = streams.value.log
    val srcDir       = getSrcDir(config).value
    val srcManaged   = (config / sourceManaged).value / "moleculeGen"
    val resourcesDir = (config / resourceDirectory).value / "moleculeGen"

    // Clear previously generated jar files to avoid conflicts
    val baseDir                                               = baseDirectory.value
    val (isJvm, platformPrefix, module, base, clsJar, srcJar) = getCoordinates(config).value

    val srcPath = s"${platformPrefix}target/scala-${scalaVersion.value}/src_managed/main/moleculeGen/"
    log.info(s"Generating Molecule DSL source files in $srcPath ...")
    if (isJvm) {
      log.info(s"Generating Molecule database schema files in resources/moleculeGen/ ...")
    }

    val (clsJarFile, srcJarFile) = (baseDir / clsJar, baseDir / srcJar)
    if (clsJarFile.exists()) {
      IO.delete(clsJarFile)
      log.success(s"Deleted previous lib/${clsJarFile.getName}")
    }
    if (srcJarFile.exists()) {
      IO.delete(srcJarFile)
      log.success(s"Deleted previous lib/${srcJarFile.getName}")
    }

    // Clear previously generated files in moleculeGen namespaces
    IO.delete(srcManaged)
    IO.delete(resourcesDir)
    IO.createDirectory(srcManaged)
    IO.createDirectory(resourcesDir)

    // Generate new sources and resources
    val pkgDomains = parseAndGenerate(srcManaged, resourcesDir, IO.listFiles(srcDir), Nil)

    if (isJvm && pkgDomains.nonEmpty) {
      // Only render once
      if (pkgDomains.nonEmpty) {
        log.success(s"Generated Molecule boilerplate files for:")
        pkgDomains.foreach { case (pkg, domain) =>
          log.success(s"  $pkg.$domain")
        }
      } else {
        log.info(s"Found no Molecule definitions in $module")
      }
    }
    pkgDomains
  }

  // Recursively find and parse all molecule definition files and generate dsl files
  private def parseAndGenerate(
    srcManaged: File,
    resourcesDir: File,
    files: Seq[File],
    pkgDomains: Seq[(String, String)]
  ): Seq[(String, String)] = {
    files.flatMap {
      case file if file.isFile =>
        if (file.name.endsWith(".scala"))
          ParseAndGenerate(file.getPath).generate(srcManaged, resourcesDir)
        else
          None

      case dir => parseAndGenerate(srcManaged, resourcesDir, IO.listFiles(dir), pkgDomains)
    }
  }


  private def cleanGeneratedSources(config: Configuration): Def.Initialize[Task[Unit]] = Def.task {
    // Clear newly generated source files in moleculeGenerated namespace only
    IO.delete((config / sourceManaged).value / "moleculeGen")
  }


  private def getSrcDir(config: Configuration): Def.Initialize[Task[File]] = Def.task {
    val parent = (config / baseDirectory).value.getParentFile
    if ((parent / "shared").exists())
      parent / "shared/src/main/scala"
    else if ((parent / ".js").exists())
      parent / "src/main/scala"
    else
      (config / scalaSource).value
  }


  private def getCoordinates(
    config: Configuration
  ): Def.Initialize[Task[(Boolean, String, String, String, String, String)]] = Def.task {
    val baseDir = (config / baseDirectory).value
    val parent  = baseDir.getParentFile.getName
    val last    = baseDir.getName

    val (isJvm, platformPrefix, module, base, jarIdentifier) = last match {
      case ".js" | ".jvm" => (last.last == 'm', s"$last/", parent, s"$parent/", parent + "-" + last.tail)
      case "js" | "jvm"   => (last.last == 'm', s"$last/", parent, s"$parent/", parent + "-" + last)
      case projectRootDir =>
        val base = if (baseDir.getCanonicalPath != (ThisBuild / baseDirectory).value.getCanonicalPath)
          thisProject.value.id + "/"
        else "" // when no explicit project name
        (true, "", last, base, projectRootDir)
    }
    (
      isJvm,
      platformPrefix,
      module,
      base,
      s"lib/molecule-$jarIdentifier.jar",
      s"lib/molecule-$jarIdentifier-sources.jar"
    )
  }


  private def packJars(config: Configuration, pkgs: Seq[String]): Def.Initialize[Task[Unit]] = Def.task {
    val log                                           = streams.value.log
    val baseDir                                       = baseDirectory.value
    val (isJvm, platformPrefix, _, _, clsJar, srcJar) = getCoordinates(config).value
    val dslSchemaDirs: Seq[String]                    = pkgs
      .map(_.split('.').mkString("/"))
      .flatMap(path => Seq(s"$path/dsl/", s"$path/schema"))

    val platform = if (platformPrefix.isEmpty) "" else if (isJvm) " jvm" else " js"

    // Create jar from class files (classes are not namespaced with /moleculeGen/)
    val clsDir   = (config / classDirectory).value
    val clsFiles = path2files("", clsDir, ".class", dslSchemaDirs)
    IO.jar(clsFiles, baseDir / clsJar, new Manifest, None)
    log.success(s"Packaged Molecule$platform class files in  $clsJar")

    // Create jar from scala source files
    val srcDir   = (config / sourceManaged).value / "moleculeGen"
    val srcFiles = path2files("", srcDir, ".scala", dslSchemaDirs)
    IO.jar(srcFiles, baseDir / srcJar, new Manifest, None)
    log.success(s"Packaged Molecule$platform source files in $srcJar")
  }

  private def path2files(
    path: String, directory: File, tpe: String, dslSchemaDirs: Seq[String]
  ): Seq[(File, String)] = {
    IO.listFiles(directory) flatMap {
      case file if file.isFile &&
        (file.name.endsWith(tpe) || file.name.endsWith(".sjsir") || file.name.endsWith(".tasty")) &&
        dslSchemaDirs.exists(path.startsWith) &&
        !file.name.endsWith(s"Domain$tpe") => Seq((file, s"$path${file.getName}"))
      case otherFile if otherFile.isFile   => Nil
      case dir                             => path2files(s"$path${dir.getName}/", dir, tpe, dslSchemaDirs)
    }
  }
}
