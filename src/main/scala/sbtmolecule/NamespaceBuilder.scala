package sbtmolecule

import Ast._


case class NamespaceBuilder(d: Ast.Definition, docs: Boolean) {

  val crossAttrs: Seq[String] = {
    (for {
      ns <- d.nss
      attr <- ns.attrs
    } yield attr).collect {
      case Ref(_, _, _, _, _, _, edgeNs, _, Some("BiEdgeRef_"), revRef, _)  => s"$edgeNs $revRef"
      case Ref(_, _, _, _, _, _, refNs, _, Some("BiTargetRef_"), revRef, _) => s"$refNs $revRef"
      case Ref(_, _, _, _, _, _, refNs, _, Some("BiOtherRef_"), revRef, _)  => s"$refNs $revRef"
    }.distinct.sorted
  }


  def nsTrait(domain0: String, namesp: Namespace, in: Int, out: Int, maxIn: Int, maxOut: Int, nsArities: Map[String, Int], docs: Boolean): String = {
    val (domain, ns, option, attrs) = (firstLow(domain0), namesp.ns, namesp.opt, namesp.attrs)
    val InTypes: Seq[String] = (0 until in) map (n => "I" + (n + 1))
    val OutTypes: Seq[String] = (0 until out) map (n => (n + 'A').toChar.toString)
    val maxTpe: Int = attrs.filter(!_.attr.startsWith("_")).map(_.tpe.length).max
    val maxAttr: Int = attrs.map(_.attr).filter(!_.startsWith("_")).map(_.length).max
    val maxTpeK = {
      val tpeKs = attrs.flatMap {
        case Val(_, attr, clazz, tpe, "K", _, _, _, _, _) if !attr.startsWith("_") => Some(tpe.length)
        case other                                                                 => None
      }
      if (tpeKs.isEmpty) 0 else tpeKs.max
    }
    val overr = if (docs) "override " else ""
    def pp(n: Int) = s"P$n[" + (1 to n).map(j => "_").mkString(",") + "]"

    val nextStay = {
      val ins = InTypes.mkString(", ")
      val outs = OutTypes.mkString(", ")
      (in, out) match {
        case (0, 0) if maxIn == 0 => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_1[Type], P2[_,_]] with ${ns}_1[Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_0, P1[_]] with ${ns}_0"
        )

        case (0, `maxOut`) if maxIn == 0 => Seq(
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_$maxOut[$outs], ${pp(maxOut + 2)}] with ${ns}_$maxOut[$outs]"
        )

        case (0, o) if maxIn == 0 => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_${o + 1}[$outs, Type], ${pp(o + 2)}] with ${ns}_${o + 1}[$outs, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_$o[$outs], ${pp(o + 1)}] with ${ns}_$o[$outs]"
        )

        case (0, 0) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_1[Type], ${ns}_In_1_1[Type, Type]] with ${ns}_1[Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_0, ${ns}_In_1_0[Type]] with ${ns}_0"
        )

        case (0, `maxOut`) => Seq(
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_$maxOut[$outs], ${ns}_In_1_$maxOut[Type, $outs]] with ${ns}_$maxOut[$outs]"
        )

        case (0, o) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_${o + 1}[$outs, Type], ${ns}_In_1_${o + 1}[Type, $outs, Type]] with ${ns}_${o + 1}[$outs, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_$o[$outs], ${ns}_In_1_$o[Type, $outs]] with ${ns}_$o[$outs]"
        )

        case (`maxIn`, 0) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_In_${maxIn}_1[$ins, Type], ${pp(maxIn + 2)}] with ${ns}_In_${maxIn}_1[$ins, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${maxIn}_0[$ins], ${pp(maxIn + 1)}] with ${ns}_In_${maxIn}_0[$ins]"
        )

        case (`maxIn`, `maxOut`) => Seq(
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${maxIn}_$maxOut[$ins, $outs], ${pp(maxIn + maxOut + 1)}] with ${ns}_In_${maxIn}_$maxOut[$ins, $outs]"
        )

        case (`maxIn`, o) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_In_${maxIn}_${o + 1}[$ins, $outs, Type], ${pp(maxIn + o + 2)}] with ${ns}_In_${maxIn}_${o + 1}[$ins, $outs, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${maxIn}_$o[$ins, $outs], ${pp(maxIn + o + 1)}] with ${ns}_In_${maxIn}_$o[$ins, $outs]"
        )

        case (i, 0) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_In_${i}_1[$ins, Type], ${ns}_In_${i + 1}_1[$ins, Type, Type]] with ${ns}_In_${i}_1[$ins, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${i}_0[$ins], ${ns}_In_${i + 1}_0[$ins, Type]] with ${ns}_In_${i}_0[$ins]"
        )

        case (i, `maxOut`) => Seq(
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${i}_$maxOut[$ins, $outs], ${ns}_In_${i + 1}_$maxOut[$ins, Type, $outs]] with ${ns}_In_${i}_$maxOut[$ins, $outs]"
        )

        case (i, o) => Seq(
          s"type Next[Attr[_, _], Type] = Attr[${ns}_In_${i}_${o + 1}[$ins, $outs, Type], ${ns}_In_${i + 1}_${o + 1}[$ins, Type, $outs, Type]] with ${ns}_In_${i}_${o + 1}[$ins, $outs, Type]",
          s"type Stay[Attr[_, _], Type] = Attr[${ns}_In_${i}_$o[$ins, $outs], ${ns}_In_${i + 1}_$o[$ins, Type, $outs]] with ${ns}_In_${i}_$o[$ins, $outs]"
        )
      }
    }

    val (attrVals: Seq[String], attrVals_ : Seq[String]) = attrs.flatMap {
      case BackRef(_, _, _, _, _, _, _, _, _) => None
      case a                                  => {
        val (attr, attrClean, tpe) = (a.attr, a.attrClean, a.tpe)
        val p1 = padS(maxAttr, attr)
        val p2 = padS(maxAttr, attrClean)
        val p3 = padS(maxTpe, tpe)
        a match {
          case _ if a.baseTpe == "K" => None
          case _                     => Some(
            s"${overr}def $attr  $p1: Next[$attr$p1 , $tpe$p3] = ???",
            s"${overr}def ${attrClean}_ $p2: Stay[$attr$p1 , $tpe$p3] = ???"
          )
        }
      }
    }.unzip

    val (attrValsK: Seq[String], attrValsK_ : Seq[String]) = attrs.flatMap {
      case BackRef(_, _, _, _, _, _, _, _, _) => None
      case a                                  => {
        val (attr, attrClean, tpe) = (a.attr, a.attrClean, a.tpe)
        val p1 = padS(maxAttr, attr)
        val p2 = padS(maxAttr, attrClean)
        val p3 = padS(maxTpeK, tpe)
        a match {
          case _ if a.baseTpe == "K" => Some(
            s"${overr}def $attr  $p1: String => Next[$attr$p1, $tpe$p3    ] = ???",
            s"${overr}def ${attrClean}_ $p2: String => Stay[$attr$p2, $tpe$p3    ] = ???"
          )
          case _                     => None
        }
      }
    }.unzip match {
      case (Nil, Nil) => (Nil, Nil)
      case (s1, s2)   => (Seq("") ++ s1, Seq("") ++ s2)
    }

    val attrValsOpt: Seq[String] = attrs.flatMap {
      case BackRef(_, _, _, _, _, _, _, _, _) => None
      case a                                  => {
        val (attr, attrClean, tpe) = (a.attr, a.attrClean, a.tpe)
        val p2 = padS(maxAttr, attrClean)
        val p3 = padS(maxTpe, tpe)
        a match {
          case _: Val if a.baseTpe == "K" => None
          case _                          => Some(s"${overr}def $attrClean$$ $p2: Next[$attrClean$$$p2, Option[$tpe]$p3] = ???")
        }
      }
    }

    val (maxClazz2: Seq[Int], maxRefNs: Seq[Int]) = attrs.map {
      case Ref(_, _, _, clazz2, _, _, refNs, _, _, _, _) => (clazz2.length, refNs.length)
      case BackRef(_, clazz2, _, _, _, _, backRef, _, _) => (clazz2.length, backRef.length)
      case other                                         => (0, 0)
    }.unzip

    val maxAttrClean: Int = attrs.map(_.attrClean.length).max

    val biDirectionals: Map[String, String] = attrs.flatMap {
      case Ref(attr, _, _, _, _, _, refNs, _, Some("BiSelfRef_"), revRef, _)     => Some(attr -> s" with BiSelfRef_")
      case Ref(attr, _, _, _, _, _, refNs, _, Some("BiOtherRef_"), revRef, _)    => Some(attr -> s" with BiOtherRef_[$domain.$refNs.$revRef[NS, NS]]")
      case Ref(attr, _, _, _, _, _, refNs, _, Some("BiEdgePropRef_"), revRef, _) => Some(attr -> s" with BiEdgePropRef_")
      case Ref(attr, _, _, _, _, _, refNs, _, Some("BiEdgeRef_"), revRef, _)     => Some(attr -> s" with BiEdgeRef_[$domain.$refNs.$revRef[NS, NS]]")
      case Ref(attr, _, _, _, _, _, refNs, _, Some("BiTargetRef_"), revRef, _)   => Some(attr -> s" with BiTargetRef_[$domain.$refNs.$revRef[NS, NS]]")
      case other                                                                 => None
    }.toMap
    val maxBidirectionals: Iterable[Int] = biDirectionals.values.map(_.length)

    def p(i: Int) = if (i < 10) "0" + i else i

    val refCode: Seq[String] = attrs.foldLeft(Seq("")) {
      case (acc, Ref(attr, attrClean, _, clazz2, _, baseType, refNs, opts, _, _, _)) => {
        val p1 = padS(maxAttrClean + 1, attrClean)
        val p2 = padS("ManyRef".length, clazz2)
        val p3 = padS(maxRefNs.max, refNs)

        val biDirectional = if (biDirectionals.nonEmpty && biDirectionals.contains(attr))
          biDirectionals(attr) + padS(maxBidirectionals.max, biDirectionals(attr))
        else ""

        val ref = (in, out, maxIn) match {
          case (0, 0, _) if baseType.isEmpty                => s"${refNs}_0$p3$biDirectional"
          case (0, 0, 3)                                    => s"${refNs}_0$p3$biDirectional with Nested00[${refNs}_1$p3, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3]"
          case (0, 0, 2)                                    => s"${refNs}_0$p3$biDirectional with Nested00[${refNs}_1$p3, ${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, P4]"
          case (0, 0, 1)                                    => s"${refNs}_0$p3$biDirectional with Nested00[${refNs}_1$p3, ${refNs}_In_1_1$p3, P3, P4]"
          case (0, 0, 0)                                    => s"${refNs}_0$p3$biDirectional with Nested00[${refNs}_1$p3, P2, P3, P4]"
          case (0, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_$o$p3[${OutTypes mkString ", "}]$biDirectional"
          case (0, o, 3)                                    => s"${refNs}_$o$p3[${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${OutTypes mkString ", "}]"
          case (0, o, 2)                                    => s"${refNs}_$o$p3[${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, P${o + 4}, ${OutTypes mkString ", "}]"
          case (0, o, 1)                                    => s"${refNs}_$o$p3[${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[${refNs}_${o + 1}$p3, ${refNs}_In_1_${o + 1}$p3, P${o + 3}, P${o + 4}, ${OutTypes mkString ", "}]"
          case (0, o, 0)                                    => s"${refNs}_$o$p3[${OutTypes mkString ", "}]$biDirectional with Nested${p(o)}[${refNs}_${o + 1}$p3, P${o + 2}, P${o + 3}, P${o + 4}, ${OutTypes mkString ", "}]"

          case (1, 0, _) if baseType.isEmpty                => s"${refNs}_In_1_0$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (1, 0, 3)                                    => s"${refNs}_In_1_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
          case (1, 0, 2)                                    => s"${refNs}_In_1_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[${refNs}_In_1_1$p3, ${refNs}_In_2_1$p3, P4, ${InTypes mkString ", "}]"
          case (1, 0, 1)                                    => s"${refNs}_In_1_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_1_00[${refNs}_In_1_1$p3, P3, P4, ${InTypes mkString ", "}]"
          case (1, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_1_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (1, o, 3)                                    => s"${refNs}_In_1_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
          case (1, o, 2)                                    => s"${refNs}_In_1_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[${refNs}_In_1_${o + 1}$p3, ${refNs}_In_2_${o + 1}$p3, P${o + 4}, ${(InTypes ++ OutTypes) mkString ", "}]"
          case (1, o, 1)                                    => s"${refNs}_In_1_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_1_${p(o)}[${refNs}_In_1_${o + 1}$p3, P${o + 3}, P${o + 4}, ${(InTypes ++ OutTypes) mkString ", "}]"

          case (2, 0, _) if baseType.isEmpty                => s"${refNs}_In_2_0$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (2, 0, 3)                                    => s"${refNs}_In_2_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_2_00[${refNs}_In_2_1$p3, ${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
          case (2, 0, 2)                                    => s"${refNs}_In_2_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_2_00[${refNs}_In_2_1$p3, P4, ${InTypes mkString ", "}]"
          case (2, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_2_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (2, o, 3)                                    => s"${refNs}_In_2_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_2_${padI(o)}[${refNs}_In_2_${o + 1}$p3, ${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
          case (2, o, 2)                                    => s"${refNs}_In_2_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_2_${padI(o)}[${refNs}_In_2_${o + 1}$p3, P${o + 4}, ${(InTypes ++ OutTypes) mkString ", "}]"

          case (3, 0, _) if baseType.isEmpty                => s"${refNs}_In_3_0$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (3, 0, 3)                                    => s"${refNs}_In_3_0$p3[${InTypes mkString ", "}]$biDirectional with Nested_In_3_00[${refNs}_In_3_1$p3, ${InTypes mkString ", "}]"
          case (3, o, _) if baseType.isEmpty || o == maxOut => s"${refNs}_In_3_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional"
          case (3, o, 3)                                    => s"${refNs}_In_3_$o$p3[${(InTypes ++ OutTypes) mkString ", "}]$biDirectional with Nested_In_3_${padI(o)}[${refNs}_In_3_${o + 1}$p3, ${(InTypes ++ OutTypes) mkString ", "}]"
        }
        acc :+ s"object ${attrClean.capitalize} $p1 extends $clazz2$p2[$ns, $refNs$p3] with $ref"
      }
      case (acc, BackRef(backNs, _, _, _, _, _, backRefNs, _, _))                    =>
        val p1 = padS(maxAttrClean + 1, backNs)
        val p2 = padS(maxClazz2.max, backRefNs)
        val ref = (in, out) match {
          case (0, 0) => s"${backRefNs}_0$p2"
          case (0, o) => s"${backRefNs}_$o$p2[${OutTypes mkString ", "}]"
          case (i, o) => s"${backRefNs}_In_${i}_$o$p2[${(InTypes ++ OutTypes) mkString ", "}]"
        }
        acc :+ s"object $backNs $p1 extends $ref"
      case (acc, _)                                                                  => acc
    }.distinct

    (in, out) match {
      // First output trait
      case (0, 0) =>
        val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) ("P" + (out + in + 1), "P" + (out + in + 2)) else (s"${ns}_In_1_0", s"${ns}_In_1_1")
        s"""trait ${ns}_0 extends $ns with Out_0[${ns}_0, ${ns}_1, $thisIn, $nextIn] {
           |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |}
         """.stripMargin

      // Last output trait
      case (0, o) if o == maxOut =>
        val thisIn = if (maxIn == 0 || in == maxIn) "P" + (out + in + 1) else s"${ns}_In_1_$o"
        val types = OutTypes mkString ", "
        s"""trait ${ns}_$o[$types] extends $ns with Out_$o[${ns}_$o, P${out + in + 1}, $thisIn, P${out + in + 2}, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |}""".stripMargin

      // Other output traits
      case (0, o) =>
        val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) ("P" + (out + in + 1), "P" + (out + in + 2)) else (s"${ns}_In_1_$o", s"${ns}_In_1_${o + 1}")
        val types = OutTypes mkString ", "
        s"""trait ${ns}_$o[$types] extends $ns with Out_$o[${ns}_$o, ${ns}_${o + 1}, $thisIn, $nextIn, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |
           |  object Self extends ${ns}_$o[$types] with SelfJoin
           |}
         """.stripMargin


      // First input trait
      case (i, 0) =>
        val s = if (in > 1) "s" else ""
        val (thisIn, nextIn) = if (maxIn == 0 || in == maxIn) ("P" + (out + in + 1), "P" + (out + in + 2)) else (s"${ns}_In_${i + 1}_0", s"${ns}_In_${i + 1}_1")
        val types = InTypes mkString ", "
        s"""
           |
           |/********* Input molecules awaiting $i input$s *******************************/
           |
           |trait ${ns}_In_${i}_0[$types] extends $ns with In_${i}_0[${ns}_In_${i}_0, ${ns}_In_${i}_1, $thisIn, $nextIn, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |}
         """.stripMargin

      // Last input trait
      case (i, o) if i <= maxIn && o == maxOut =>
        val thisIn = if (maxIn == 0 || i == maxIn) "P" + (out + in + 1) else s"${ns}_In_${i + 1}_$o"
        val types = (InTypes ++ OutTypes) mkString ", "
        s"""trait ${ns}_In_${i}_$o[$types] extends $ns with In_${i}_$o[${ns}_In_${i}_$o, P${out + in + 1}, $thisIn, P${out + in + 2}, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |}""".stripMargin

      // Max input traits
      case (i, o) if i == maxIn =>
        val types = (InTypes ++ OutTypes) mkString ", "
        s"""trait ${ns}_In_${i}_$o[$types] extends $ns with In_${i}_$o[${ns}_In_${i}_$o, ${ns}_In_${i}_${o + 1}, P${out + in + 1}, P${out + in + 2}, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |}
         """.stripMargin

      // Other input traits
      case (i, o) =>
        val (thisIn, nextIn) = if (i == maxIn) ("P" + (out + in + 1), "P" + (out + in + 2)) else (s"${ns}_In_${i + 1}_$o", s"${ns}_In_${i + 1}_${o + 1}")
        val types = (InTypes ++ OutTypes) mkString ", "
        s"""trait ${ns}_In_${i}_$o[$types] extends $ns with In_${i}_$o[${ns}_In_${i}_$o, ${ns}_In_${i}_${o + 1}, $thisIn, $nextIn, $types] {
           |  ${(nextStay ++ Seq("") ++ attrVals ++ attrValsK ++ Seq("") ++ attrValsOpt ++ Seq("") ++ attrVals_ ++ attrValsK_ ++ refCode).mkString("\n  ").trim}
           |
           |  object Self extends ${ns}_In_${i}_$o[$types] with SelfJoin
           |}
         """.stripMargin
    }
  }

  def nsBodies(namespace: Namespace): (String, Seq[(Int, String)]) = {
    val inArity = d.in
    val outArity = d.out
    val ns = namespace.ns
    val attrs = namespace.attrs
    val ext = namespace.opt match {
      case Some(Edge) => "extends BiEdge_ "
      case _          => ""
    }
    val p1 = (s: String) => padS(attrs.map(_.attr).filter(!_.startsWith("_")).map(_.length).max, s)
    val p2 = (s: String) => padS(attrs.map(_.clazz).filter(!_.startsWith("Back")).map(_.length).max, s)

    def indexedFirst(opts: Seq[Optional]): Seq[String] = {
      val classes = opts.filter(_.clazz.nonEmpty).map(_.clazz)
      if (classes.contains("Indexed"))
        "Indexed" +: classes.filterNot(_ == "Indexed")
      else
        classes
    }

    val attrClasses: String = attrs.flatMap {
      case Val(attr, _, clazz, tpe, baseTpe, datomicTpe, opts, bi, revRef, _) if tpe.take(3) == "Map" =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        Seq(s"class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")

      case Val(attr, _, clazz, tpe, baseTpe, datomicTpe, opts, bi, revRef, _) if baseTpe == "K" =>
        val extensions0 = indexedFirst(opts) ++ bi.toList :+ "MapAttrK"
        val extensions = " with " + extensions0.mkString(" with ")
        Seq(s"class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")

      case Val(attr, _, clazz, _, _, _, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        Seq(s"class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")

      case Enum(attr, _, clazz, _, _, enums, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        val enumValues = s"private lazy val ${enums.mkString(", ")} = EnumValue"
        Seq( s"""class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions { $enumValues }""")

      case Ref(attr, _, clazz, _, _, _, revNs, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ (bi match {
          case Some("BiSelfRef_")     => Seq(s"BiSelfRefAttr_")
          case Some("BiOtherRef_")    => Seq(s"BiOtherRefAttr_[$revNs.$revRef[NS, NS]]")
          case Some("BiEdgeRef_")     => Seq(s"BiEdgeRefAttr_[$revNs.$revRef[NS, NS]]")
          case Some("BiEdgePropAttr") => Seq(s"BiEdgePropAttr_")
          case Some("BiEdgePropRef_") => Seq(s"BiEdgePropRefAttr_")
          case Some("BiTargetRef_")   => Seq(s"BiTargetRefAttr_[$revNs.$revRef[NS, NS]]")
          case other                  => Nil
        })
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")

        Seq(s"class $attr${p1(attr)}[Ns, In] extends $clazz${p2(clazz)}[Ns, In]$extensions")

      case BackRef(backAttr, _, clazz, _, _, _, _, _, _) => Nil
    }.mkString("\n  ").trim


    val attrClassesOpt = attrs.flatMap {
      case Val(attr, attrClean, clazz, tpe, baseTpe, _, opts, bi, revRef, _) if tpe.take(3) == "Map" =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        Seq(s"class $attrClean$$${p1(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions")

      case Val(attr, attrClean, clazz, _, _, _, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        Seq(s"class $attrClean$$${p1(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions")

      case Enum(attr, attrClean, clazz, _, _, enums, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ bi.toList
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")
        val enumValues = s"private lazy val ${enums.mkString(", ")} = EnumValue"
        Seq( s"""class $attrClean$$${p1(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions { $enumValues }""")

      case Ref(attr, attrClean, clazz, _, _, _, revNs, opts, bi, revRef, _) =>
        val extensions0 = indexedFirst(opts) ++ (bi match {
          case Some("BiSelfRef_")     => Seq(s"BiSelfRefAttr_")
          case Some("BiOtherRef_")    => Seq(s"BiOtherRefAttr_[$revNs.$revRef[NS, NS]]")
          case Some("BiEdgeRef_")     => Seq(s"BiEdgeRefAttr_[$revNs.$revRef[NS, NS]]")
          case Some("BiEdgePropAttr") => Seq(s"BiEdgePropAttr_")
          case Some("BiEdgePropRef_") => Seq(s"BiEdgePropRefAttr_")
          case Some("BiTargetRef_")   => Seq(s"BiTargetRefAttr_[$revNs.$revRef[NS, NS]]")
          case other                  => Nil
        })
        val extensions = if (extensions0.isEmpty) "" else " with " + extensions0.mkString(" with ")

        Seq(s"class $attrClean$$${p1(attrClean)}[Ns, In] extends $clazz$$${p2(clazz)}[Ns]$extensions")

      case BackRef(backAttr, _, clazz, _, _, _, _, _, _) => Nil
    }.mkString("\n  ").trim

    def doc(opts: Seq[Optional]): String = opts.collectFirst {
      case Optional(kv, _) if kv.startsWith("""":db/doc"""") => kv.substring(27).init
    }.getOrElse("(doc text...) ")

    val docMethods: String = if (docs) {
      attrs.flatMap {
        case Val(attr, attrClean, _, tpe, _, _, opts, _, _, _) if tpe.take(3) == "Map" =>
          Some(
            s"""
               |  /** ${doc(opts)} [mandatory map attr]*/
               |  def $attr: AnyRef = ???
               |  /** ${doc(opts)} [optional map attr]*/
               |  def $attrClean$$: AnyRef = ???
               |  /** ${doc(opts)} [tacit map attr]*/
               |  def ${attrClean}_ : AnyRef = ???""".stripMargin)

        case Val(attr, attrClean, _, _, baseTpe, _, opts, _, _, _) if baseTpe == "K" =>
          Some(
            s"""
               |  /** ${doc(opts)} [mandatory keyed map attr]*/
               |  def $attr: AnyRef = ???
               |  /** ${doc(opts)} [optional keyed map attr]*/
               |  def $attrClean$$: AnyRef = ???
               |  /** ${doc(opts)} [tacit keyed map attr]*/
               |  def ${attrClean}_ : AnyRef = ???""".stripMargin)

        case Val(attr, attrClean, _, _, baseTpe, _, opts, _, _, _) =>
          val card = if (baseTpe.nonEmpty) "many" else "one"
          Some(
            s"""
               |  /** ${doc(opts)} [mandatory card-$card attr]*/
               |  def $attr: AnyRef = ???
               |  /** ${doc(opts)} [optional card-$card attr]*/
               |  def $attrClean$$: AnyRef = ???
               |  /** ${doc(opts)} [tacit card-$card attr]*/
               |  def ${attrClean}_ : AnyRef = ???""".stripMargin)

        case Enum(attr, attrClean, _, _, baseTpe, enums, opts, _, _, _) =>
          val card = if (baseTpe.nonEmpty) "many" else "one"
          Some(
            s"""
               |  /** ${doc(opts)} [mandatory card-$card enum attr]
               |    * <br><br>
               |    * Enums available:
               |    * <br> - ${enums.mkString("\n    * <br> - ")}*/
               |  def $attr: AnyRef = ???
               |  /** ${doc(opts)} [optional card-$card enum attr]
               |    * <br><br>
               |    * Enums available:
               |    * <br> - ${enums.mkString("\n    * <br> - ")}*/
               |  def $attrClean$$: AnyRef = ???
               |  /** ${doc(opts)} [tacit card-$card enum attr]
               |    * <br><br>
               |    * Enums available:
               |    * <br> - ${enums.mkString("\n    * <br> - ")}*/
               |  def ${attrClean}_ : AnyRef = ???""".stripMargin)

        case Ref(attr, attrClean, _, _, _, baseTpe, _, opts, _, _, _) =>
          val card = if (baseTpe.nonEmpty) "many" else "one"
          Some(
            s"""
               |  /** ${doc(opts)} [mandatory card-$card ref attr]*/
               |  def $attr: AnyRef = ???
               |  /** ${doc(opts)} [optional card-$card ref attr]*/
               |  def $attrClean$$: AnyRef = ???
               |  /** ${doc(opts)} [tacit card-$card ref attr]*/
               |  def ${attrClean}_ : AnyRef = ???""".stripMargin)

        case _ => None
      }.mkString("\n  ", "\n  ", "")
    } else {
      ""
    }

    val nsArities: Map[String, Int] = d.nss.map(ns => ns.ns -> ns.attrs.size).toMap

    val nestedImports: List[String] = if (
      attrs.exists {
        case Ref(_, _, _, _, _, baseTpe, _, _, _, _, _) if baseTpe.nonEmpty => true
        case _                                                              => false
      }
    ) List("Nested", "Nested_In_1", "Nested_In_2", "Nested_In_3").map("molecule.composition." + _ + "._").take(inArity + 1) else Nil

    val extraImports0: Seq[String] = attrs.collect {
      case Val(_, _, _, "Date", _, _, _, _, _, _) => "java.util.Date"
      case Val(_, _, _, "UUID", _, _, _, _, _, _) => "java.util.UUID"
      case Val(_, _, _, "URI", _, _, _, _, _, _)  => "java.net.URI"
    }.distinct ++ nestedImports

    val extraImports: String = if (extraImports0.isEmpty) "" else extraImports0.mkString(s"\nimport ", "\nimport ", "")

    val (inputEids, inputSpace) = if (inArity > 0)
      (s"\n  override def apply(eids: ?)               : ${ns}_In_1_0[Long] = ???", "           ")
    else
      ("", "")

    val nsTraitsOut: String = (0 to outArity).map(nsTrait(d.domain, namespace, 0, _, inArity, outArity, nsArities, docs)).mkString("\n")

    val outFile: String =
      s"""/*
         |* AUTO-GENERATED Molecule DSL boilerplate code for namespace `$ns`
         |*
         |* To change:
         |* 1. edit schema definition file in `${d.pkg}.schema/`
         |* 2. `sbt compile` in terminal
         |* 3. Refresh and re-compile project in IDE
         |*/
         |package ${d.pkg}.dsl
         |package ${firstLow(d.domain)}$extraImports
         |import scala.language.higherKinds
         |import molecule.boilerplate.attributes._
         |import molecule.boilerplate.base._
         |import molecule.boilerplate.dummyTypes._
         |import molecule.boilerplate.out._
         |import molecule.expression.AttrExpressions.?
         |
         |
         |object $ns extends ${ns}_0 with FirstNS {
         |  override def apply(eid: Long, eids: Long*): ${ns}_0 $inputSpace= ???
         |  override def apply(eids: Iterable[Long])  : ${ns}_0 $inputSpace= ???$inputEids
         |}
         |
         |trait $ns {
         |  $attrClasses
         |
         |  $attrClassesOpt$docMethods
         |}
         |
         |$nsTraitsOut""".stripMargin

    val nsTraitsIn: Seq[(Int, String)] = if (inArity == 0) Nil
    else (1 to inArity).map(in =>
      (in, (0 to outArity).map(nsTrait(d.domain, namespace, in, _, inArity, outArity, nsArities, docs)).mkString("\n"))
    )
    val inFiles: Seq[(Int, String)] = nsTraitsIn.map { case (in, inTraits) =>
      val inFile: String =
        s"""/*
           |* AUTO-GENERATED Molecule DSL boilerplate code for namespace `$ns`
           |*
           |* To change:
           |* 1. edit schema definition file in `${d.pkg}.schema/`
           |* 2. `sbt compile` in terminal
           |* 3. Refresh and re-compile project in IDE
           |*/
           |package ${d.pkg}.dsl
           |package ${firstLow(d.domain)}$extraImports
           |import scala.language.higherKinds
           |import molecule.boilerplate.attributes._
           |import molecule.boilerplate.base._
           |import molecule.boilerplate.dummyTypes._
           |import molecule.boilerplate.in1._
           |import molecule.boilerplate.in2._
           |import molecule.boilerplate.in3._
           |
           |$inTraits""".stripMargin

      (in, inFile)
    }

    (outFile, inFiles)
  }
}
