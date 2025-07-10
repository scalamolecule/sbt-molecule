package sbtmolecule

import java.io.File
import java.nio.file.{Files, Paths}
import sbtmolecule.db.ParseDomainStructure
import sbtmolecule.graphql.ParseGraphqlSchema
import scala.meta.*

case class ParseAndGenerate(filePath: String) {

  val actual = classOf[scala.meta.Tree].getPackage.getImplementationVersion
  require(actual == "4.9.0", s"Incompatible scalameta version: $actual (expected 4.9.0)")


  private val bytes       = Files.readAllBytes(Paths.get(filePath))
  private val content     = new String(bytes, "UTF-8")
  private val virtualFile = Input.VirtualFile(filePath, content)
  private val dialect     = dialects.Scala3(virtualFile)
  private val tree        = dialect.parse[Source].get

  private val (pkg, afterPkg) = tree.children.collectFirst {
    case Pkg(pkg, afterPkg) => (pkg.toString, afterPkg)
  }.getOrElse(throw new Exception(s"Missing package definition in file $filePath:\n" + tree))


  // Use Scala Meta to find definition files:
  def generate(srcManaged: File, resourcesDir: File): Option[(String, String)] = afterPkg.collectFirst {
    // object MyDefFile extends DomainStructure
    case Defn.Object(Nil, Term.Name(domain), Template.internal.Latest(_, List(Init.internal.Latest(
    Type.Name("DomainStructure"), _, Nil)), _, body, _)) =>
      val metaDomain = ParseDomainStructure(filePath, pkg, domain, body).getMetaDomain
      GenerateSourceFiles_db(metaDomain).generate(srcManaged, resourcesDir)
      (pkg, domain)

    // object MyDefFile extends Graphql("url or filePath")
    case Defn.Object(Nil, Term.Name(domain), Template.internal.Latest(_, List(Init.internal.Latest(
    Type.Name("Graphql"), _, List(Term.ArgClause(List(Lit.String(urlOrPath)), _)))), _, _, _)) =>
      val doc = ParseGraphqlSchema(filePath, domain, urlOrPath).getDoc
      GenerateSourceFiles_graphql(doc, pkg, domain).generate(srcManaged)
      (pkg, domain)
  }


  def extract: (String, String, List[Stat], String) = afterPkg.collectFirst {
    case Defn.Object(Nil, Term.Name(domain), Template.internal.Latest(_, List(Init.internal.Latest(
    Type.Name(defFile@("DomainStructure" | "Graphql")), _, args)), _, body, _)) =>
      val urlOrPath = args match {
        case List(Term.ArgClause(List(Lit.String(urlOrPath)), _)) => urlOrPath
        case _                                                    => ""
      }
      (domain, defFile, body, urlOrPath)
  }.getOrElse(
    throw new Exception(
      s"Couldn't find matching source tree for $filePath:" +
        afterPkg.take(5).map(_.structure.take(300) + " ...")
          .mkString("\n\n", "\n-----------------\n", "\n-----------------\n")
    )
  )


  // For testing:

  def metaDomain: GenerateSourceFiles_db = {
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

