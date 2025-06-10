package sbtmolecule

import java.util.jar.Manifest
import molecule.base.ast.MetaDomain
import molecule.base.error.ModelError
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

    moleculeGen := generateSources("source", Compile).value,

    moleculePackage := Def.taskDyn {
      val metaDomains = generateSources("source and jar", Compile).value
      Def.sequential( // make sure generated sources are fully compiled before packaging jars
        Compile / compile,
        packJars(Compile, metaDomains),
        cleanGeneratedSources(Compile),
      )
    }.value
  )


  private def generateSources(tpe: String, config: Configuration): Def.Initialize[Task[Seq[MetaDomain]]] = Def.task {
    val log = streams.value.log

    val srcDir      = getSrcDir(config).value
    val src_managed = (config / sourceManaged).value / "moleculeGen"

    // Clear previously generated jar files to avoid conflicts
    val baseDir                                               = baseDirectory.value
    val (isJvm, platformPrefix, module, base, clsJar, srcJar) = getCoordinates(config).value

    val metaDomains = files2metaDomains(IO.listFiles(srcDir), Nil)
    if (metaDomains.isEmpty)
      throw ModelError("No Domain definitions found in\n" + srcDir)

    if (isJvm) {
      // Only render once
      renderDomains(tpe, metaDomains, module)
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

    // Clear previously generated source files in moleculeGenerated namespace only
    IO.delete(src_managed)
    IO.createDirectory(src_managed)
    log.success(s"Deleted previous generated sources in src_managed/main/moleculeGen/$base")

    GenerateSources(src_managed, metaDomains)

    val srcPath = s"${platformPrefix}target/scala-${scalaVersion.value}/src_managed/main/moleculeGen/$base"
    log.success(s"Generated Molecule source files in $srcPath")

    metaDomains
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


  // Recursively find and parse all molecule definition files
  private def files2metaDomains(files: Seq[File], acc: Seq[MetaDomain]): Seq[MetaDomain] = {
    files.flatMap {
      case file if file.isFile =>
        if (file.name.endsWith(".scala"))
          ParseDefinitionFile(file.getPath).optMetaDomain else Nil

      case dir => files2metaDomains(IO.listFiles(dir), acc)
    }
  }


  private def renderDomains(tpe: String, metaDomains: Seq[MetaDomain], module: String): Unit = {
    val stats         = metaDomains.map { d =>
      val file          = s"${d.pkg}.${d.domain}"
      val segmentLength = d.segments.length
      val entityLength  = d.segments.map(_.ents.length).sum
      val attrLength    = d.segments.map(_.ents.map(_.attrs.filterNot(_.attr == "id").length).sum).sum
      val segmentCount  = (" " * (4 - segmentLength.toString.length)) + segmentLength
      val entityCount   = (" " * (11 - entityLength.toString.length)) + entityLength
      val attrCount     = (" " * (12 - attrLength.toString.length)) + attrLength
      (file, segmentCount, entityCount, attrCount)
    }
    val maxFileLength = stats.map(_._1.length).max
    val list          = stats.map { case (file, segmentCount, entityCount, attrCount) =>
      val pad = " " * (maxFileLength - file.length)
      s"$file $pad    $segmentCount$entityCount$attrCount"
    }.mkString("\n")
    val namePadding   = " " * (maxFileLength - "Domain".length)

    val segmentsLength = metaDomains.map(_.segments.length).sum
    val entitiesLength = metaDomains.map(_.segments.map(_.ents.length).sum).sum
    val attrsLength    = metaDomains.map(_.segments.map(_.ents.map(_.attrs.filterNot(_.attr == "id").length).sum).sum).sum
    val segmentsCount  = (" " * (4 - segmentsLength.toString.length)) + segmentsLength
    val entitiesCount  = (" " * (11 - entitiesLength.toString.length)) + entitiesLength
    val attrsCount     = (" " * (12 - attrsLength.toString.length)) + attrsLength

    println(
      s"""---------------------------------------------------------------------------------
         |Generating Molecule $tpe files for domains in '$module':
         |
         |${scala.Console.BOLD}Domain $namePadding   Segments   Entities   Attributes${scala.Console.RESET}
         |$list
         |       $namePadding   --------   --------   ----------
         |       $namePadding    $segmentsCount$entitiesCount$attrsCount
         |---------------------------------------------------------------------------------
         |""".stripMargin
    )
  }


  private def packJars(config: Configuration, metaDomains: Seq[MetaDomain]): Def.Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log

    val baseDir                                       = baseDirectory.value
    val (isJvm, platformPrefix, _, _, clsJar, srcJar) = getCoordinates(config).value

    val dslSchemaDirs: Seq[String] = metaDomains
      .map(_.pkg.split('.').mkString("/"))
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
