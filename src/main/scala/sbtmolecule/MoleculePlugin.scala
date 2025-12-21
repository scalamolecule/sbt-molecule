package sbtmolecule

import java.io.File
import java.util.jar.Manifest
import sbt.*
import sbt.Keys.*
import sbt.internal.util.ManagedLogger
import sbt.plugins.JvmPlugin

object MoleculePlugin extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  object autoImport {
    lazy val moleculePackage         = taskKey[Unit]("Generate Molecule source files, compile and package jars to lib/")
    lazy val moleculeMigrationStatus = taskKey[Unit]("Show migration status for all domains.")
  }

  import autoImport.*
  import sbt.complete.DefaultParsers._

  private lazy val moleculeGenerateSources = taskKey[Seq[(String, String)]]("Internal task for generating sources")

  private lazy val moleculeGenCommand = Command.args("moleculeGen", "<args>") { (state, args) =>
    val extracted = Project.extract(state)
    import extracted._

    val log = state.log
    val config = Compile
    val baseDir = (config / baseDirectory).get(structure.data).getOrElse(file("."))
    val resourcesDir = (config / resourceDirectory).get(structure.data).getOrElse(baseDir / "src" / "main" / "resources")
    val srcDir = getSrcDirValue(state, config)
    val schemaDir = resourcesDir / "db" / "schema"
    val migrationDir = resourcesDir / "db" / "migration"

    // Check for subcommands
    args.toList match {
      case "initMigrations" :: domains =>
        log.info("Generating Molecule DSL source files...")
        executeTask(moleculeGenerateSources, state)
        handleInitMigrationsImpl(schemaDir, migrationDir, srcDir, resourcesDir, domains, log)

      case "deleteMigrations" :: domains =>
        handleDeleteMigrationsImpl(migrationDir, domains, log)

      case other :: _ =>
        log.error(s"Unknown subcommand: $other")
        log.error("Valid subcommands: initMigrations, deleteMigrations")
        log.error("")
        log.error("Usage from SBT shell:")
        log.error("  > moleculeGen")
        log.error("  > moleculeGen initMigrations")
        log.error("  > moleculeGen initMigrations Foo Bar")
        log.error("  > moleculeGen deleteMigrations Foo")
        log.error("")
        log.error("Usage from command line (requires quotes):")
        log.error("  sbt \"moleculeGen\"")
        log.error("  sbt \"moleculeGen initMigrations\"")
        log.error("  sbt \"moleculeGen initMigrations Foo Bar\"")
        log.error("  sbt \"moleculeGen deleteMigrations Foo\"")

      case Nil =>
        // No subcommand, run normal generation
        executeTask(moleculeGenerateSources, state)
    }

    state
  }

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    // Make sure generated sources are discoverable
    Compile / unmanagedSourceDirectories += sourceManaged.value,

    commands += moleculeGenCommand,

    moleculeGenerateSources := generateSources(Compile).value,

    moleculePackage := Def.taskDyn {
      val pkgs = moleculeGenerateSources.value.map(_._1)
      Def.sequential( // make sure generated sources are fully compiled before packaging jars
        Compile / compile,
        packJars(Compile, pkgs),
        cleanGeneratedSources(Compile),
      )
    }.value,

    moleculeMigrationStatus := showMigrationStatus(Compile).value
  )

  private def executeTask[T](taskKey: TaskKey[T], state: State): T = {
    val extracted = Project.extract(state)
    import extracted._
    EvaluateTask(structure, taskKey, state, currentRef) match {
      case Some((_, Value(result))) => result
      case Some((_, Inc(cause))) => throw new Exception(s"Task failed: $cause")
      case None => throw new Exception("Task evaluation failed")
    }
  }

  private def getSrcDirValue(state: State, config: Configuration): File = {
    val extracted = Project.extract(state)
    import extracted._
    val baseDir = (config / baseDirectory).get(structure.data).getOrElse(file("."))
    val parent = baseDir.getParentFile
    if ((parent / "shared").exists())
      parent / "shared/src/main/scala"
    else if ((parent / ".js").exists())
      parent / "src/main/scala"
    else
      (config / scalaSource).get(structure.data).getOrElse(baseDir / "src" / "main" / "scala")
  }

  private def generateSources(config: Configuration): Def.Initialize[Task[Seq[(String, String)]]] = Def.task {
    val log          = streams.value.log
    val srcDir       = getSrcDir(config).value
    val srcManaged   = (config / sourceManaged).value / "moleculeGen"
    val resourcesDir = (config / resourceDirectory).value / "db" / "schema"

    // Clear previously generated jar files to avoid conflicts
    val baseDir                                               = baseDirectory.value
    val (isJvm, platformPrefix, module, base, clsJar, srcJar) = getCoordinates(config).value

    val srcPath = s"${platformPrefix}target/scala-${scalaVersion.value}/src_managed/main/moleculeGen/"
    log.info(s"Generating Molecule DSL source files in $srcPath ...")
    if (isJvm) {
      log.info(s"Generating Molecule database schema files in resources/db/schema/ ...")
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
    val pkgDomains = parseAndGenerate(srcManaged, resourcesDir, IO.listFiles(srcDir), log)

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

  private def handleMigrationWithFile(
    file: File,
    srcManaged: File,
    schemaDir: File,
    parseAndGen: ParseAndGenerate,
    migrationFile: File,
    resourcesRoot: File,
    namespacePath: String,
    domain: String,
    log: ManagedLogger
  ): Unit = {
    // Parse migration file and get current domain structure
    val migrationMeta = ParseAndGenerate(migrationFile.getPath).generators.head.metaDomain
    val currentMeta   = parseAndGen.generators.head.metaDomain

    // Load previous structure
    GenerateSourceFiles_db.loadBeforeStructure(resourcesRoot, namespacePath, domain) match {
      case Some(previousMeta) =>
        // Apply migration commands from migration file to previous structure
        import sbtmolecule.migration.MigrationFileGenerator
        val beforeWithMigrations = MigrationFileGenerator.applyMigrationCommands(previousMeta, migrationMeta)

        // Annotate and generate migration
        import sbtmolecule.migration.MigrationDetector
        val annotatedBefore = MigrationDetector.annotateBefore(beforeWithMigrations, currentMeta, Some(domain))

        // Generate Flyway migration files
        GenerateSourceFiles_db.generateFlywayMigration(resourcesRoot, namespacePath, domain, annotatedBefore, currentMeta)

        // Delete migration file after successful generation
        IO.delete(migrationFile)

        // Prompt user to remove attributes marked with .remove from source
        import sbtmolecule.migration.AttributeRemover
        val wasCleanedUp = AttributeRemover.promptAndRemoveAttributes(file, namespacePath, annotatedBefore, msg => log.info(msg))

        // If cleanup happened (markers removed), regenerate boilerplate with cleaned structure
        if (wasCleanedUp) {
          log.info("Regenerating boilerplate with cleaned-up domain structure...")
          val cleanedParseAndGen = ParseAndGenerate(file.getPath)
          cleanedParseAndGen.generate(srcManaged, schemaDir)
        }

        // Save current as new before structure
        GenerateSourceFiles_db.saveBeforeStructure(resourcesRoot, namespacePath, domain, file)

      case None =>
        // This shouldn't happen - migration file exists but no previous structure
        throw new Exception(s"Migration file exists but no previous structure found for domain $domain")
    }
  }

  private def handleMigrationWithoutFile(
    file: File,
    srcManaged: File,
    schemaDir: File,
    parseAndGen: ParseAndGenerate,
    migrationFile: File,
    resourcesRoot: File,
    namespacePath: String,
    domain: String,
    pkg: String,
    log: ManagedLogger
  ): Unit = {
    // No migration file - check for ambiguous changes
    GenerateSourceFiles_db.loadBeforeStructure(resourcesRoot, namespacePath, domain) match {
      case Some(previousMeta) =>
        // Get current metadata
        val generators  = parseAndGen.generators
        val currentMeta = generators.head.metaDomain

        // Check for ambiguous changes
        import sbtmolecule.migration.MigrationDetector
        val migrationFilePath = s"src/main/scala/$namespacePath/${domain}_migration.scala"
        MigrationDetector.annotateBeforeSafe(previousMeta, currentMeta, Some(domain), Some(migrationFilePath)) match {
          case MigrationDetector.Success(annotatedBefore) =>
            // No ambiguities - proceed with migration if there are changes
            if (annotatedBefore.migrationStatus == molecule.base.metaModel.MigrationStatus.HasChanges) {
              // Generate Flyway migration files
              GenerateSourceFiles_db.generateFlywayMigration(resourcesRoot, namespacePath, domain, annotatedBefore, currentMeta)

              // Prompt user to remove attributes marked with .remove from source
              // Use currentMeta because it has the migration markers (.remove, .rename)
              import sbtmolecule.migration.AttributeRemover
              val wasCleanedUp = AttributeRemover.promptAndRemoveAttributes(file, namespacePath, currentMeta, msg => log.info(msg))

              // If cleanup happened (markers removed), regenerate boilerplate with cleaned structure
              if (wasCleanedUp) {
                log.info("Regenerating boilerplate with cleaned-up domain structure...")
                val cleanedParseAndGen = ParseAndGenerate(file.getPath)
                cleanedParseAndGen.generate(srcManaged, schemaDir)
              }
            }

            // Save current as new before structure
            GenerateSourceFiles_db.saveBeforeStructure(resourcesRoot, namespacePath, domain, file)

          case MigrationDetector.AmbiguityDetected(errorMessage) =>
            // Ambiguity detected - generate migration file and throw error
            import sbtmolecule.migration.MigrationFileGenerator

            if (!migrationFile.exists()) {
              val migrationMeta       = MigrationFileGenerator.migrationMetaDomain(
                previousMeta,
                currentMeta,
                s"${domain}_migration"
              )
              val migrationSourceBody = MigrationFileGenerator.migrationSource(
                migrationMeta,
                domain,
                previousMeta,
                currentMeta
              )
              // Add package declaration
              val migrationSource     = s"package $pkg\n\nimport molecule.DomainStructure\n\n$migrationSourceBody"
              IO.write(migrationFile, migrationSource)
            }

            throw new Exception(errorMessage)
        }

      case None =>
        // First run - only save if migrations are already initialized for this domain
        val migrationDir = resourcesRoot / "db" / "migration" / namespacePath / domain
        if (migrationDir.exists()) {
          GenerateSourceFiles_db.saveBeforeStructure(resourcesRoot, namespacePath, domain, file)
        }
        // Otherwise skip - migrations not enabled for this domain yet
    }
  }

  // Recursively find and parse all molecule definition files and generate dsl files
  private def parseAndGenerate(
    srcManaged: File,
    schemaDir: File,
    files: Seq[File],
    log: ManagedLogger
  ): Seq[(String, String)] = {
    files.flatMap {
      case file if file.isFile =>
        if (file.name.endsWith(".scala")) {
          val parseAndGen = ParseAndGenerate(file.getPath)
          val result      = parseAndGen.generate(srcManaged, schemaDir)

          // Check for migration ambiguities after parsing
          result.foreach { case (pkg, domain) =>
            // schemaDir points to resources/db/schema, go up two levels to get resources/
            val resourcesRoot = schemaDir.getParentFile.getParentFile
            val migrationFile = file.getParentFile / s"${domain}_migration.scala"
            val namespacePath = pkg.replace('.', '/')

            // Check if user has provided a migration file to resolve ambiguities
            if (migrationFile.exists()) {
              handleMigrationWithFile(file, srcManaged, schemaDir, parseAndGen, migrationFile, resourcesRoot, namespacePath, domain, log)
            } else {
              handleMigrationWithoutFile(file, srcManaged, schemaDir, parseAndGen, migrationFile, resourcesRoot, namespacePath, domain, pkg, log)
            }
          }
          result
        } else None
      case dir => parseAndGenerate(srcManaged, schemaDir, IO.listFiles(dir), log)
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

  private def handleInitMigrationsImpl(
    schemaDir: File,
    migrationDir: File,
    srcDir: File,
    resourcesDir: File,
    domains: List[String],
    log: ManagedLogger
  ): Unit = {

    if (!schemaDir.exists()) {
      log.error("No schema files found. Please run 'sbt moleculeGen' first to generate schemas.")
      throw new Exception("Cannot initialize migrations without schemas")
    }

    // Find all domains with schemas
    val allDomains = findDomainsFromSchemas(schemaDir)

    // Filter by specified domains if provided
    val targetDomains = if (domains.isEmpty) {
      allDomains
    } else {
      allDomains.filter { case (namespacePath, domain) =>
        val fullPath = s"${namespacePath.replace('/', '.')}.$domain"
        domains.exists(d => fullPath.endsWith(d))
      }
    }

    if (targetDomains.isEmpty) {
      if (domains.isEmpty) {
        log.info("No domains found")
      } else {
        log.error(s"No matching domains found for: ${domains.mkString(", ")}")
      }
    } else {
      targetDomains.foreach { case (namespacePath, domain) =>
        val domainMigrationDir = migrationDir / namespacePath / domain

        if (domainMigrationDir.exists()) {
          log.info(s"${namespacePath.replace('/', '.')}.$domain - migrations already initialized")
        } else {
          // Create migration directories for all dialects
          val dialects = List("h2", "postgresql", "mysql", "mariadb", "sqlite")
          dialects.foreach { dialect =>
            val dialectDir = domainMigrationDir / dialect
            IO.createDirectory(dialectDir)

            // Copy current schema as V1__initial_schema.sql
            val schemaFile = schemaDir / namespacePath / domain / s"${domain}_$dialect.sql"
            val migrationFile = dialectDir / "V1__initial_schema.sql"

            if (schemaFile.exists()) {
              IO.copyFile(schemaFile, migrationFile)
            }
          }

          // Save current domain structure as baseline
          findDomainSourceFile(srcDir, namespacePath, domain) match {
            case Some(sourceFile) =>
              GenerateSourceFiles_db.saveBeforeStructure(resourcesDir, namespacePath, domain, sourceFile)
              log.success(s"${namespacePath.replace('/', '.')}.$domain - migrations initialized")
            case None =>
              log.warn(s"${namespacePath.replace('/', '.')}.$domain - could not find source file")
          }
        }
      }
    }
  }

  private def handleDeleteMigrationsImpl(
    migrationDir: File,
    domains: List[String],
    log: ManagedLogger
  ): Unit = {

    if (!migrationDir.exists()) {
      log.info("No migrations to delete")
    } else {
      val activeMigrations = findDomainsWithMigrations(migrationDir)

      val targetDomains = if (domains.isEmpty) {
        activeMigrations
      } else {
        activeMigrations.filter { case (fullPath, _) =>
          domains.exists(d => fullPath.endsWith(d))
        }
      }

      if (targetDomains.isEmpty) {
        if (domains.isEmpty) {
          log.info("No active migrations found")
        } else {
          log.error(s"No active migrations found for: ${domains.mkString(", ")}")
        }
      } else {
        targetDomains.foreach { case (fullPath, _) =>
          val namespacePath = fullPath.split('.').dropRight(1).mkString("/")
          val domain = fullPath.split('.').last
          val domainMigrationDir = migrationDir / namespacePath / domain

          IO.delete(domainMigrationDir)
          log.success(s"$fullPath - migrations deleted")
        }
      }
    }
  }

  private def findDomainSourceFile(dir: File, namespacePath: String, domain: String): Option[File] = {
    val files = IO.listFiles(dir)
    files.find { file =>
      if (file.isFile && file.name.endsWith(".scala")) {
        try {
          val parseAndGen = ParseAndGenerate(file.getPath)
          val pkg = parseAndGen.getPackage
          parseAndGen.extracts.exists {
            case (d, "DomainStructure", _, _) =>
              d == domain && pkg.replace('.', '/') == namespacePath
            case _ => false
          }
        } catch {
          case _: Exception => false
        }
      } else false
    }.orElse {
      files.filter(_.isDirectory).flatMap(subDir => findDomainSourceFile(subDir, namespacePath, domain)).headOption
    }
  }

  private def findDomainsFromSchemas(schemaDir: File): Seq[(String, String)] = {
    def scanDir(dir: File, pathSoFar: String): Seq[(String, String)] = {
      if (!dir.isDirectory) return Nil

      val files = IO.listFiles(dir)

      // Check if this directory contains schema SQL files (indicating it's a domain dir)
      val sqlFiles = files.filter(f => f.isFile && f.name.endsWith(".sql"))

      if (sqlFiles.nonEmpty) {
        // This is a domain directory - extract namespace and domain name
        val domainName = dir.getName
        val parentPath = dir.getParentFile.getPath.substring(schemaDir.getPath.length)
          .stripPrefix("/").stripPrefix("\\")
        Seq((parentPath.replace('\\', '/'), domainName))
      } else {
        // Recurse into subdirectories
        files.filter(_.isDirectory).flatMap(subDir => scanDir(subDir, pathSoFar))
      }
    }

    scanDir(schemaDir, "")
  }

  private def showMigrationStatus(config: Configuration): Def.Initialize[Task[Unit]] = Def.task {
    val log          = streams.value.log
    val resourcesDir = (config / resourceDirectory).value
    val migrationDir = resourcesDir / "db" / "migration"

    if (!migrationDir.exists()) {
      log.info("No active migrations (all domains in development mode)")
    } else {
      val activeMigrations = findDomainsWithMigrations(migrationDir)

      if (activeMigrations.isEmpty) {
        log.info("No active migrations (all domains in development mode)")
      } else {
        log.info("Active migrations:")
        activeMigrations.foreach { case (domainPath, latestMigration) =>
          log.info(s"  $domainPath - latest: $latestMigration")
        }
      }
    }
  }

  // Find domains by scanning the migration directory structure
  // Migration files are in: db/migration/{namespace}/{domain}/{dialect}/V*__*.sql
  private def findDomainsWithMigrations(migrationDir: File): Seq[(String, String)] = {
    def scanForDomains(dir: File, namespacePath: String): Seq[(String, String)] = {
      if (!dir.isDirectory) return Nil

      val subdirs = IO.listFiles(dir).filter(_.isDirectory)

      // Check if subdirectories are dialect directories (contain V*.sql files)
      val dialectDirs = subdirs.filter { subdir =>
        IO.listFiles(subdir).exists(f => f.isFile && f.name.matches("""V\d+__.*\.sql"""))
      }

      if (dialectDirs.nonEmpty) {
        // This is a domain directory - get latest migration from first dialect
        val migrations = IO.listFiles(dialectDirs.head)
          .filter(_.name.matches("""V\d+__.*\.sql"""))
          .sortBy(_.name)

        migrations.lastOption.map { latestFile =>
          val domainName = dir.getName
          val fullPath = if (namespacePath.isEmpty) {
            domainName
          } else {
            s"${namespacePath.replace('/', '.')}.$domainName"
          }
          Seq((fullPath, latestFile.name))
        }.getOrElse(Nil)
      } else {
        // Not a domain directory - recurse into subdirectories
        // Add current directory to namespace path
        subdirs.flatMap { subdir =>
          val newNamespacePath = if (namespacePath.isEmpty) dir.getName else s"$namespacePath/${dir.getName}"
          scanForDomains(subdir, newNamespacePath)
        }
      }
    }

    // Start scanning - the first level subdirs are the start of namespace paths
    val topLevelDirs = IO.listFiles(migrationDir).filter(_.isDirectory)
    topLevelDirs.flatMap(dir => scanForDomains(dir, ""))
  }
}
