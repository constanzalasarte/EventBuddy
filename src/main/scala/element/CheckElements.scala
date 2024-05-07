package element

import event.Event

trait CheckElements {
  def byId(id: Int): Option[Element]
//  def byCreatorId(id: Int): Set[Event]
  def deleteById(id: Int): Boolean
//  def deleteByCreatorId(id: Int): Set[Event]
}
