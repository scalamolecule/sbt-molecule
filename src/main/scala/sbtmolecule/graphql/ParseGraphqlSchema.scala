package sbtmolecule.graphql

import java.io.File
import caliban.parsing.adt.Document
import caliban.tools.SchemaLoader
import molecule.base.util.BaseHelpers
import zio.*
import scala.util.Try

case class ParseGraphqlSchema(
  filePath: String,
  domain: String,
  urlOrPath: String
) extends BaseHelpers {

  // Zio runtime
  private val runtime = Runtime.default

  // Use Caliban to parse graphql schema
  def getDoc: Document = urlOrPath match {
    case ""                            => docFromFile(filePath.dropRight(6) + ".graphql")
    case url if url.startsWith("http") => docFromUrl
    case _                             => docFromFile(urlOrPath)
  }

  private def docFromFile(path: String): Document = {
    if (new File(path).exists()) {
      Try {
        Unsafe.unsafe { implicit u =>
          runtime.unsafe.run(
            SchemaLoader.fromFile(path).load // Caliban
          ).getOrThrow()
        }
      }.getOrElse(throw new Exception(s"Couldn't find graphql definition file at path: $path"))
    } else {
      val msg = if (urlOrPath.isEmpty)
        s"When no url or path is supplied, please add a graphql schema file named $domain.graphql " +
          s"beside your $domain.scala definition file."
      else
        s"Couldn't find definition file at path: $path."
      throw new Exception(msg)
    }
  }

  private def docFromUrl: Document = {
    try {
      Unsafe.unsafe { implicit u =>
        runtime.unsafe.run(
          SchemaLoader.fromIntrospection(urlOrPath, None).load
        ).getOrThrow()
      }
    } catch {
      case e: Throwable =>
        // Let it blow and show the problem
        e.printStackTrace()
        throw e
    }
  }
}

