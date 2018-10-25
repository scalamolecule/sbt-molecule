package sbtmolecule

import java.io.File
import sbt._
import Ast._


object FileBuilder {

  def apply(codeDir: File, managedDir: File, defDirs: Seq[String], docs: Boolean = true, allIndexed: Boolean = true): Seq[File] = {
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
        val d: Definition = DefinitionParser(definitionFile, docs, allIndexed).parse

        // Write schema file
        val schemaFile: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "schema" / s"${d.domain}Schema.scala"
        IO.write(schemaFile, SchemaTransaction(d))

        val namespaceFiles: Seq[File] = {
          val nsBuilder = NamespaceBuilder(d, docs)
          d.nss.flatMap { ns =>
            val (outBody, inBodies) = nsBuilder.nsBodies(ns)

            // Output namespace files
            val nsOutFile: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "dsl" / firstLow(d.domain) / s"${ns.ns}.scala"
            IO.write(nsOutFile, outBody)

            // Input namespace files
            val nsInFiles = inBodies.map { case (i, inBody) =>
              val inFile: File = d.pkg.split('.').toList.foldLeft(managedDir)((dir, pkg) => dir / pkg) / "dsl" / firstLow(d.domain) / s"${ns.ns}_in$i.scala"
              IO.write(inFile, inBody)
              inFile
            }
            nsOutFile +: nsInFiles
          }
        }

        schemaFile +: namespaceFiles
      }
    }
    files
  }
}
