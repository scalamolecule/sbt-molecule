package sbtmolecule

import java.io.File
import sbt._
import Ast._
import scala.io.Source


object FileBuilder {

  def apply(
    sourceDir: File,
    managedDir: File,
    dataModelDirs: Seq[String],
    allIndexed: Boolean = true,
    isJvm: Boolean = true
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
        val model: Model        = DataModelParser(dataModelFile.getName, dataModelFileSource.getLines().toList, allIndexed).parse
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

        val namespaceFiles: Seq[File] = {
          val d2        = model.copy(nss = model.nss.filterNot(_.attrs.isEmpty).map(ns => ns.copy(attrs = ns.attrs.filterNot(_.attr.isEmpty))))
          val nsBuilder = NamespaceBuilder(d2)
          d2.nss.flatMap { ns =>
            val (outBody, outBodies, inBodies) = nsBuilder.nsBodies(ns)

            val path   = d2.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "dsl" / firstLow(d2.domain)
            val folder = path / (firstLow(ns.ns) + "_")

            // Output namespace files
            val outFileBase: File = path / s"${ns.ns}.scala"
            IO.write(outFileBase, outBody)

            // Create sub folder with individual arity out files
            val outFiles: Seq[File] = outBodies.zipWithIndex.map { case (body, outArity) =>
              val outFile = folder / s"${ns.ns}_$outArity.scala"
              IO.write(outFile, body)
              outFile
            }

            // Input namespace files
            val inFiles: Seq[File] = inBodies.zipWithIndex.map { case ((inArity, inBody), i) =>
              val outArity     = i - (inArity - 1) * d2.out - (inArity - 1)
              val inFile: File = folder / s"${ns.ns}_In_${inArity}_$outArity.scala"
              IO.write(inFile, inBody)
              inFile
            }

            outFileBase +: (outFiles ++ inFiles)
          }
        }

        schemaFiles ++ namespaceFiles
      }
    }
    files
  }
}
