package sbtmolecule

import java.io.File
import sbt._
import Ast._
import scala.io.Source


object FileBuilder {

  def apply(codeDir: File, managedDir: File, defDirs: Seq[String], allIndexed: Boolean = true): Seq[File] = {
    // Loop domain directories
    val files: Seq[File] = defDirs flatMap { defDir =>

      val schemaDirs: Array[File] = sbt.IO.listFiles(codeDir / defDir)
      assert(schemaDirs.exists(f => f.isDirectory && f.getName == "schema"),
        s"\nMissing `schema` package inside supplied moleculeSchema directory `$defDir`.")

      val definitionFiles: Array[File] = sbt.IO.listFiles(codeDir / defDir / "schema").filter(f => f.isFile && f.getName.endsWith("Definition.scala"))
      assert(definitionFiles.nonEmpty, "\nFound no definition files in path: " + codeDir / defDir +
        "\nSchema definition file names should end with `<YourDomain...>Definition.scala`")

      // Loop definition files in each domain directory
      definitionFiles flatMap { defFile =>
        val defFileSource = Source.fromFile(defFile)
        val d: Definition = DefinitionParser(defFile.getName, defFileSource.getLines().toList, allIndexed).parse
        defFileSource.close()

        // Write schema file
        val schemaFile: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${d.domain}Schema.scala"
        IO.write(schemaFile, SchemaTransaction(d))

        // Write schema file with lower-cased namespace names when no custom partitions are defined
        // Useful to have lower-case namespace named attributes also for data imports from the Clojure world where namespace names are lower case by convention.
        // In Scala/Molecule code we can still use our uppercase-namespace attribute names.
        val schemaFileModifiers: Seq[File] = if (d.curPart.isEmpty) {
          val schemaFileLowerToUpper: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${d.domain}SchemaLowerToUpper.scala"
          IO.write(schemaFileLowerToUpper, SchemaTransactionLowerToUpper(d))

          val schemaFileUpperToLower: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${d.domain}SchemaUpperToLower.scala"
          IO.write(schemaFileUpperToLower, SchemaTransactionUpperToLower(d))

          Seq(schemaFileLowerToUpper, schemaFileUpperToLower)
        } else Nil

        val namespaceFiles: Seq[File] = {
          val d2        = d.copy(nss = d.nss.filterNot(_.attrs.isEmpty).map(ns => ns.copy(attrs = ns.attrs.filterNot(_.attr.isEmpty))))
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

        (schemaFile +: schemaFileModifiers) ++ namespaceFiles
      }
    }
    files
  }
}
