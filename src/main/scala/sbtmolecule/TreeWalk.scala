package sbtmolecule

import scala.meta.Tree

/** Depth-first, preorder traversal utilities for scalameta trees.
  * Replaces scalameta's Traverser/Tree.traverse/Tree.collect for the Scala 3 build: those were
  * macro-based (TraverserMacros) and are absent from every published scalameta _3 artifact; only
  * Tree.children remains. Match-then-recurse is set-equivalent to the old match-xor-recurse
  * Traverser: every node these call sites match (Term.Name, Term.Select(Name, Name)) contains
  * only unmatchable leaf children. */
object TreeWalk {
  def foreach(t: Tree)(pf: PartialFunction[Tree, Unit]): Unit = {
    pf.applyOrElse(t, (_: Tree) => ())
    t.children.foreach(foreach(_)(pf))
  }

  def collect[A](t: Tree)(pf: PartialFunction[Tree, A]): List[A] =
    pf.andThen(a => List(a)).applyOrElse(t, (_: Tree) => Nil) ::: t.children.flatMap(collect(_)(pf))
}
