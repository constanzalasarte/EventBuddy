package event

trait CheckEvents {
  def byId(id: Int): Option[Event]
  def deleteById(id: Int): Boolean
}
