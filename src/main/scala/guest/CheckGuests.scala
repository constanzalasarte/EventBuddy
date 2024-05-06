package guest

trait CheckGuests {
  def byId(id: Int): Option[Guest]
//  def byUserId(id: Int): Set[Guest]
//  def byEventId(id: Int): Set[Guest]
  def deleteById(id: Int): Boolean
//  def deleteByUserId(id: Int): Unit
//  def deleteByEventId(id: Int): Unit
}
