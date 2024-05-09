package event

import event.repository.{EventRepository, EventSetRepo}
import util.Version

object EventServiceFactory{
  def createService(version: Version): Events =
    version match {
      case Version.SetVersion => Events(EventSetRepo(Set.empty))
    }
}

case class Events(private var repository: EventRepository) extends CheckEvents {
  def addEvent(event: Event): Unit =
    repository.addEvent(event)

  def getEvents: Set[Event] = repository.getEvents

  override def byId(id: Int): Option[Event] = {
    repository.byId(id)
  }

  override def deleteById(id: Int): Boolean = {
    repository.deleteById(id)
  }

  override def byCreatorId(id: Int): Set[Event] =
    getEvents.filter(event => event.getCreatorId == id)

  override def deleteByCreatorId(id: Int): Set[Event] = {
    val events = getEvents
      .filter(event => event.getCreatorId == id)
    events.foreach(event => deleteById(event.getId))
    events
  }

  def changeEvent(id: Int, newEvent: Event): Unit =
    repository.changeEvent(id, newEvent)
}