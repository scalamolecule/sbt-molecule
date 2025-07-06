package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting

case class Entity_Attrs(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0
) extends Formatting(metaDomain, metaEntity) {

  private val entityList          = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private var attrIndex           = attrIndexPrev
  private val validationExtractor = Entity_Validations(metaDomain, metaEntity)

  def get: String = {
    val man = List.newBuilder[String]
    val opt = List.newBuilder[String]
    val tac = List.newBuilder[String]
    val vas = List.newBuilder[String]

    val refIndexes   = attributes.collect { case a if a.ref.isDefined => entityList.indexOf(a.ref.get) }
    val maxRefIndex  = if (refIndexes.isEmpty) 0 else refIndexes.max.toString.length
    val padRefIndex  = (reIndex: Int) => padS(maxRefIndex, reIndex.toString)
    val maxAttrIndex = (attrIndexPrev + attributes.length).toString.length
    val padAttrIndex = (attrIndex: Int) => padS(maxAttrIndex, attrIndex.toString)

    attributes.collect {
      case MetaAttribute(attr, card, tpe, _, optRef, _, _, optAlias, _, valueAttrs, validations, _) =>
        val cleanAttr = optAlias.getOrElse(attr)
        val valids    = if (validations.nonEmpty) {
          val valueAttrMetas = attributes.collect {
            case MetaAttribute(attr1, card1, tpe1, _, _, _, _, _, _, _, _, _)
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
          vas += validationExtractor.validationMethod(attr, cleanAttr, tpe, validations, valueAttrMetas)
          if (valueAttrs.isEmpty) {
            s", validator = Some(validation_$cleanAttr)"
          } else {
            val valueAttrsStr = valueAttrs.mkString("\"", "\", \"", "\"")
            s", validator = Some(validation_$cleanAttr), valueAttrs = List($valueAttrsStr)"
          }
        } else ""
        val padA      = padAttrClean(cleanAttr)
        val padB      = padAttr(attr)
        val padT0     = padType(tpe)
        val padAI     = padAttrIndex(attrIndex)
        val coord     = optRef.fold {
          val padRNI = if (maxRefIndex == 0) "" else "  " + " " * maxRefIndex
          s""", coord = List($nsIndex, $attrIndex$padAI$padRNI)"""
        } { ref =>
          val refIndex = entityList.indexOf(ref)
          val padRNI   = padRefIndex(refIndex)
          s""", coord = List($nsIndex, $attrIndex$padAI, $refIndex$padRNI)"""
        }
        val ref1      = optRef.fold("")(ref => s""", ref = Some("$ref")""")
        val attrMan   = "Attr" + card._marker + "Man" + tpe
        val attrOpt   = "Attr" + card._marker + "Opt" + tpe
        val attrTac   = "Attr" + card._marker + "Tac" + tpe
        attrIndex += 1

        man += s"""protected def ${cleanAttr}_man$padA = $attrMan$padT0("$entity", "$attr"$padB$coord$ref1$valids)"""
        if (attr != "id") {
          opt += s"""protected def ${cleanAttr}_opt$padA = $attrOpt$padT0("$entity", "$attr"$padB$coord$ref1$valids)"""
        }
        tac += s"""protected def ${cleanAttr}_tac$padA = $attrTac$padT0("$entity", "$attr"$padB$coord$ref1$valids)"""
    }
    val vas1     = vas.result()
    val vas2     = if (vas1.isEmpty) Nil else "" +: vas1
    val attrDefs = (man.result() ++ Seq("") ++ opt.result() ++ Seq("") ++ tac.result() ++ vas2).mkString("\n  ")

    s"""trait ${entity}_attrs {
       |  $attrDefs
       |}""".stripMargin
  }
}
