package modules.guest

import modules.event.Event

import scala.concurrent.Future

trait CheckGuests {
  def byId(id: Int): Future[Option[Guest]]
  def deleteById(id: Int): Future[Boolean]
  def deleteByUserId(id: Int): Future[Unit]
  def deleteByEventId(id: Int): Future[Unit]
  def deleteByEvents(deletedEvents: Set[Event]): Unit
}
