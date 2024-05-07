package element

import event.Event

trait CheckElements {
  def byId(id: Int): Option[Element]
  def deleteById(id: Int): Boolean
  def deleteByEventId(id: Int): Unit
  def deleteUserInUsers(id: Int): Unit
}
