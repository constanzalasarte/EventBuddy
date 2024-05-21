package modules.event.repository

import modules.event.Event

import scala.concurrent.Future
import server.Server.executionContext

case class EventSetRepo(private var events: Set[Event]) extends EventRepository {

  override def addEvent(event: Event): Future[Unit] = {
    events = events + event
    Future{}
  }

  override def getEvents: Future[Set[Event]] = Future {
    events
  }

  override def byId(id: Int): Future[Option[Event]] = Future {
    events.find(event => event.getId == id)
  }

  override def deleteById(id: Int): Future[Boolean] = {
    val maybeEvent = events.find(_.getId == id)
    maybeEvent.foreach { found =>
      events = events - found
    }
    Future{ maybeEvent.isDefined }
  }

  override def changeEvent(id: Int, newEvent: Event): Future[Unit] = {
    var result: Set[Event]= Set.empty
    for(event <- events) {
      if(event.getId == id) result = result + newEvent
      else result = result + event
    }
    events = result
    Future{}
  }
}
