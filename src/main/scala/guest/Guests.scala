package guest

import event.Event

case class Guests(private var guests: Set[Guest]) extends CheckGuests {
  def addGuest(guest: Guest): Unit =
    guests = guests + guest

  def getGuests: Set[Guest] = guests

  override def byId(id: Int): Option[Guest] = {
    for (guest <- guests) {
      if(guest.getId == id) return Some(guest)
    }
    None
  }

  def changeGuest(id: Int, newGuest: Guest): Unit = {
    var result: Set[Guest]= Set.empty
    for (guest <- guests) {
      if(guest.getId == id) result = result + newGuest
      else result = result + guest
    }
    guests = result
  }

  override def deleteById(id: Int): Boolean = {
    var result: Set[Guest]= Set.empty
    var foundEvent: Boolean = false
    for (guest <- guests) {
      if(guest.getId == id) foundEvent = true
      else result = result + guest
    }
    if(foundEvent) guests = result
    foundEvent
  }

  override def deleteByUserId(id: Int): Unit = {
    var result: Set[Guest]= Set.empty
    for (guest <- guests) {
      if(guest.getUserId != id) result = result + guest
    }
    guests = result
  }

  override def deleteByEventId(id: Int): Unit ={
    var result: Set[Guest]= Set.empty
    for (guest <- guests) {
      if(guest.getEventId != id) result = result + guest
    }
    guests = result
  }

  override def deleteByEvents(deletedEvents: Set[Event]): Unit = {
    for (event <- deletedEvents) {
      deleteById(event.getId)
    }
  }
}

