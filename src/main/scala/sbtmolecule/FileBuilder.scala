package sbtmolecule

import java.io.File
import sbt._
import Ast._
import sbtmolecule.generate.{NsArity, NsBase}
import scala.io.Source


object FileBuilder {

  def apply(
    sourceDir: File,
    managedDir: File,
    dataModelDirs: Seq[String],
    allIndexed: Boolean,
    isJvm: Boolean,
    genericPkg: String
  ): Seq[File] = {
    // Loop domain directories
    val files: Seq[File] = dataModelDirs flatMap { dataModelDir =>

      val dataModelDirs: Array[File] = sbt.IO.listFiles(sourceDir / dataModelDir)
      assert(
        dataModelDirs.exists(f => f.isDirectory && f.getName == "dataModel"),
        s"\nMissing `dataModel` package inside supplied moleculeDataModelPath:\n" + sourceDir / dataModelDir
      )

      val dataModelFiles: Array[File] = sbt.IO.listFiles(sourceDir / dataModelDir / "dataModel").filter(f => f.isFile && f.getName.endsWith("DataModel.scala"))
      assert(
        dataModelFiles.nonEmpty,
        "\nFound no valid data model object in " + sourceDir / dataModelDir +
          "\nData model file names should end with `<YourDomain...>DataModel.scala`"
      )

      // Loop data model files in each domain directory
      dataModelFiles flatMap { dataModelFile =>
        val dataModelFileSource = Source.fromFile(dataModelFile)
        val model: Model        = DataModelParser(dataModelFile.getName, dataModelFileSource.getLines().toList, allIndexed, genericPkg).parse
        dataModelFileSource.close()

        val schemaFiles = if (isJvm) {
          // Write schema file
          val schemaFile: File = model.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${model.domain}Schema.scala"
          IO.write(schemaFile, SchemaTransaction(model))

          // Write schema file with lower-cased namespace names when no custom partitions are defined
          // Useful to have lower-case namespace named attributes also for data imports from the Clojure world where namespace names are lower case by convention.
          // In Scala/Molecule code we can still use our uppercase-namespace attribute names.
          val schemaFileModifiers: Seq[File] = if (model.curPart.isEmpty) {
            val schemaFileLowerToUpper: File = model.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${model.domain}SchemaLowerToUpper.scala"
            IO.write(schemaFileLowerToUpper, SchemaTransactionLowerToUpper(model))

            val schemaFileUpperToLower: File = model.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${model.domain}SchemaUpperToLower.scala"
            IO.write(schemaFileUpperToLower, SchemaTransactionUpperToLower(model))

            Seq(schemaFileLowerToUpper, schemaFileUpperToLower)
          } else {
            Nil
          }

          schemaFile +: schemaFileModifiers
        } else {
          Nil
        }

        val nsFiles: Seq[File] = {
          val cleanModel = model.copy(nss = model.nss.filterNot(_.attrs.isEmpty).map(ns => ns.copy(attrs = ns.attrs.filterNot(_.attr.isEmpty))))
          cleanModel.nss.flatMap { ns =>
            val path   = cleanModel.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "dsl" / cleanModel.domain
            val folder = path / ("_" + ns.ns)

            val nsBaseFile: File = path / s"${ns.ns}.scala"
            IO.write(nsBaseFile, NsBase(cleanModel, ns, genericPkg).get)

            val nsArityFiles: Seq[File] = for {
              in <- 0 to cleanModel.maxIn
              out <- 0 to cleanModel.maxOut
            } yield {
              val file: File = folder / s"${ns.ns}_${in}_$out.scala"
              IO.write(file, NsArity(cleanModel, ns, in, out, genericPkg).get)
              file
            }

            nsBaseFile +: nsArityFiles
          }
        }

        schemaFiles ++ nsFiles
      }
    }
    files
  }
}
