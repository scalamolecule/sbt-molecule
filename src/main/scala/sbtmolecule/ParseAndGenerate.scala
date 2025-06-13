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

  private val (pkg, afterPkg) = tree.children.collectFirst {
    case Pkg(pkg, afterPkg) => (pkg.toString, afterPkg)
  }.getOrElse(throw new Exception(s"Missing package definition in file $filePath:\n" + tree))


  def generate(srcManaged: File): Option[(String, String)] = {
    val (domain, defFile, body, maxArity, urlOrPath) = extract
    if (maxArity > 0) {
      defFile match {
        case "DomainStructure" =>
          val metaDomain = ParseDomainStructure(filePath, pkg, domain, maxArity, body).getMetaDomain
          GenerateSourceFiles_db(metaDomain).generate(srcManaged)

        case "Graphql" =>
          val doc = ParseGraphqlSchema(filePath, domain, urlOrPath).getDoc
          GenerateSourceFiles_graphql(doc, pkg, domain, maxArity).generate(srcManaged)
      }
      Some((pkg, domain))
    } else None
  }


  def extract: (String, String, List[Stat], Int, String) = {
    afterPkg.collectFirst {
      // Use Scala Meta to find definition files:
      // object MyDefFile extends DomainStructure(6)
      // object MyDefFile extends Graphql(6) // adjacent graphql schema
      // object MyDefFile extends Graphql(6, "url")
      // object MyDefFile extends Graphql(6, "filePath")
      case Defn.Object(_, Term.Name(domain),
      Template.internal.Latest(_,
      List(Init.internal.Latest(Type.Name(defFile), _,
      List(Term.ArgClause(args, _)))), _, body, _)) =>
        val (maxArity, urlOrPath) = args match {
          case List(Lit.Int(maxArity))                        => (maxArity, "")
          case List(Lit.Int(maxArity), Lit.String(urlOrPath)) => (maxArity, urlOrPath)
        }
        (domain, defFile, body, maxArity, urlOrPath)
    }.get
  }


  // For testing:

  def metaDomain: GenerateSourceFiles_db = {
    val (domain, _, body, maxArity, _) = extract

    val metaDomain = ParseDomainStructure(filePath, pkg, domain, maxArity, body).getMetaDomain
    GenerateSourceFiles_db(metaDomain)
  }

  def graphql: GenerateSourceFiles_graphql = {
    val (domain, _, _, maxArity, urlOrPath) = extract

    val doc = ParseGraphqlSchema(filePath, domain, urlOrPath).getDoc
    GenerateSourceFiles_graphql(doc, pkg, domain, maxArity)
  }
}

