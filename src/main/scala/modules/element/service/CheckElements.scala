package modules.element.service

import modules.element.controller.Element
import modules.event.Event

trait CheckElements {
  def byId(id: Int): Option[Element]
  def deleteById(id: Int): Boolean
  def deleteByEventId(id: Int): Unit
  def deleteUserInUsers(id: Int): Unit
  def deleteInEvents(deletedEvents: Set[Event]): Unit
}
