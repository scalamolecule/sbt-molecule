// AUTO-GENERATED Molecule boilerplate code
package app.domain.dsl.Bar
package ops // to access enums and let them be public to the user

import molecule.core.dataModel as _dm
import molecule.core.dataModel.*
import molecule.db.common.api.*
import molecule.db.common.api.expression.*
import molecule.db.common.ops.ModelTransformations_.*
import scala.Tuple.:*


class Person_0(override val dataModel: DataModel) extends Molecule_0 with Person_attrs {
  final lazy val id     = Person_1_ExprOneMan        [Long  ](dataModel.add(id_man  ))
  final lazy val name   = Person_1_ExprOneMan_String [String](dataModel.add(name_man))
  final lazy val age    = Person_1_ExprOneMan_Integer[Int   ](dataModel.add(age_man ))

  final lazy val name_? = Person_1_ExprOneOpt        [String](dataModel.add(name_opt))
  final lazy val age_?  = Person_1_ExprOneOpt        [Int   ](dataModel.add(age_opt ))

  final lazy val id_    = Person_0_ExprOneTac        [Long  ](dataModel.add(id_tac  ))
  final lazy val name_  = Person_0_ExprOneTac_String [String](dataModel.add(name_tac))
  final lazy val age_   = Person_0_ExprOneTac_Integer[Int   ](dataModel.add(age_tac ))
}


class Person_1[T](override val dataModel: DataModel) extends Molecule_1[T] with Person_attrs {
  final lazy val id     = Person_n_ExprOneMan        [Long  , (T, Long          )](dataModel.add(id_man  ))
  final lazy val name   = Person_n_ExprOneMan_String [String, (T, String        )](dataModel.add(name_man))
  final lazy val age    = Person_n_ExprOneMan_Integer[Int   , (T, Int           )](dataModel.add(age_man ))

  final lazy val name_? = Person_n_ExprOneOpt        [String, (T, Option[String])](dataModel.add(name_opt))
  final lazy val age_?  = Person_n_ExprOneOpt        [Int   , (T, Option[Int   ])](dataModel.add(age_opt ))

  final lazy val id_    = Person_1_ExprOneTac        [T, Long                    ](dataModel.add(id_tac  ))
  final lazy val name_  = Person_1_ExprOneTac_String [T, String                  ](dataModel.add(name_tac))
  final lazy val age_   = Person_1_ExprOneTac_Integer[T, Int                     ](dataModel.add(age_tac ))
}


class Person_n[Tpl <: Tuple](override val dataModel: DataModel) extends Molecule_n[Tpl] with Person_attrs {
  final lazy val id     = Person_n_ExprOneMan        [Long  , Tpl :* Long          ](dataModel.add(id_man  ))
  final lazy val name   = Person_n_ExprOneMan_String [String, Tpl :* String        ](dataModel.add(name_man))
  final lazy val age    = Person_n_ExprOneMan_Integer[Int   , Tpl :* Int           ](dataModel.add(age_man ))

  final lazy val name_? = Person_n_ExprOneOpt        [String, Tpl :* Option[String]](dataModel.add(name_opt))
  final lazy val age_?  = Person_n_ExprOneOpt        [Int   , Tpl :* Option[Int   ]](dataModel.add(age_opt ))

  final lazy val id_    = Person_n_ExprOneTac        [Long  , Tpl                  ](dataModel.add(id_tac  ))
  final lazy val name_  = Person_n_ExprOneTac_String [String, Tpl                  ](dataModel.add(name_tac))
  final lazy val age_   = Person_n_ExprOneTac_Integer[Int   , Tpl                  ](dataModel.add(age_tac ))
}



// Sort ========================================================================================================

class Person_1_Sort   [T           ](dm: DataModel) extends Person_1[T        ](dm) with Person_Sort_1[T        ] with OneValue
class Person_1_SortOpt[T           ](dm: DataModel) extends Person_1[Option[T]](dm) with Person_Sort_1[Option[T]] with OneValue
class Person_n_Sort   [Tpl <: Tuple](dm: DataModel) extends Person_n[Tpl      ](dm) with Person_Sort_n[Tpl      ] with OneValue

trait Person_Sort_1[T] extends Sort[Person_1[T]] { self: Molecule =>
  override def sortEntity: DataModel => Person_1[T] = (dm: DataModel) => Person_1[T](dm)
}

trait Person_Sort_n[Tpl <: Tuple] extends Sort[Person_n[Tpl]] { self: Molecule =>
  override def sortEntity: DataModel => Person_n[Tpl] = (dm: DataModel) => Person_n[Tpl](dm)
}


// One =======================================================================================================

class Person_1_ExprOneMan[T](override val dataModel: DataModel)
  extends Person_1[T](dataModel)
    with Person_Sort_1[T]
    with ExprOneMan_1[T, [t] =>> Person_1_Sort[t]](
    [t] => (dm: DataModel) => new Person_1_Sort[t](dm)
  )
    with ExprOneMan_1_Aggr[T, [t] =>> Person_1_ExprOneMan_AggrOps[t]](
    [t] => (dm: DataModel) => new Person_1_ExprOneMan_AggrOps[t](dm)
  )

class Person_1_ExprOneMan_AggrOps[T](dm: DataModel)
  extends Person_1_Sort[T](dm)
    with ExprOneMan_1_AggrOps[T, Person_1_Sort[T]](
    (dm: DataModel) => new Person_1_Sort[T](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_1[T] = (dm: DataModel) => Person_1[T](dm)
}

class Person_n_ExprOneMan[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with Person_Sort_n[Tpl]
    with ExprOneMan_n[T, Tpl, [tpl <: Tuple] =>> Person_n_Sort[tpl]](
    [tpl <: Tuple] => (dm: DataModel) => new Person_n_Sort[tpl](dm)
  )
    with ExprOneMan_n_Aggr[T, Tpl, [t, tpl <: Tuple] =>> Person_n_ExprOneMan_AggrOps[t, tpl]](
    [t, tpl <: Tuple] => (dm: DataModel) => new Person_n_ExprOneMan_AggrOps[t, tpl](dm)
  )

class Person_n_ExprOneMan_AggrOps[T, Tpl <: Tuple](dm: DataModel)
  extends Person_n_Sort[Tpl](dm)
    with ExprOneMan_n_AggrOps[T, Person_n_Sort[Tpl]](
    (dm: DataModel) => new Person_n_Sort[Tpl](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_n[Tpl] = (dm: DataModel) => Person_n[Tpl](dm)
}

class Person_1_ExprOneOpt[T](override val dataModel: DataModel)
  extends Person_1[Option[T]](dataModel)
    with ExprOneOpt[T, Person_1_SortOpt[T]]((dm: DataModel) => new Person_1_SortOpt[T](dm))
    with Person_Sort_1[Option[T]]

class Person_n_ExprOneOpt[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with ExprOneOpt[T, Person_n_Sort[Tpl]]((dm: DataModel) => new Person_n_Sort[Tpl](dm))
    with Person_Sort_n[Tpl]


class Person_0_ExprOneTac[T](override val dataModel: DataModel)
  extends Person_0(dataModel)
    with ExprOneTac[T, Person_0]((dm: DataModel) => new Person_0(dm) with OneValue)

class Person_1_ExprOneTac[S, T](override val dataModel: DataModel)
  extends Person_1[S](dataModel)
    with ExprOneTac[T, Person_1[S]]((dm: DataModel) => new Person_1[S](dm) with  OneValue)

class Person_n_ExprOneTac[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with ExprOneTac[T, Person_n[Tpl]]((dm: DataModel) => new Person_n[Tpl](dm) with  OneValue)


class Person_1_ExprOneMan_String[T](override val dataModel: DataModel)
  extends Person_1[T](dataModel)
    with Person_Sort_1[T]
    with ExprOneMan_1_String[T, [t] =>> Person_1_Sort[t]](
    [t] => (dm: DataModel) => new Person_1_Sort[t](dm)
  )
    with ExprOneMan_1_String_Aggr[T, [t] =>> Person_1_ExprOneMan_String_AggrOps[t]](
    [t] => (dm: DataModel) => new Person_1_ExprOneMan_String_AggrOps[t](dm)
  )

class Person_1_ExprOneMan_String_AggrOps[T](dm: DataModel)
  extends Person_1_Sort[T](dm)
    with ExprOneMan_1_String_AggrOps[T, Person_1_Sort[T]](
    (dm: DataModel) => new Person_1_Sort[T](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_1[T] = (dm: DataModel) => Person_1[T](dm)
}

class Person_n_ExprOneMan_String[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with Person_Sort_n[Tpl]
    with ExprOneMan_n_String[T, Tpl, [tpl <: Tuple] =>> Person_n_Sort[tpl]](
    [tpl <: Tuple] => (dm: DataModel) => new Person_n_Sort[tpl](dm)
  )
    with ExprOneMan_n_String_Aggr[T, Tpl, [t, tpl <: Tuple] =>> Person_n_ExprOneMan_String_AggrOps[t, tpl]](
    [t, tpl <: Tuple] => (dm: DataModel) => new Person_n_ExprOneMan_String_AggrOps[t, tpl](dm)
  )

class Person_n_ExprOneMan_String_AggrOps[T, Tpl <: Tuple](dm: DataModel)
  extends Person_n_Sort[Tpl](dm)
    with ExprOneMan_n_String_AggrOps[T, Person_n_Sort[Tpl]](
    (dm: DataModel) => new Person_n_Sort[Tpl](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_n[Tpl] = (dm: DataModel) => Person_n[Tpl](dm)
}

class Person_0_ExprOneTac_String[T](override val dataModel: DataModel)
  extends Person_0(dataModel)
    with ExprOneTac_String[T, Person_0]((dm: DataModel) => new Person_0(dm) with OneValue)

class Person_1_ExprOneTac_String[S, T](override val dataModel: DataModel)
  extends Person_1[S](dataModel)
    with ExprOneTac_String[T, Person_1[S]]((dm: DataModel) => new Person_1[S](dm) with  OneValue)

class Person_n_ExprOneTac_String[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with ExprOneTac_String[T, Person_n[Tpl]]((dm: DataModel) => new Person_n[Tpl](dm) with  OneValue)


class Person_1_ExprOneMan_Integer[T](override val dataModel: DataModel)
  extends Person_1[T](dataModel)
    with Person_Sort_1[T]
    with ExprOneMan_1_Integer[T, [t] =>> Person_1_Sort[t]](
    [t] => (dm: DataModel) => new Person_1_Sort[t](dm)
  )
    with ExprOneMan_1_Integer_Aggr[T, [t] =>> Person_1_ExprOneMan_Integer_AggrOps[t]](
    [t] => (dm: DataModel) => new Person_1_ExprOneMan_Integer_AggrOps[t](dm)
  )

class Person_1_ExprOneMan_Integer_AggrOps[T](dm: DataModel)
  extends Person_1_Sort[T](dm)
    with ExprOneMan_1_Integer_AggrOps[T, Person_1_Sort[T]](
    (dm: DataModel) => new Person_1_Sort[T](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_1[T] = (dm: DataModel) => Person_1[T](dm)
}

class Person_n_ExprOneMan_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with Person_Sort_n[Tpl]
    with ExprOneMan_n_Integer[T, Tpl, [tpl <: Tuple] =>> Person_n_Sort[tpl]](
    [tpl <: Tuple] => (dm: DataModel) => new Person_n_Sort[tpl](dm)
  )
    with ExprOneMan_n_Integer_Aggr[T, Tpl, [t, tpl <: Tuple] =>> Person_n_ExprOneMan_Integer_AggrOps[t, tpl]](
    [t, tpl <: Tuple] => (dm: DataModel) => new Person_n_ExprOneMan_Integer_AggrOps[t, tpl](dm)
  )

class Person_n_ExprOneMan_Integer_AggrOps[T, Tpl <: Tuple](dm: DataModel)
  extends Person_n_Sort[Tpl](dm)
    with ExprOneMan_n_Integer_AggrOps[T, Person_n_Sort[Tpl]](
    (dm: DataModel) => new Person_n_Sort[Tpl](dm)
  ) { self: Molecule =>
  override def sortEntity: DataModel => Person_n[Tpl] = (dm: DataModel) => Person_n[Tpl](dm)
}

class Person_0_ExprOneTac_Integer[T](override val dataModel: DataModel)
  extends Person_0(dataModel)
    with ExprOneTac_Integer[T, Person_0]((dm: DataModel) => new Person_0(dm) with OneValue)

class Person_1_ExprOneTac_Integer[S, T](override val dataModel: DataModel)
  extends Person_1[S](dataModel)
    with ExprOneTac_Integer[T, Person_1[S]]((dm: DataModel) => new Person_1[S](dm) with  OneValue)

class Person_n_ExprOneTac_Integer[T, Tpl <: Tuple](override val dataModel: DataModel)
  extends Person_n[Tpl](dataModel)
    with ExprOneTac_Integer[T, Person_n[Tpl]]((dm: DataModel) => new Person_n[Tpl](dm) with  OneValue)


trait Person_attrs {
  protected def id_man   = AttrOneManID    ("Person", "id"  , coord = List(0, 0))
  protected def name_man = AttrOneManString("Person", "name", coord = List(0, 1))
  protected def age_man  = AttrOneManInt   ("Person", "age" , coord = List(0, 2))
  
  protected def name_opt = AttrOneOptString("Person", "name", coord = List(0, 1))
  protected def age_opt  = AttrOneOptInt   ("Person", "age" , coord = List(0, 2))
  
  protected def id_tac   = AttrOneTacID    ("Person", "id"  , coord = List(0, 0))
  protected def name_tac = AttrOneTacString("Person", "name", coord = List(0, 1))
  protected def age_tac  = AttrOneTacInt   ("Person", "age" , coord = List(0, 2))
}

