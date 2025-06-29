package sbtmolecule.db.dsl

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class DbTable(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
  nsIndex: Int = 0,
  attrIndexPrev: Int = 0
) extends Formatting(metaDomain, metaEntity) {

  private val entityList: Seq[String] = metaDomain.segments.flatMap(_.entities.map(_.entity))
  private val attrList  : Seq[String] = {
    for {
      segment <- metaDomain.segments
      entity <- segment.entities
      attribute <- entity.attributes
    } yield entity.entity + "." + attribute.attribute
  }

  var attrIndex = attrIndexPrev

  private val imports: String = {
    val baseImports = Seq(
      "java.time.*",
      "molecule.base.metaModel.*",
      "molecule.core.dataModel as _dm",
      "molecule.core.dataModel.*",
      "molecule.core.dataModel.Keywords.Kw",
      "molecule.db.core.api.*",
      "molecule.db.core.api.expression.*",
    )
    val typeImports = attributes.collect {
      case MetaAttribute(_, _, "Date", _, _, _, _, _, _, _, _, _) => "java.util.Date"
      case MetaAttribute(_, _, "UUID", _, _, _, _, _, _, _, _, _) => "java.util.UUID"
      case MetaAttribute(_, _, "URI", _, _, _, _, _, _, _, _, _)  => "java.net.URI"
    }.distinct
    (baseImports ++ typeImports).sorted.mkString("import ", "\nimport ", "")
  }

  private val validationExtractor = DbTable_Validations(metaDomain, metaEntity)

  private val baseEntity: String = {
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
      case MetaAttribute(attr, card, tpe, _, refOpt, _, _, _, _, valueAttrs, validations, _) =>
        val valids  = if (validations.nonEmpty) {
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
          vas += validationExtractor.validationMethod(attr, tpe, validations, valueAttrMetas)
          if (valueAttrs.isEmpty) {
            s", validator = Some(validation_$attr)"
          } else {
            val valueAttrsStr = valueAttrs.mkString("\"", "\", \"", "\"")
            s", validator = Some(validation_$attr), valueAttrs = List($valueAttrsStr)"
          }
        } else ""
        val padA    = padAttr(attr)
        val padT0   = padType(tpe)
        val padAI   = padAttrIndex(attrIndex)
        val coord   = refOpt.fold {
          val padRNI = if (maxRefIndex == 0) "" else "  " + " " * maxRefIndex
          s""", coord = List($nsIndex, $attrIndex$padAI$padRNI)"""
        } { ref =>
          val refIndex = entityList.indexOf(ref)
          val padRNI   = padRefIndex(refIndex)
          s""", coord = List($nsIndex, $attrIndex$padAI, $refIndex$padRNI)"""
        }
        val ref1    = refOpt.fold("")(ref => s""", ref = Some("$ref")""")
        val attrMan = "Attr" + card._marker + "Man" + tpe
        val attrOpt = "Attr" + card._marker + "Opt" + tpe
        val attrTac = "Attr" + card._marker + "Tac" + tpe
        attrIndex += 1

        man += s"""protected lazy val ${attr}_man$padA: $attrMan$padT0 = $attrMan$padT0("$entity", "$attr"$padA$coord$ref1$valids)"""
        if (attr != "id") {
          opt += s"""protected lazy val ${attr}_opt$padA: $attrOpt$padT0 = $attrOpt$padT0("$entity", "$attr"$padA$coord$ref1$valids)"""
        }
        tac += s"""protected lazy val ${attr}_tac$padA: $attrTac$padT0 = $attrTac$padT0("$entity", "$attr"$padA$coord$ref1$valids)"""
    }
    val vas1     = vas.result()
    val vas2     = if (vas1.isEmpty) Nil else "" +: vas1
    val attrDefs = (man.result() ++ Seq("") ++ opt.result() ++ Seq("") ++ tac.result() ++ vas2).mkString("\n  ")

    s"""trait ${entity}_base {
       |  $attrDefs
       |}""".stripMargin
  }


  private val entities: String = (0 to metaDomain.maxArity)
    .map(DbTable_Arities(metaDomain, entityList, attrList, metaEntity, _).get).mkString("\n\n")

  private val entityIndex = entityList.indexOf(entity)
  private val idCoord = s"coord = List($entityIndex, ${attrList.indexOf(entity + ".id")})"

  private val (rightRefOp, rightRef) = if (refs.isEmpty) ("", "") else (
    s"with OptEntityOp_0[${entity}_1_refs] with OptEntity_0[${entity}_1_refs] ",
    s"""
       |
       |  override protected def _optEntity[OptEntityTpl](attrs: List[Attr]): ${entity}_1_refs[Option[OptEntityTpl], Any] =
       |    new ${entity}_1_refs[Option[OptEntityTpl], Any](DataModel(List(_dm.OptEntity(attrs))))""".stripMargin
  )

  def get: String = {
    s"""// AUTO-GENERATED Molecule DSL boilerplate code for entity `$entity`
       |package $pkg.$domain
       |
       |$imports
       |
       |
       |$baseEntity
       |
       |object $entity extends $ent_0[Nothing](DataModel(Nil, firstEntityIndex = $entityIndex)) $rightRefOp{
       |  final def apply(id : Long, ids: Long*) = new $ent_0[String](DataModel(List(AttrOneTacID("$entity", "id", Eq, id +: ids, $idCoord)), firstEntityIndex = $entityIndex))
       |  final def apply(ids: Iterable[Long])   = new $ent_0[String](DataModel(List(AttrOneTacID("$entity", "id", Eq, ids.toSeq, $idCoord)), firstEntityIndex = $entityIndex))$rightRef
       |}
       |
       |
       |$entities
       |""".stripMargin
  }
}
