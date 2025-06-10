package sbtmolecule

import java.nio.file.{Files, Paths}
import molecule.base.ast.*
import molecule.base.error.ModelError
import sbtmolecule.db.ParseDomainStructure
import sbtmolecule.graphql.ParseGraphql
import scala.meta.*

case class ParseDefinitionFile(filePath: String) {
  private val bytes       = Files.readAllBytes(Paths.get(filePath))
  private val content     = new String(bytes, "UTF-8")
  private val virtualFile = Input.VirtualFile(filePath, content)
  private val dialect     = dialects.Scala3(virtualFile)
  private val tree        = dialect.parse[Source].get

  private val (pkg, afterPkg) = tree.children.collectFirst {
    case Pkg(pkg, afterPkg) => (pkg.toString, afterPkg)
  }.getOrElse(throw ModelError(s"Missing package definition in file $filePath:\n" + tree))

  def optMetaDomain: Option[MetaDomain] = {
    afterPkg.collectFirst {
      // Look for some of:
      // object MyDefFile extends DomainStructure(6)
      // object MyDefFile extends Graphql(6)
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
        if (maxArity > 0)
          Some(
            defFile match {
              case "DomainStructure" =>

                ParseDomainStructure(filePath, pkg, domain, maxArity, body).getMetaDomain
              case "Graphql"         =>

                ParseGraphql(filePath, pkg, domain, maxArity, urlOrPath, body).getMetaDomain
            }
          )
        else None
    }.flatten
  }
}

