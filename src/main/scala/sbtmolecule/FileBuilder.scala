package sbtmolecule

import java.io.File
import sbt._
import Ast._


object FileBuilder {

  def apply(codeDir: File, managedDir: File, defDirs: Seq[String], allIndexed: Boolean = true): Seq[File] = {
    // Loop domain directories
    val files: Seq[File] = defDirs flatMap { defDir =>

      val schemaDirs: Array[File] = sbt.IO.listFiles(codeDir / defDir)
      assert(schemaDirs.exists(f => f.isDirectory && f.getName == "schema"), "\nMissing `schema` package inside any defined moleculeSchemas. Found:\n"
        + schemaDirs.mkString("\n"))

      val definitionFiles: Array[File] = sbt.IO.listFiles(codeDir / defDir / "schema").filter(f => f.isFile && f.getName.endsWith("Definition.scala"))
      assert(definitionFiles.nonEmpty, "\nFound no definition files in path: " + codeDir / defDir +
        "\nSchema definition file names should end with `<YourDomain...>Definition.scala`")

      // Loop definition files in each domain directory
      definitionFiles flatMap { definitionFile =>
        val d: Definition = DefinitionParser(definitionFile, allIndexed).parse

        // Write schema file
        val schemaFile: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${d.domain}Schema.scala"
        IO.write(schemaFile, SchemaTransaction(d))

        val namespaceFiles: Seq[File] = {
          val nsBuilder = NamespaceBuilder(d)
          d.nss.flatMap { ns =>
            val (outBody, outBodies, inBodies) = nsBuilder.nsBodies(ns)

            val path = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "dsl" / firstLow(d.domain)
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
              val outArity = i - (inArity - 1) * d.out - (inArity - 1)
              val inFile: File = folder / s"${ns.ns}_In_${inArity}_$outArity.scala"
              IO.write(inFile, inBody)
              inFile
            }

            outFileBase +: (outFiles ++ inFiles)
          }
        }

        schemaFile +: namespaceFiles
      }
    }
    files
  }
}
