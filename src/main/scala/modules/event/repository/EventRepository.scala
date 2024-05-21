package modules.event.repository

import modules.event.Event

import scala.concurrent.Future

trait EventRepository {
  def addEvent(event: Event): Future[Unit]

  def getEvents: Future[Set[Event]]

  def byId(id: Int): Future[Option[Event]]

  def deleteById(id: Int): Future[Boolean]

  def changeEvent(id: Int, newEvent: Event): Future[Unit]
}
