package app

import java.sql.DriverManager
import app.Test.getConn
import app.domain.dsl.Accounting.metadb.Accounting_h2
import app.domain.dsl.Bar.metadb.Bar_h2
import app.domain.dsl.Foo.metadb.Foo_h2
import molecule.db.common.api.MetaDb
import molecule.db.common.facade.{JdbcConn_JVM, JdbcHandler_JVM}
import molecule.db.common.marshalling.JdbcProxy
import molecule.db.h2.sync.*
import utest.*

object Test extends TestSuite {

  def getConn(metaDb: MetaDb): JdbcConn_JVM = {
    val url = "jdbc:h2:mem:"
    Class.forName("org.h2.Driver")
    val proxy   = JdbcProxy(url, metaDb)
    val sqlConn = DriverManager.getConnection(url)
    JdbcHandler_JVM.recreateDb(proxy, sqlConn)
  }

  override def tests: Tests = Tests {

    //    "bar" - {
    //      import app.domain.dsl.Bar.*
    //      given JdbcConn_JVM = getConn(Bar_h2())
    //
    //      Person.name("Bob").age(42).save.i.transact
    //      Person.name.age.query.i.get ==> List(("Bob", 42))
    //    }
    //
    //    "foo" - {
    //      import app.domain.dsl.Foo.*
    //      given JdbcConn_JVM = getConn(Foo_h2())
    //
    //      Person.name("Liz").age(38).save.transact
    //      Person.name.age.query.get ==> List(("Liz", 38))
    //    }
    //
    //    "types" - {
    //      import molecule.db.h2.async.*
    //      import molecule.db.common.util.Executor.*
    //
    //      import app.domain.dsl.Bar.*
    //      given JdbcConn_JVM = getConn(Bar_h2())
    //
    //      val chatRoomQuery = Person.name.query
    //      var chatRoomUI    = List.empty[String]
    //      for {
    //        _ <- Person.name("a").save.transact
    //
    //        _ <- chatRoomQuery.subscribe { updatedStrings =>
    //          println("--- " + updatedStrings)
    //          chatRoomUI = updatedStrings
    //        }
    //
    //        _ <- Person.name("b").save.transact
    //        _ <- Person.name("c").save.transact
    //
    //        _ <- chatRoomQuery.unsubscribe()
    //        _ = chatRoomUI ==> List("a", "b", "c")
    //
    //        _ <- Person.name("x").save.transact
    //        _ = chatRoomUI ==> List("a", "b", "c")
    //      } yield ()
    //    }

    "accounting" - {
      import app.domain.dsl.Accounting.*
      given JdbcConn_JVM = getConn(Accounting_h2())

      Invoice.no.Lines.*(InvoiceLine.amount).insert(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      ).transact


      Invoice.no.Lines.*(InvoiceLine.amount).query.get ==> List(
        (1, List(10, 20, 30)),
        (2, List(20, 70)),
      )
      Invoice.no.Lines.amount.query.get ==> List(
        (1, 10),
        (1, 20),
        (1, 30),
        (2, 20),
        (2, 70),
      )

      InvoiceLine.amount.a2.Invoice.no.a1.query.get ==> List(
        (10, 1),
        (20, 1),
        (30, 1),
        (20, 2),
        (70, 2),
      )


      Invoice.no.Lines.?(InvoiceLine.amount).query.get ==> List(
        (1, Some(10)),
        (1, Some(20)),
        (1, Some(30)),
        (2, Some(20)),
        (2, Some(70)),
      )
      InvoiceLine.amount.Invoice.no.query.get ==> List(
        (10, 1),
        (20, 1),
        (20, 2),
        (30, 1),
        (70, 2),
      )
      InvoiceLine.amount.Invoice.?(Invoice.no).query.get ==> List(
        (10, Some(1)),
        (20, Some(1)),
        (20, Some(2)),
        (30, Some(1)),
        (70, Some(2)),
      )


      Invoice.?(Invoice.no).Lines.amount.query.get ==> List(
        (Some(1), 10),
        (Some(1), 20),
        (Some(1), 30),
        (Some(2), 20),
        (Some(2), 70),
      )
      InvoiceLine.?(InvoiceLine.amount).Invoice.no.query.get ==> List(
        (Some(10), 1),
        (Some(20), 1),
        (Some(20), 2),
        (Some(30), 1),
        (Some(70), 2),
      )

      // OBS: we can't group by random/multiple values, only by id,
      // and that would always reference a single entity which makes nested obsolete
      // - use flat instead!

      //// Nested retrieval, line-to-invoices
      //// "Line amount with list of invoice numbers"
      //InvoiceLine.amount.Invoice.*(Invoice.no).query.get ==> List(
      //  (10, List(1)),
      //  (20, List(1, 2)),
      //  (30, List(1)),
      //  (70, List(2)),
      //)

    }
  }
}

/*
Invoice_refs_1:
  trait NestedInit { self: Molecule =>
    def * [NestedT](nested: Molecule_1[NestedT]) = new Invoice_n[(T, Seq[NestedT])](addNested(self, nested))
    def *?[NestedT](nested: Molecule_1[NestedT]) = new Invoice_n[(T, Seq[NestedT])](addOptNested(self, nested))

    def * [NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new Invoice_n[(T, Seq[NestedTpl])](addNested(self, nested))
    def *?[NestedTpl <: Tuple](nested: Molecule_n[NestedTpl]) = new Invoice_n[(T, Seq[NestedTpl])](addOptNested(self, nested))
  }

  trait OptRefInit { self: Molecule =>
    def ?[OptRefT](optRef: Molecule_1[OptRefT]) = new InvoiceLine_n[(T, Option[OptRefT])](addOptRef(self, optRef))
    def ?[OptRefTpl <: Tuple](optRef: Molecule_n[OptRefTpl]) = new InvoiceLine_n[(T, Option[OptRefTpl])](addOptRef(self, optRef))
  }

  object Lines extends InvoiceLine_1[T](dataModel.add(_dm.Ref("Invoice", "lines", "InvoiceLine", OneToMany, false, List(0, 2, 1))))
    with NestedInit with OptRefInit



class InvoiceLine_refs_n[Tpl <: Tuple](override val dataModel: DataModel) extends Molecule_n[Tpl] {
  object _Invoice extends Invoice_n[Tpl](dataModel.add(_dm.BackRef("Invoice", "InvoiceLine", List(0, 1))))

  trait OptRefInit { self: Molecule =>
    def ?[OptRefT](optRef: Molecule_1[OptRefT]) = new InvoiceLine_n[Tpl :* Option[OptRefT]](addOptRef(self, optRef))
    def ?[OptRefTpl <: Tuple](optRef: Molecule_n[OptRefTpl]) = new InvoiceLine_n[Tpl :* Option[OptRefTpl]](addOptRef(self, optRef))
  }

  object Invoice extends Invoice_n[Tpl](dataModel.add(_dm.Ref("InvoiceLine", "invoice", "Invoice", ManyToOne, true, List(1, 3, 0)))) with OptRefInit
}
*/



