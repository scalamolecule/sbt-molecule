package sbtmolecule

import java.io.File
import java.nio.file.{Files, Paths}
import sbtmolecule.db.ParseDomainStructure
import sbtmolecule.graphql.ParseGraphqlSchema
import scala.meta.*

case class ParseAndGenerate(filePath: String) {
  private val bytes       = Files.readAllBytes(Paths.get(filePath))
  private val content     = new String(bytes, "UTF-8")
  private val virtualFile = Input.VirtualFile(filePath, content)
  private val dialect     = dialects.Scala3(virtualFile)
  private val tree        = dialect.parse[Source].get

  private val (pkg, afterPkg) = if (tree.children.isEmpty) ("", Nil) else tree.children.collectFirst {
    case Pkg(pkg, afterPkg) => (pkg.toString, afterPkg)
  }.getOrElse(throw new Exception(s"Missing package definition in file $filePath:\n" + tree))

  def generate(srcManaged: File, resourcesDir: File): Option[(String, String)] = afterPkg.collectFirst {
    case q"object $domain extends DomainStructure { ..$body }" =>
      val metaDomain = ParseDomainStructure(filePath, pkg, domain.value, body).getMetaDomain
      GenerateSourceFiles_db(metaDomain).generate(srcManaged, resourcesDir)
      (pkg, domain.value)

    case q"object $domain extends Graphql($urlOrPath)" =>
      val doc = ParseGraphqlSchema(filePath, domain.value, urlOrPath.children.head.text).getDoc
      GenerateSourceFiles_graphql(doc, pkg, domain.value).generate(srcManaged)
      (pkg, domain.value)
  }


  def extract: (String, String, Seq[Stat], String) = afterPkg.collectFirst {
    case q"object $domain extends DomainStructure { ..$body }" =>
      (domain.value, "DomainStructure", body, "")

    case q"object $domain extends Graphql($urlOrPath)" =>
      (domain.value, "Graphql", Nil, urlOrPath.children.head.text)
  }.getOrElse(
    throw new Exception(
      s"Couldn't find matching source tree for $filePath:" +
        afterPkg.take(5).map(_.structure.take(300) + " ...")
          .mkString("\n\n", "\n-----------------\n", "\n-----------------\n")
    )
  )


  // For testing:

  def generator: GenerateSourceFiles_db = {
    val (domain, _, body, _) = extract

    val metaDomain = ParseDomainStructure(filePath, pkg, domain, body).getMetaDomain
    GenerateSourceFiles_db(metaDomain)
  }

  def graphql: GenerateSourceFiles_graphql = {
    val (domain, _, _, urlOrPath) = extract

    val doc = ParseGraphqlSchema(filePath, domain, urlOrPath).getDoc
    GenerateSourceFiles_graphql(doc, pkg, domain)
  }
}

