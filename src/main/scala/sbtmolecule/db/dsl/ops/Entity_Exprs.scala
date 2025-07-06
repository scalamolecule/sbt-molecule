package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import sbtmolecule.Formatting


case class Entity_Exprs(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
) extends Formatting(metaDomain, metaEntity) {
  val ent_0 = entity + "_0"
  val ent_1 = entity + "_1"
  val ent_n = entity + "_n"

  val exprOne = if (attributes.exists(_.cardinality == CardOne)) {
    val cardOneAttrs = attributes.filter(_.cardinality == CardOne)

    val base = List(
      s"""// One =======================================================================================================
         |
         |class ${ent_1}_ExprOneMan[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1[T, [t] =>> $ent_1[t] & ${entity}_Sort_1[t] & CardOne]
         |    with ${entity}_Sort_1[T] {
         |  override val entity = [t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne
         |}
         |class ${ent_n}_ExprOneMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl] & ${entity}_Sort_n[tpl] & CardOne]
         |    with ${entity}_Sort_n[Tpl] {
         |  override val entity = [tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne
         |}
         |
         |class ${ent_1}_ExprOneOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[T]](dataModel)
         |    with ExprOneOpt[T, $ent_1[Option[T]]]((dm: DataModel) => new $ent_1[Option[T]](dm) with ${entity}_Sort_1[Option[T]] with CardOne)
         |    with ${entity}_Sort_1[Option[T]]
         |
         |class ${ent_n}_ExprOneOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneOpt[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with ${entity}_Sort_n[Tpl] with CardOne)
         |    with ${entity}_Sort_n[Tpl]
         |
         |
         |class ${ent_0}_ExprOneTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac[T, $ent_0] {
         |  override val entity = (dm: DataModel) => new $ent_0(dm) with CardOne
         |}
         |class ${ent_1}_ExprOneTac[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneTac[T, $ent_1[?]] {
         |  override val entity = (dm: DataModel) => new $ent_1[T](dm) with CardOne
         |}
         |class ${ent_n}_ExprOneTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac[T, $ent_n[?]] {
         |  override val entity = (dm: DataModel) => new $ent_n[Tpl](dm) with CardOne
         |}
         |""".stripMargin
    )

    val string = if (cardOneAttrs.exists(_.baseTpe == "String")) List(
      s"""class ${ent_1}_ExprOneMan_String[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1_String[T, [t] =>> $ent_1[t] & ${entity}_Sort_1[t] & CardOne]
         |    with ${entity}_Sort_1[T] {
         |  override val entity = [t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne
         |}
         |class ${ent_n}_ExprOneMan_String[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl] & ${entity}_Sort_n[tpl] & CardOne]
         |    with ${entity}_Sort_n[Tpl] {
         |  override val entity = [tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne
         |}
         |
         |class ${ent_0}_ExprOneTac_String[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_String[T, $ent_0] {
         |  override val entity = (dm: DataModel) => new $ent_0(dm) with CardOne
         |}
         |class ${ent_1}_ExprOneTac_String[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneTac_String[T, $ent_1[?]] {
         |  override val entity = (dm: DataModel) => new $ent_1[T](dm) with CardOne
         |}
         |class ${ent_n}_ExprOneTac_String[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_String[T, $ent_n[?]] {
         |  override val entity = (dm: DataModel) => new $ent_n[Tpl](dm) with CardOne
         |}
         |""".stripMargin
    ) else Nil

    val enum = if (cardOneAttrs.exists(_.enumTpe.isDefined)) List(
      s"""class ${ent_1}_ExprOneMan_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1_Enum[T, [t] =>> $ent_1[t]]([t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne)
         |    with ${entity}_Sort_1[T]
         |
         |class ${ent_n}_ExprOneMan_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n_Enum[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl]]([tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne)
         |    with ${entity}_Sort_n[Tpl]
         |
         |class ${ent_1}_ExprOneOpt_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[T]](dataModel)
         |    with ExprOneOpt_Enum[T, $ent_1[Option[T]]]((dm: DataModel) => new $ent_1[Option[T]](dm) with ${entity}_Sort_1[Option[T]] with CardOne)
         |    with ${entity}_Sort_1[Option[T]]
         |
         |class ${ent_n}_ExprOneOpt_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneOpt_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with ${entity}_Sort_n[Tpl] with CardOne)
         |    with ${entity}_Sort_n[Tpl]
         |
         |class ${ent_0}_ExprOneTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Enum[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with ${entity}_Sort_1[T] with CardOne)
         |
         |class ${ent_1}_ExprOneTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneTac_Enum[T, $ent_1[?]]((dm: DataModel) => new $ent_1[T](dm) with  CardOne)
         |
         |class ${ent_n}_ExprOneTac_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Enum[T, $ent_n[?]]((dm: DataModel) => new $ent_n[Tpl](dm) with  CardOne)
         |""".stripMargin
    ) else Nil

    val integerTypes = List("Int", "Long", "BigInt", "Short", "Byte")
    val integer      = if (cardOneAttrs.exists(a => integerTypes.contains(a.baseTpe))) List(
      s"""class ${ent_1}_ExprOneMan_Integer[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1_Integer[T, [t] =>> $ent_1[t] & ${entity}_Sort_1[t] & CardOne]
         |    with ${entity}_Sort_1[T] {
         |  override val entity = [t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne
         |}
         |class ${ent_n}_ExprOneMan_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n_Integer[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl] & ${entity}_Sort_n[tpl] & CardOne]
         |    with ${entity}_Sort_n[Tpl] {
         |  override val entity = [tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne
         |}
         |
         |class ${ent_0}_ExprOneTac_Integer[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Integer[T, $ent_0] {
         |  override val entity = (dm: DataModel) => new $ent_0(dm) with CardOne
         |}
         |class ${ent_1}_ExprOneTac_Integer[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneTac_Integer[T, $ent_1[?]] {
         |  override val entity = (dm: DataModel) => new $ent_1[T](dm) with CardOne
         |}
         |class ${ent_n}_ExprOneTac_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Integer[T, $ent_n[?]] {
         |  override val entity = (dm: DataModel) => new $ent_n[Tpl](dm) with CardOne
         |}
         |""".stripMargin
    ) else Nil

    val decimalTypes = List("Double", "Float", "BigDecimal")
    val decimal      = if (cardOneAttrs.exists(a => decimalTypes.contains(a.baseTpe))) List(
      s"""class ${ent_1}_ExprOneMan_Decimal[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1_Decimal[T, [t] =>> $ent_1[t] & ${entity}_Sort_1[t] & CardOne]
         |    with ${entity}_Sort_1[T] {
         |  override val entity = [t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne
         |}
         |class ${ent_n}_ExprOneMan_Decimal[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n_Decimal[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl] & ${entity}_Sort_n[tpl] & CardOne]
         |    with ${entity}_Sort_n[Tpl] {
         |  override val entity = [tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne
         |}
         |""".stripMargin
    ) else Nil

    val boolean = if (cardOneAttrs.exists(_.baseTpe == "Boolean")) List(
      s"""class ${ent_1}_ExprOneMan_Boolean[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ExprOneMan_1_Boolean[T, [t] =>> $ent_1[t] & ${entity}_Sort_1[t] & CardOne]
         |    with ${entity}_Sort_1[T] {
         |  override val entity = [t] => (dm: DataModel) => new $ent_1[t](dm) with ${entity}_Sort_1[t] with CardOne
         |}
         |class ${ent_n}_ExprOneMan_Boolean[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n_Boolean[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl] & ${entity}_Sort_n[tpl] & CardOne]
         |    with ${entity}_Sort_n[Tpl] {
         |  override val entity = [tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm) with ${entity}_Sort_n[tpl] with CardOne
         |}
         |""".stripMargin
    ) else Nil

    List(List(base, string, enum, integer, decimal, boolean).flatten.mkString("\n\n"))
  } else Nil


  val exprSet = if (attributes.exists(_.cardinality == CardSet)) {
    val base = List(
      s"""// Set =======================================================================================================
         |
         |class ${ent_1}_ExprSetMan[T](override val dataModel: DataModel)
         |  extends $ent_1[Set[T]](dataModel)
         |    with ExprSetMan[T, $ent_1[Set[T]]]((dm: DataModel) => new $ent_1[Set[T]](dm))
         |
         |class ${ent_n}_ExprSetMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetMan[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_1}_ExprSetOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Set[T]]](dataModel)
         |    with ExprSetOpt[T, $ent_1[Option[Set[T]]]]((dm: DataModel) => new $ent_1[Option[Set[T]]](dm))
         |
         |class ${ent_n}_ExprSetOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetOpt[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_0}_ExprSetTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprSetTac[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprSetTac[T](override val dataModel: DataModel)
         |  extends $ent_1[Set[T]](dataModel)
         |    with ExprSetTac[T, $ent_1[Set[T]]]((dm: DataModel) => new $ent_1[Set[T]](dm))
         |
         |class ${ent_n}_ExprSetTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )

    val enum = if (attributes.filter(_.cardinality == CardSet).exists(_.enumTpe.isDefined)) List(
      s"""class ${ent_1}_ExprSetMan_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Set[T]](dataModel)
         |    with ExprSetMan_Enum[T, $ent_1[Set[T]]]((dm: DataModel) => new $ent_1[Set[T]](dm))
         |
         |class ${ent_n}_ExprSetMan_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetMan_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_1}_ExprSetOpt_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Set[T]]](dataModel)
         |    with ExprSetOpt_Enum[T, $ent_1[Option[Set[T]]]]((dm: DataModel) => new $ent_1[Option[Set[T]]](dm))
         |
         |class ${ent_n}_ExprSetOpt_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetOpt_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_0}_ExprSetTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprSetTac_Enum[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprSetTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Set[T]](dataModel)
         |    with ExprSetTac_Enum[T, $ent_1[Set[T]]]((dm: DataModel) => new $ent_1[Set[T]](dm))
         |
         |class ${ent_n}_ExprSetTac_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetTac_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    ) else Nil
    List(List(base, enum).flatten.mkString("\n\n"))
  } else Nil


  val exprSeq = if (attributes.exists(a => a.cardinality == CardSeq && a.baseTpe != "Byte")) {
    val base = List(
      s"""// Seq =======================================================================================================
         |
         |class ${ent_1}_ExprSeqMan[T](override val dataModel: DataModel)
         |  extends $ent_1[Seq[T]](dataModel)
         |    with ExprSeqMan[T, $ent_1[Seq[T]]]((dm: DataModel) => new $ent_1[Seq[T]](dm))
         |
         |class ${ent_n}_ExprSeqMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqMan[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_1}_ExprSeqOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Seq[T]]](dataModel)
         |    with ExprSeqOpt[T, $ent_1[Option[Seq[T]]]]((dm: DataModel) => new $ent_1[Option[Seq[T]]](dm))
         |
         |class ${ent_n}_ExprSeqOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqOpt[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_0}_ExprSeqTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprSeqTac[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprSeqTac[T](override val dataModel: DataModel)
         |  extends $ent_1[Seq[T]](dataModel)
         |    with ExprSeqTac[T, $ent_1[Seq[T]]]((dm: DataModel) => new $ent_1[Seq[T]](dm))
         |
         |class ${ent_n}_ExprSeqTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )

    val enum = if (attributes.filter(_.cardinality == CardSeq).exists(_.enumTpe.isDefined)) List(
      s"""class ${ent_1}_ExprSeqMan_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Seq[T]](dataModel)
         |    with ExprSeqMan_Enum[T, $ent_1[Seq[T]]]((dm: DataModel) => new $ent_1[Seq[T]](dm))
         |
         |class ${ent_n}_ExprSeqMan_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqMan_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_1}_ExprSeqOpt_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Seq[T]]](dataModel)
         |    with ExprSeqOpt_Enum[T, $ent_1[Option[Seq[T]]]]((dm: DataModel) => new $ent_1[Option[Seq[T]]](dm))
         |
         |class ${ent_n}_ExprSeqOpt_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqOpt_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_0}_ExprSeqTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprSeqTac_Enum[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprSeqTac_Enum[T](override val dataModel: DataModel)
         |  extends $ent_1[Seq[T]](dataModel)
         |    with ExprSeqTac_Enum[T, $ent_1[Seq[T]]]((dm: DataModel) => new $ent_1[Seq[T]](dm))
         |
         |class ${ent_n}_ExprSeqTac_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqTac_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    ) else Nil
    List(List(base, enum).flatten.mkString("\n\n"))

  } else Nil


  val exprByteArray = if (attributes.exists(a => a.cardinality == CardSeq && a.baseTpe == "Byte")) {
    List(
      s"""// Byte Array ================================================================================================
         |
         |class ${ent_1}_ExprBArMan[T](override val dataModel: DataModel)
         |  extends $ent_1[Array[T]](dataModel)
         |    with ExprBArMan[T, $ent_1[Array[T]]]((dm: DataModel) => new $ent_1[Array[T]](dm))
         |
         |class ${ent_n}_ExprBArMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprBArMan[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_1}_ExprBArOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Array[T]]](dataModel)
         |    with ExprBArOpt[T, $ent_1[Option[Array[T]]]]((dm: DataModel) => new $ent_1[Option[Array[T]]](dm))
         |
         |class ${ent_n}_ExprBArOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprBArOpt[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |
         |
         |class ${ent_0}_ExprBArTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac[T, $ent_0] {
         |  override val entity = (dm: DataModel) => new $ent_0(dm)
         |}
         |class ${ent_1}_ExprBArTac[T](override val dataModel: DataModel)
         |  extends $ent_1[Array[T]](dataModel)
         |    with ExprBArTac[T, $ent_1[Array[T]]]((dm: DataModel) => new $ent_1[Array[T]](dm))
         |
         |class ${ent_n}_ExprBArTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprBArTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )
  } else Nil


  val exprMap = if (attributes.exists(_.cardinality == CardMap)) {
    List(
      s"""// Map =======================================================================================================
         |
         |class ${ent_1}_ExprMapMan[T](override val dataModel: DataModel)
         |  extends $ent_1[Map[String, T]](dataModel)
         |    with ExprMapMan_1[T, [t] =>> $ent_1[t]]([t] => (dm: DataModel) => new $ent_1[t](dm))
         |
         |class ${ent_n}_ExprMapMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprMapMan_n[T, Tpl, [tpl <: Tuple] =>> $ent_n[tpl]]([tpl <: Tuple] => (dm: DataModel) => new $ent_n[tpl](dm))
         |
         |
         |class ${ent_1}_ExprMapOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[Map[String, T]]](dataModel)
         |    with ExprMapOpt_1[T, [t] =>> $ent_1[t]]([t] => (dm: DataModel) => new $ent_1[t](dm))
         |
         |class ${ent_n}_ExprMapOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprMapOpt_n[T, Tpl, [t <: Tuple] =>> $ent_n[t]]([t <: Tuple] => (dm: DataModel) => new $ent_n[t](dm))
         |
         |
         |class ${ent_0}_ExprMapTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprMapTac[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprMapTac[T](override val dataModel: DataModel)
         |  extends $ent_1[Map[String, T]](dataModel)
         |    with ExprMapTac[T, $ent_1[Map[String, T]]]((dm: DataModel) => new $ent_1[Map[String, T]](dm))
         |
         |class ${ent_n}_ExprMapTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprMapTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )
  } else Nil

  def get: String = (exprOne ++ exprSet ++ exprSeq ++ exprByteArray ++ exprMap).map(_.trim).mkString("\n\n\n")
}
