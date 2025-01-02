package sbtmolecule.domain

import molecule.Domain

object Cardinalities extends Domain(6) {

  //  trait Card1 {
  //    val one = oneInt
  //  }
  //
  //  trait Card2 {
  //    val one = oneInt
  //    val set = setInt
  //  }
  //
  //  trait Card3 {
  //    val one = oneInt
  //    val set = setInt
  //    val arr = arrInt
  //  }

  trait Human {
    //    val name = oneString

    val one = oneBigDecimal
    val set = setInt
    val seq = seqInt
//    val ba  = arrayByte
//    val map = mapInt

    //    val map2  = mapBoolean
  }
}