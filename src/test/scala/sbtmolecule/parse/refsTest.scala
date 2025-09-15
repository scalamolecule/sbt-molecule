package sbtmolecule.parse

import molecule.DomainStructure
import molecule.base.metaModel.*
import molecule.core.dataModel.*
import sbtmolecule.ParseAndGenerate
import utest.*

object refs extends DomainStructure {

  trait A {
    val i    = oneInt
    val iSet = setInt
    val iSeq = seqInt
    val iMap = mapInt
    val s    = oneString
    val bool = oneBoolean

    val a    = manyToOne[A].oneToMany("Aa")
    val b    = manyToOne[B].oneToMany("Aa")
    val b1   = manyToOne[B].oneToMany("Aa1")
    val b2   = manyToOne[B].oneToMany("Aa2")
    val c    = manyToOne[C].oneToMany("Aa")
    val d    = manyToOne[D].oneToMany("Aa")
  }

  trait B {
    val i    = oneInt
    val iSet = setInt
    val iSeq = seqInt
    val iMap = mapInt
    val s    = oneString

    val a    = manyToOne[A].oneToMany("Bb")
    val b    = manyToOne[B].oneToMany("Bb")
    val c    = manyToOne[C].oneToMany("Bb")
    val c1   = manyToOne[C].oneToMany("Bb1")
    val d    = manyToOne[D].oneToMany("Bb")
  }

  trait C {
    val i    = oneInt
    val s    = oneString
    val iSet = setInt
    val iSeq = seqInt
    val iMap = mapInt
    val a    = manyToOne[A].oneToMany("Cc")
    val b    = manyToOne[B].oneToMany("Cc")
    val d    = manyToOne[D].oneToMany("Cc")
  }


  trait D {
    val i  = oneInt
    val s  = oneString
    val a  = manyToOne[A].oneToMany("Dd")
    val b  = manyToOne[B].oneToMany("Dd")
    val c  = manyToOne[C].oneToMany("Dd")
    val e  = manyToOne[E].oneToMany("Dd")
    val e1 = manyToOne[E].oneToMany("Dd1")
  }

  trait E {
    val i = oneInt
    val s = oneString
    val d = manyToOne[D].oneToMany("Ee")
    val f = manyToOne[F]
  }

  trait F {
    val i = oneInt
    val s = oneString
    val e = manyToOne[E].oneToMany("Ff")
    val g = manyToOne[G]
  }

  trait G {
    val i = oneInt
    val s = oneString
    val f = manyToOne[F].oneToMany("Gg")
    val h = manyToOne[H]
  }

  trait H {
    val i = oneInt
    val s = oneString
    val g = manyToOne[G].oneToMany("Hh")
  }
}

object refsTest extends TestSuite {

  override def tests: Tests = Tests {

    "DSL" - {
      val path      = System.getProperty("user.dir") + "/src/test/scala/sbtmolecule/parse/"
      val generator = ParseAndGenerate(path + "refsTest.scala").generator
      generator.metaDomain ==>
        MetaDomain("sbtmolecule.parse", "refs", List(
          MetaSegment("", List(
            MetaEntity("A", List(
              MetaAttribute("id"  , OneValue, "ID"     , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"   , OneValue, "Int"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSet", SetValue, "Int"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSeq", SeqValue, "Int"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iMap", MapValue, "Int"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s"   , OneValue, "String" , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("bool", OneValue, "Boolean", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("a"   , OneValue, "ID"     , Nil, Some("A"), Some("Aa"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b"   , OneValue, "ID"     , Nil, Some("B"), Some("Aa"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b1"  , OneValue, "ID"     , Nil, Some("B"), Some("Aa1"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b2"  , OneValue, "ID"     , Nil, Some("B"), Some("Aa2"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("c"   , OneValue, "ID"     , Nil, Some("C"), Some("Aa"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("d"   , OneValue, "ID"     , Nil, Some("D"), Some("Aa"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa"  , SetValue, "ID"     , Nil, Some("A"), Some("a"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Bb"  , SetValue, "ID"     , Nil, Some("B"), Some("a"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Cc"  , SetValue, "ID"     , Nil, Some("C"), Some("a"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Dd"  , SetValue, "ID"     , Nil, Some("D"), Some("a"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("A", "B", "C", "D"), List(), List(), false, None),

            MetaEntity("B", List(
              MetaAttribute("id"  , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"   , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSet", SetValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSeq", SeqValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iMap", MapValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s"   , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("a"   , OneValue, "ID"    , Nil, Some("A"), Some("Bb"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b"   , OneValue, "ID"    , Nil, Some("B"), Some("Bb"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("c"   , OneValue, "ID"    , Nil, Some("C"), Some("Bb"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("c1"  , OneValue, "ID"    , Nil, Some("C"), Some("Bb1"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("d"   , OneValue, "ID"    , Nil, Some("D"), Some("Bb"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa"  , SetValue, "ID"    , Nil, Some("A"), Some("b"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa1" , SetValue, "ID"    , Nil, Some("A"), Some("b1"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa2" , SetValue, "ID"    , Nil, Some("A"), Some("b2"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Bb"  , SetValue, "ID"    , Nil, Some("B"), Some("b"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Cc"  , SetValue, "ID"    , Nil, Some("C"), Some("b"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Dd"  , SetValue, "ID"    , Nil, Some("D"), Some("b"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("A", "B", "C", "D"), List(), List(), false, None),

            MetaEntity("C", List(
              MetaAttribute("id"  , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"   , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s"   , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSet", SetValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iSeq", SeqValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("iMap", MapValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("a"   , OneValue, "ID"    , Nil, Some("A"), Some("Cc"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b"   , OneValue, "ID"    , Nil, Some("B"), Some("Cc"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("d"   , OneValue, "ID"    , Nil, Some("D"), Some("Cc"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa"  , SetValue, "ID"    , Nil, Some("A"), Some("c"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Bb"  , SetValue, "ID"    , Nil, Some("B"), Some("c"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Bb1" , SetValue, "ID"    , Nil, Some("B"), Some("c1"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Dd"  , SetValue, "ID"    , Nil, Some("D"), Some("c"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("A", "B", "D"), List(), List(), false, None),

            MetaEntity("D", List(
              MetaAttribute("id", OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i" , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s" , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("a" , OneValue, "ID"    , Nil, Some("A"), Some("Dd"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("b" , OneValue, "ID"    , Nil, Some("B"), Some("Dd"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("c" , OneValue, "ID"    , Nil, Some("C"), Some("Dd"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("e" , OneValue, "ID"    , Nil, Some("E"), Some("Dd"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("e1", OneValue, "ID"    , Nil, Some("E"), Some("Dd1"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Aa", SetValue, "ID"    , Nil, Some("A"), Some("d"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Bb", SetValue, "ID"    , Nil, Some("B"), Some("d"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Cc", SetValue, "ID"    , Nil, Some("C"), Some("d"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Ee", SetValue, "ID"    , Nil, Some("E"), Some("d"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("A", "B", "C", "E"), List(), List(), false, None),

            MetaEntity("E", List(
              MetaAttribute("id" , OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i"  , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s"  , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("d"  , OneValue, "ID"    , Nil, Some("D"), Some("Ee"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("f"  , OneValue, "ID"    , Nil, Some("F"), Some("Es"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Dd" , SetValue, "ID"    , Nil, Some("D"), Some("e"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Dd1", SetValue, "ID"    , Nil, Some("D"), Some("e1"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Ff" , SetValue, "ID"    , Nil, Some("F"), Some("e"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("D", "F"), List(), List(), false, None),

            MetaEntity("F", List(
              MetaAttribute("id", OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i" , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s" , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("e" , OneValue, "ID"    , Nil, Some("E"), Some("Ff"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("g" , OneValue, "ID"    , Nil, Some("G"), Some("Fs"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Es", SetValue, "ID"    , Nil, Some("E"), Some("f"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Gg", SetValue, "ID"    , Nil, Some("G"), Some("f"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("E", "G"), List(), List(), false, None),

            MetaEntity("G", List(
              MetaAttribute("id", OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i" , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s" , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("f" , OneValue, "ID"    , Nil, Some("F"), Some("Gg"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("h" , OneValue, "ID"    , Nil, Some("H"), Some("Gs"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Fs", SetValue, "ID"    , Nil, Some("F"), Some("g"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Hh", SetValue, "ID"    , Nil, Some("H"), Some("g"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("F", "H"), List(), List(), false, None),

            MetaEntity("H", List(
              MetaAttribute("id", OneValue, "ID"    , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("i" , OneValue, "Int"   , Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("s" , OneValue, "String", Nil, None, None, None, None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("g" , OneValue, "ID"    , Nil, Some("G"), Some("Hh"), Some("ManyToOne"), None, Nil, None, Nil, Nil, Nil, None),
              MetaAttribute("Gs", SetValue, "ID"    , Nil, Some("G"), Some("h"), Some("OneToMany"), None, Nil, None, Nil, Nil, Nil, None)
            ), List("G"), List(), List(), false, None)
          ))
        ))


      //      generator.printEntity(generator.metaDomain.segments.head.entities(1))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(0))
      generator.printEntityBuilder(generator.metaDomain.segments.head.entities(1))
    }
  }
}
