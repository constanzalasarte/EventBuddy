package modules.guest

import modules.event.Event

trait CheckGuests {
  def byId(id: Int): Option[Guest]
  def deleteById(id: Int): Boolean
  def deleteByUserId(id: Int): Unit
  def deleteByEventId(id: Int): Unit
  def deleteByEvents(deletedEvents: Set[Event]): Unit
}
