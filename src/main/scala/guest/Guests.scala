package guest

import event.Event
import guest.repository.{GuestRepository, SetGuestRepo}
import util.Version

object GuestServiceFactory{
  def createService(version: Version): Guests =
    version match {
      case Version.SetVersion => Guests(SetGuestRepo(Set.empty))
    }
}

case class Guests(private val repository: GuestRepository) extends CheckGuests {
  def addGuest(guest: Guest): Unit =
    repository.addGuest(guest)

  def getGuests: Set[Guest] = repository.getGuests

  override def byId(id: Int): Option[Guest] =
    repository.byId(id)

  def changeGuest(id: Int, newGuest: Guest): Unit =
    repository.changeGuestById(id, newGuest)

  override def deleteById(id: Int): Boolean =
    repository.deleteById(id)

  override def deleteByUserId(id: Int): Unit =
    getGuests
      .filter(guest => guest.getUserId == id)
      .foreach(guest => deleteById(guest.getId))

  override def deleteByEventId(id: Int): Unit =
    getGuests
      .filter(guest => guest.getEventId == id)
      .foreach(guest => deleteById(guest.getId))

  override def deleteByEvents(deletedEvents: Set[Event]): Unit =
    deletedEvents
      .foreach(event => deleteByEventId(event.getId))
}

