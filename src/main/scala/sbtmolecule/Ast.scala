package sbtmolecule

import scala.util.matching

object Ast {

  class SchemaDefinitionException(message: String) extends RuntimeException(message)

  case class Definition(pkg: String, in: Int, out: Int, domain: String, curPart: String, curPartDescr: String, nss: Seq[Namespace]) {
    def addAttr(attrs: Seq[DefAttr]): Definition = {
      val previousNs = nss.init
      val lastNs = nss.last
      copy(nss = previousNs :+ lastNs.copy(attrs = lastNs.attrs ++ attrs))
    }
  }

  case class Namespace(part: String, partDescr: Option[String], ns: String, nsDescr: Option[String], opt: Option[Extension] = None, attrs: Seq[DefAttr] = Seq()) {
    override def toString: String =
      s"""Namespace(
         |   part     : $part
         |   partDescr: $partDescr
         |   ns       : $ns
         |   nsDescr  : $nsDescr
         |   opt      : $opt
         |   attrs    :
         |     ${attrs.mkString("\n     ")}
         |)""".stripMargin
  }

  sealed trait Extension
  case object Edge extends Extension

  sealed trait DefAttr {
    val attr     : String
    val attrClean: String
    val clazz    : String
    val tpe      : String
    val baseTpe  : String
    val options  : Seq[Optional]
    val attrGroup: Option[String]
  }


  case class Val(attr: String, attrClean: String, clazz: String, tpe: String, baseTpe: String, datomicTpe: String,
                 options: Seq[Optional] = Seq(), bi: Option[String] = None, revRef: String = "", attrGroup: Option[String] = None) extends DefAttr

  case class Enum(attr: String, attrClean: String, clazz: String, tpe: String, baseTpe: String, enums: Seq[String],
                  options: Seq[Optional] = Seq(), bi: Option[String] = None, revRef: String = "", attrGroup: Option[String] = None) extends DefAttr

  case class Ref(attr: String, attrClean: String, clazz: String, clazz2: String, tpe: String, baseTpe: String, refNs: String,
                 options: Seq[Optional] = Seq(), bi: Option[String] = None, revRef: String = "", attrGroup: Option[String] = None) extends DefAttr

  case class BackRef(attr: String, attrClean: String, clazz: String, clazz2: String, tpe: String, baseTpe: String, backRefNs: String,
                     options: Seq[Optional] = Seq(), attrGroup: Option[String] = None) extends DefAttr

  case class Optional(datomicKeyValue: String, clazz: String)


  // Helpers ..........................................

  def padS(longest: Int, str: String): String = pad(longest, str.length)
  def pad(longest: Int, shorter: Int): String = if (longest > shorter) " " * (longest - shorter) else ""
  def padI(n: Int): String = if (n < 10) s"0$n" else s"$n"
  def firstLow(str: Any): String = str.toString.head.toLower + str.toString.tail
  implicit class Regex(sc: StringContext) {
    def r: matching.Regex = new scala.util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
}
