package modules.event.repository

import modules.event.Event

trait EventRepository {
  def addEvent(event: Event): Unit

  def getEvents: Set[Event]

  def byId(id: Int): Option[Event]

  def deleteById(id: Int): Boolean

  def changeEvent(id: Int, newEvent: Event): Unit
}
