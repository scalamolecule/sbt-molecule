package sbtmolecule.render

import molecule.base.ast.*


case class Dsl(
  metaDomain: MetaDomain,
  groupPrefix: String,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0,
  scalaVersion: String = "3"
) extends DslFormatting(metaDomain, metaEntity) {

  private val entityList: Seq[String] = metaDomain.groups.flatMap(_.ents.map(_.ent))
  private val attrList  : Seq[String] = {
    for {
      group <- metaDomain.groups
      entity <- group.ents
      a <- entity.attrs
    } yield entity.ent + "." + a.attr
  }

  var attrIndex = attrIndexPrev

  private val imports: String = {
    val baseImports = Seq(
      "java.time._",
      "molecule.base.ast._",
      "molecule.boilerplate.api.Keywords._",
      "molecule.boilerplate.api._",
      "molecule.boilerplate.api.expression._",
      "molecule.boilerplate.ast.Model",
      "molecule.boilerplate.ast.Model._",
      "scala.reflect.ClassTag"
    )
    val typeImports = attrs.collect {
      case MetaAttribute(_, _, "Date", _, _, _, _, _, _, _) => "java.util.Date"
      case MetaAttribute(_, _, "UUID", _, _, _, _, _, _, _) => "java.util.UUID"
      case MetaAttribute(_, _, "URI", _, _, _, _, _, _, _)  => "java.net.URI"
    }.distinct
    val higherKinds = if (scalaVersion == "212") Seq("scala.language.higherKinds") else Nil
    (baseImports ++ typeImports ++ higherKinds).sorted.mkString("import ", "\nimport ", "")
  }

  private val validationExtractor = Dsl_Validations(metaDomain, metaEntity)

  private val baseEntity: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]
    val tac = List.newBuilder[String]
    val vas = List.newBuilder[String]

    val refIndexes   = attrs.collect { case a if a.ref.isDefined => entityList.indexOf(a.ref.get) }
    val maxRefIndex  = if (refIndexes.isEmpty) 0 else refIndexes.max.toString.length
    val padRefIndex  = (reIndex: Int) => padS(maxRefIndex, reIndex.toString)
    val maxAttrIndex = (attrIndexPrev + attrs.length).toString.length
    val padAttrIndex = (attrIndex: Int) => padS(maxAttrIndex, attrIndex.toString)

    attrs.collect {
      case MetaAttribute(attr, card, tpe, refOpt, _, _, _, _, valueAttrs, validations) =>
        val valids  = if (validations.nonEmpty) {
          val valueAttrMetas = attrs.collect {
            case MetaAttribute(attr1, card1, tpe1, _, _, _, _, _, _, _)
              if valueAttrs.contains(attr1) =>
              val isCardOne = card1.isInstanceOf[CardOne.type]
              val fullTpe   = card1 match {
                case CardOne                   => tpe1
                case CardSet                   => s"Set[$tpe1]"
                case CardSeq if tpe1 == "Byte" => s"Array[Byte]"
                case CardSeq                   => s"Seq[$tpe1]"
                case CardMap                   => s"Map[String, $tpe1]"
              }
              (attr1, isCardOne, fullTpe, s"Attr${card1._marker}Man$tpe1", s"${card1._marker}$tpe1")
          }.sortBy(_._1)
          vas += validationExtractor.validationMethod(attr, tpe, validations, valueAttrMetas)
          if (valueAttrs.isEmpty) {
            s", validator = Some(validation_$attr)"
          } else {
            val valueAttrsStr = valueAttrs.mkString("\"", "\", \"", "\"")
            s", validator = Some(validation_$attr), valueAttrs = Seq($valueAttrsStr)"
          }
        } else ""
        val padA    = padAttr(attr)
        val padT0   = padType(tpe)
        val padAI   = padAttrIndex(attrIndex)
        val coord   = refOpt.fold {
          val padRNI = if (maxRefIndex == 0) "" else "  " + " " * maxRefIndex
          s""", coord = Seq($nsIndex, $attrIndex$padAI$padRNI)"""
        } { ref =>
          val refIndex = entityList.indexOf(ref)
          val padRNI   = padRefIndex(refIndex)
          s""", coord = Seq($nsIndex, $attrIndex$padAI, $refIndex$padRNI)"""
        }
        val ref1    = refOpt.fold("")(ref => s""", ref = Some("$ref")""")
        val attrMan = "Attr" + card._marker + "Man" + tpe
        val attrOpt = "Attr" + card._marker + "Opt" + tpe
        val attrTac = "Attr" + card._marker + "Tac" + tpe
        attrIndex += 1

        man += s"""protected lazy val ${attr}_man$padA: $attrMan$padT0 = $attrMan$padT0("$ent", "$attr"$padA$coord$ref1$valids)"""
        if (attr != "id") {
          opt += s"""protected lazy val ${attr}_opt$padA: $attrOpt$padT0 = $attrOpt$padT0("$ent", "$attr"$padA$coord$ref1$valids)"""
        }
        tac += s"""protected lazy val ${attr}_tac$padA: $attrTac$padT0 = $attrTac$padT0("$ent", "$attr"$padA$coord$ref1$valids)"""
    }
    val vas1     = vas.result()
    val vas2     = if (vas1.isEmpty) Nil else "" +: vas1
    val attrDefs = (man.result() ++ Seq("") ++ opt.result() ++ Seq("") ++ tac.result() ++ vas2).mkString("\n  ")

    s"""trait ${ent}_base {
       |  $attrDefs
       |}""".stripMargin
  }


  private val entities: String = (0 to metaDomain.maxArity)
    .map(Dsl_Arities(scalaVersion, metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")

  val idCoord = s"coord = Seq(${entityList.indexOf(ent)}, ${attrList.indexOf(ent + ".id")})"

  def get: String = {
    s"""/*
       |* AUTO-GENERATED Molecule DSL boilerplate code for entity `$ent`
       |*
       |* To change:
       |* 1. Edit domain structure in $pkg.$domain
       |* 2. `sbt compile -Dmolecule=true`
       |*/
       |package $pkg.$domain
       |
       |$imports
       |
       |
       |$baseEntity
       |
       |object $ent extends $ent_0[Nothing](Nil) {
       |  final def apply(id : Long, ids: Long*) = new $ent_0[String](List(AttrOneTacID("$ent", "id", Eq, id +: ids, $idCoord)))
       |  final def apply(ids: Iterable[Long])   = new $ent_0[String](List(AttrOneTacID("$ent", "id", Eq, ids.toSeq, $idCoord)))
       |}
       |
       |
       |$entities
       |""".stripMargin
  }
}
