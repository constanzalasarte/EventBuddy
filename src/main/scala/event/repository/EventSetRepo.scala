package event.repository

import event.Event

case class EventSetRepo(private var events: Set[Event]) extends EventRepository {

  override def addEvent(event: Event): Unit =
    events = events + event

  override def getEvents: Set[Event] = events

  override def byId(id: Int): Option[Event] =
    events.find(event => event.getId == id)

  override def deleteById(id: Int): Boolean = {
    val maybeEvent = events.find(_.getId == id)
    maybeEvent.foreach { found =>
      events = events - found
    }
    maybeEvent.isDefined
  }

  override def changeEvent(id: Int, newEvent: Event): Unit = {
    var result: Set[Event]= Set.empty
    for(event <- events) {
      if(event.getId == id) result = result + newEvent
      else result = result + event
    }
    events = result
  }
}
