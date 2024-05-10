package modules.event

trait CheckEvents {
  def byId(id: Int): Option[Event]
  def byCreatorId(id: Int): Set[Event]
  def deleteById(id: Int): Boolean
  def deleteByCreatorId(id: Int): Set[Event]
}
