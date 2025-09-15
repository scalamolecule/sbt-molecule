package sbtmolecule.db.dsl.ops

import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.Formatting


case class Entity_Exprs(
  metaDomain: MetaDomain,
  metaEntity: MetaEntity,
) extends Formatting(metaDomain, metaEntity) {
  val ent_0 = entity + "_0"
  val ent_1 = entity + "_1"
  val ent_n = entity + "_n"

  val sorts = List(
    s"""// Sort ========================================================================================================
       |
       |class ${entity}_1_Sort   [T           ](dm: DataModel) extends ${entity}_1[T        ](dm) with ${entity}_Sort_1[T        ] with OneValue
       |class ${entity}_1_SortOpt[T           ](dm: DataModel) extends ${entity}_1[Option[T]](dm) with ${entity}_Sort_1[Option[T]] with OneValue
       |class ${entity}_n_Sort   [Tpl <: Tuple](dm: DataModel) extends ${entity}_n[Tpl      ](dm) with ${entity}_Sort_n[Tpl      ] with OneValue
       |       |
       |trait ${entity}_Sort_1[T] extends Sort[${entity}_1[T]] { self: Molecule =>
       |  override def sortEntity: DataModel => ${entity}_1[T] = (dm: DataModel) => ${entity}_1[T](dm)
       |}
       |
       |trait ${entity}_Sort_n[Tpl <: Tuple] extends Sort[${entity}_n[Tpl]] { self: Molecule =>
       |  override def sortEntity: DataModel => ${entity}_n[Tpl] = (dm: DataModel) => ${entity}_n[Tpl](dm)
       |}
       |
       |""".stripMargin
  )

  val exprOne = if (attributes.exists(_.value == OneValue)) {
    val oneValueAttrs = attributes.filter(_.value == OneValue)

    val base = List(
      s"""// One =======================================================================================================
         |
         |class ${ent_1}_ExprOneMan[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ${entity}_Sort_1[T]
         |    with ExprOneMan_1[T, [t] =>> ${ent_1}_Sort[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_Sort[t](dm)
         |  )
         |    with ExprOneMan_1_Aggr[T, [t] =>> ${ent_1}_ExprOneMan_AggrOps[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_ExprOneMan_AggrOps[t](dm)
         |  )
         |
         |class ${ent_1}_ExprOneMan_AggrOps[T](dm: DataModel)
         |  extends ${ent_1}_Sort[T](dm)
         |    with ExprOneMan_1_AggrOps[T, ${ent_1}_Sort[T]](
         |    (dm: DataModel) => new ${ent_1}_Sort[T](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_1[T] = (dm: DataModel) => $ent_1[T](dm)
         |}
         |
         |class ${ent_n}_ExprOneMan[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ${entity}_Sort_n[Tpl]
         |    with ExprOneMan_n[T, Tpl, [tpl <: Tuple] =>> ${ent_n}_Sort[tpl]](
         |    [tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_Sort[tpl](dm)
         |  )
         |    with ExprOneMan_n_Aggr[T, Tpl, [t, tpl <: Tuple] =>> ${ent_n}_ExprOneMan_AggrOps[t, tpl]](
         |    [t, tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_ExprOneMan_AggrOps[t, tpl](dm)
         |  )
         |
         |class ${ent_n}_ExprOneMan_AggrOps[T, Tpl <: Tuple](dm: DataModel)
         |  extends ${ent_n}_Sort[Tpl](dm)
         |    with ExprOneMan_n_AggrOps[T, ${ent_n}_Sort[Tpl]](
         |    (dm: DataModel) => new ${ent_n}_Sort[Tpl](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_n[Tpl] = (dm: DataModel) => $ent_n[Tpl](dm)
         |}
         |
         |class ${ent_1}_ExprOneOpt[T](override val dataModel: DataModel)
         |  extends $ent_1[Option[T]](dataModel)
         |    with ExprOneOpt[T, ${ent_1}_SortOpt[T]]((dm: DataModel) => new ${ent_1}_SortOpt[T](dm))
         |    with ${entity}_Sort_1[Option[T]]
         |
         |class ${ent_n}_ExprOneOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneOpt[T, ${ent_n}_Sort[Tpl]]((dm: DataModel) => new ${ent_n}_Sort[Tpl](dm))
         |    with ${entity}_Sort_n[Tpl]
         |
         |
         |class ${ent_0}_ExprOneTac[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    )

    val string = if (oneValueAttrs.exists(_.baseTpe == "String")) List(
      s"""class ${ent_1}_ExprOneMan_String[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ${entity}_Sort_1[T]
         |    with ExprOneMan_1_String[T, [t] =>> ${ent_1}_Sort[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_Sort[t](dm)
         |  )
         |    with ExprOneMan_1_String_Aggr[T, [t] =>> ${ent_1}_ExprOneMan_String_AggrOps[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_ExprOneMan_String_AggrOps[t](dm)
         |  )
         |
         |class ${ent_1}_ExprOneMan_String_AggrOps[T](dm: DataModel)
         |  extends ${ent_1}_Sort[T](dm)
         |    with ExprOneMan_1_String_AggrOps[T, ${ent_1}_Sort[T]](
         |    (dm: DataModel) => new ${ent_1}_Sort[T](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_1[T] = (dm: DataModel) => $ent_1[T](dm)
         |}
         |
         |class ${ent_n}_ExprOneMan_String[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ${entity}_Sort_n[Tpl]
         |    with ExprOneMan_n_String[T, Tpl, [tpl <: Tuple] =>> ${ent_n}_Sort[tpl]](
         |    [tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_Sort[tpl](dm)
         |  )
         |    with ExprOneMan_n_String_Aggr[T, Tpl, [t, tpl <: Tuple] =>> ${ent_n}_ExprOneMan_String_AggrOps[t, tpl]](
         |    [t, tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_ExprOneMan_String_AggrOps[t, tpl](dm)
         |  )
         |
         |class ${ent_n}_ExprOneMan_String_AggrOps[T, Tpl <: Tuple](dm: DataModel)
         |  extends ${ent_n}_Sort[Tpl](dm)
         |    with ExprOneMan_n_String_AggrOps[T, ${ent_n}_Sort[Tpl]](
         |    (dm: DataModel) => new ${ent_n}_Sort[Tpl](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_n[Tpl] = (dm: DataModel) => $ent_n[Tpl](dm)
         |}
         |
         |class ${ent_0}_ExprOneTac_String[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_String[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac_String[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac_String[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac_String[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_String[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    ) else Nil

    val enum = if (oneValueAttrs.exists(_.enumTpe.isDefined)) List(
      s"""class ${ent_1}_ExprOneMan_Enum[EnumType](override val dataModel: DataModel)
         |  extends $ent_1[String](dataModel)
         |    with ExprOneMan_1_Enum[EnumType, ${ent_1}_Sort[String]]((dm: DataModel) => new ${ent_1}_Sort[String](dm))
         |    with ${entity}_Sort_1[String]
         |
         |class ${ent_n}_ExprOneMan_Enum[EnumType, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneMan_n_Enum[EnumType, ${ent_n}_Sort[Tpl]]((dm: DataModel) => new ${ent_n}_Sort[Tpl](dm))
         |    with ${entity}_Sort_n[Tpl]
         |
         |
         |class ${ent_1}_ExprOneOpt_Enum[EnumType](override val dataModel: DataModel)
         |  extends $ent_1[Option[String]](dataModel)
         |    with ExprOneOpt_Enum[EnumType, ${ent_1}_SortOpt[String]]((dm: DataModel) => new ${ent_1}_SortOpt[String](dm))
         |    with ${entity}_Sort_1[Option[String]]
         |
         |class ${ent_n}_ExprOneOpt_Enum[EnumType, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneOpt_Enum[EnumType, ${ent_n}_Sort[Tpl]]((dm: DataModel) => new ${ent_n}_Sort[Tpl](dm))
         |    with ${entity}_Sort_n[Tpl]
         |
         |
         |class ${ent_0}_ExprOneTac_Enum[EnumType](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Enum[EnumType, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac_Enum[S, EnumType](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac_Enum[EnumType, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac_Enum[EnumType, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Enum[EnumType, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    ) else Nil

    val integerTypes = List("Int", "Long", "BigInt", "Short", "Byte")
    val integer      = if (oneValueAttrs.exists(a => integerTypes.contains(a.baseTpe))) List(
      s"""class ${ent_1}_ExprOneMan_Integer[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ${entity}_Sort_1[T]
         |    with ExprOneMan_1_Integer[T, [t] =>> ${ent_1}_Sort[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_Sort[t](dm)
         |  )
         |    with ExprOneMan_1_Integer_Aggr[T, [t] =>> ${ent_1}_ExprOneMan_Integer_AggrOps[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_ExprOneMan_Integer_AggrOps[t](dm)
         |  )
         |
         |class ${ent_1}_ExprOneMan_Integer_AggrOps[T](dm: DataModel)
         |  extends ${ent_1}_Sort[T](dm)
         |    with ExprOneMan_1_Integer_AggrOps[T, ${ent_1}_Sort[T]](
         |    (dm: DataModel) => new ${ent_1}_Sort[T](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_1[T] = (dm: DataModel) => $ent_1[T](dm)
         |}
         |
         |class ${ent_n}_ExprOneMan_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ${entity}_Sort_n[Tpl]
         |    with ExprOneMan_n_Integer[T, Tpl, [tpl <: Tuple] =>> ${ent_n}_Sort[tpl]](
         |    [tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_Sort[tpl](dm)
         |  )
         |    with ExprOneMan_n_Integer_Aggr[T, Tpl, [t, tpl <: Tuple] =>> ${ent_n}_ExprOneMan_Integer_AggrOps[t, tpl]](
         |    [t, tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_ExprOneMan_Integer_AggrOps[t, tpl](dm)
         |  )
         |
         |class ${ent_n}_ExprOneMan_Integer_AggrOps[T, Tpl <: Tuple](dm: DataModel)
         |  extends ${ent_n}_Sort[Tpl](dm)
         |    with ExprOneMan_n_Integer_AggrOps[T, ${ent_n}_Sort[Tpl]](
         |    (dm: DataModel) => new ${ent_n}_Sort[Tpl](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_n[Tpl] = (dm: DataModel) => $ent_n[Tpl](dm)
         |}
         |
         |class ${ent_0}_ExprOneTac_Integer[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Integer[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac_Integer[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac_Integer[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Integer[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    ) else Nil

    val decimalTypes = List("Double", "Float", "BigDecimal")
    val decimal      = if (oneValueAttrs.exists(a => decimalTypes.contains(a.baseTpe))) List(
      s"""class ${ent_1}_ExprOneMan_Decimal[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ${entity}_Sort_1[T]
         |    with ExprOneMan_1_Decimal[T, [t] =>> ${ent_1}_Sort[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_Sort[t](dm)
         |  )
         |    with ExprOneMan_1_Decimal_Aggr[T, [t] =>> ${ent_1}_ExprOneMan_Decimal_AggrOps[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_ExprOneMan_Decimal_AggrOps[t](dm)
         |  )
         |
         |class ${ent_1}_ExprOneMan_Decimal_AggrOps[T](dm: DataModel)
         |  extends ${ent_1}_Sort[T](dm)
         |    with ExprOneMan_1_Decimal_AggrOps[T, ${ent_1}_Sort[T]](
         |    (dm: DataModel) => new ${ent_1}_Sort[T](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_1[T] = (dm: DataModel) => $ent_1[T](dm)
         |}
         |
         |class ${ent_n}_ExprOneMan_Decimal[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ${entity}_Sort_n[Tpl]
         |    with ExprOneMan_n_Decimal[T, Tpl, [tpl <: Tuple] =>> ${ent_n}_Sort[tpl]](
         |    [tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_Sort[tpl](dm)
         |  )
         |    with ExprOneMan_n_Decimal_Aggr[T, Tpl, [t, tpl <: Tuple] =>> ${ent_n}_ExprOneMan_Decimal_AggrOps[t, tpl]](
         |    [t, tpl <: Tuple] => (dm: DataModel) => ${ent_n}_ExprOneMan_Decimal_AggrOps[t, tpl](dm)
         |  )
         |
         |class ${ent_n}_ExprOneMan_Decimal_AggrOps[T, Tpl <: Tuple](dm: DataModel)
         |  extends ${ent_n}_Sort[Tpl](dm)
         |    with ExprOneMan_n_Decimal_AggrOps[T, ${ent_n}_Sort[Tpl]](
         |    (dm: DataModel) => new ${ent_n}_Sort[Tpl](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_n[Tpl] = (dm: DataModel) => $ent_n[Tpl](dm)
         |}
         |
         |class ${ent_0}_ExprOneTac_Decimal[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Decimal[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac_Decimal[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac_Decimal[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac_Decimal[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Decimal[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    ) else Nil

    val boolean = if (oneValueAttrs.exists(_.baseTpe == "Boolean")) List(
      s"""class ${ent_1}_ExprOneMan_Boolean[T](override val dataModel: DataModel)
         |  extends $ent_1[T](dataModel)
         |    with ${entity}_Sort_1[T]
         |    with ExprOneMan_1_Boolean[T, [t] =>> ${ent_1}_Sort[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_Sort[t](dm)
         |  )
         |    with ExprOneMan_1_Boolean_Aggr[T, [t] =>> ${ent_1}_ExprOneMan_Boolean_AggrOps[t]](
         |    [t] => (dm: DataModel) => new ${ent_1}_ExprOneMan_Boolean_AggrOps[t](dm)
         |  )
         |
         |class ${ent_1}_ExprOneMan_Boolean_AggrOps[T](dm: DataModel)
         |  extends ${ent_1}_Sort[T](dm)
         |    with ExprOneMan_1_Boolean_AggrOps[T, ${ent_1}_Sort[T]](
         |    (dm: DataModel) => new ${ent_1}_Sort[T](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_1[T] = (dm: DataModel) => $ent_1[T](dm)
         |}
         |
         |class ${ent_n}_ExprOneMan_Boolean[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ${entity}_Sort_n[Tpl]
         |    with ExprOneMan_n_Boolean[T, Tpl, [tpl <: Tuple] =>> ${ent_n}_Sort[tpl]](
         |    [tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_Sort[tpl](dm)
         |  )
         |    with ExprOneMan_n_Boolean_Aggr[T, Tpl, [t, tpl <: Tuple] =>> ${ent_n}_ExprOneMan_Boolean_AggrOps[t, tpl]](
         |    [t, tpl <: Tuple] => (dm: DataModel) => new ${ent_n}_ExprOneMan_Boolean_AggrOps[t, tpl](dm)
         |  )
         |
         |class ${ent_n}_ExprOneMan_Boolean_AggrOps[T, Tpl <: Tuple](dm: DataModel)
         |  extends ${ent_n}_Sort[Tpl](dm)
         |    with ExprOneMan_n_Boolean_AggrOps[T, ${ent_n}_Sort[Tpl]](
         |    (dm: DataModel) => new ${ent_n}_Sort[Tpl](dm)
         |  ) { self: Molecule =>
         |  override def sortEntity: DataModel => $ent_n[Tpl] = (dm: DataModel) => $ent_n[Tpl](dm)
         |}
         |
         |class ${ent_0}_ExprOneTac_Boolean[T](override val dataModel: DataModel)
         |  extends $ent_0(dataModel)
         |    with ExprOneTac_Boolean[T, $ent_0]((dm: DataModel) => new $ent_0(dm) with OneValue)
         |
         |class ${ent_1}_ExprOneTac_Boolean[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprOneTac_Boolean[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm) with  OneValue)
         |
         |class ${ent_n}_ExprOneTac_Boolean[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprOneTac_Boolean[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm) with  OneValue)
         |""".stripMargin
    ) else Nil

    List(List(base, string, enum, integer, decimal, boolean).flatten.mkString("\n\n"))
  } else Nil


  val exprSet = if (attributes.exists(_.value == SetValue)) {
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
         |class ${ent_1}_ExprSetTac[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprSetTac[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprSetTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )

    val enum = if (attributes.filter(_.value == SetValue).exists(_.enumTpe.isDefined)) List(
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
         |class ${ent_1}_ExprSetTac_Enum[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprSetTac_Enum[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprSetTac_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSetTac_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    ) else Nil
    List(List(base, enum).flatten.mkString("\n\n"))
  } else Nil


  val exprSeq = if (attributes.exists(a => a.value == SeqValue && a.baseTpe != "Byte")) {
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
         |class ${ent_1}_ExprSeqTac[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprSeqTac[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprSeqTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )

    val enum = if (attributes.filter(_.value == SeqValue).exists(_.enumTpe.isDefined)) List(
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
         |class ${ent_1}_ExprSeqTac_Enum[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprSeqTac_Enum[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprSeqTac_Enum[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprSeqTac_Enum[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    ) else Nil
    List(List(base, enum).flatten.mkString("\n\n"))

  } else Nil


  val exprByteArray = if (attributes.exists(a => a.value == SeqValue && a.baseTpe == "Byte")) {
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
         |    with ExprOneTac[T, $ent_0]((dm: DataModel) => new $ent_0(dm))
         |
         |class ${ent_1}_ExprBArTac[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprBArTac[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprBArTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprBArTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )
  } else Nil


  val exprMap = if (attributes.exists(_.value == MapValue)) {
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
         |class ${ent_1}_ExprMapTac[S, T](override val dataModel: DataModel)
         |  extends $ent_1[S](dataModel)
         |    with ExprMapTac[T, $ent_1[S]]((dm: DataModel) => new $ent_1[S](dm))
         |
         |class ${ent_n}_ExprMapTac[T, Tpl <: Tuple](override val dataModel: DataModel)
         |  extends $ent_n[Tpl](dataModel)
         |    with ExprMapTac[T, $ent_n[Tpl]]((dm: DataModel) => new $ent_n[Tpl](dm))
         |""".stripMargin
    )
  } else Nil

  def get: String = (sorts ++ exprOne ++ exprSet ++ exprSeq ++ exprByteArray ++ exprMap).map(_.trim).mkString("\n\n\n")
}
