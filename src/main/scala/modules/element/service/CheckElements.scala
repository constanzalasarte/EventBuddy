package modules.element.service

import modules.element.controller.Element
import modules.event.Event

import scala.concurrent.Future

trait CheckElements {
  def byId(id: Int): Future[Option[Element]]
  def deleteById(id: Int): Future[Unit]
  def deleteByEventId(id: Int): Future[Unit]
  def deleteUserInUsers(id: Int): Future[Unit]
  def deleteInEvents(deletedEvents: Set[Event]): Future[Unit]
}
