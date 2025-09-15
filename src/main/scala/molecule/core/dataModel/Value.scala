package molecule.core.dataModel

sealed trait Value {
  def _marker: String
  def _tpe: String
}

trait OneValue extends Value {
  override def _marker = "One"
  override def _tpe = ""
}
case object OneValue extends OneValue

trait SetValue extends Value {
  override def _marker = "Set"
  override def _tpe = "Set"
}
case object SetValue extends SetValue

trait SeqValue extends Value {
  override def _marker = "Seq"
  override def _tpe = "Seq"
}
case object SeqValue extends SeqValue

trait MapValue extends Value {
  override def _marker = "Map"
  override def _tpe = "Map"
}
case object MapValue extends MapValue
