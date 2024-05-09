package guest.repository
import guest.Guest

case class SetGuestRepo(private var guests: Set[Guest]) extends GuestRepository {

  override def addGuest(guest: Guest): Unit = {
    guests = guests + guest
  }

  override def getGuests: Set[Guest] = guests

  override def changeGuestById(id: Int, newGuest: Guest): Unit = {
    var result: Set[Guest]= Set.empty
    for (guest <- guests) {
      if(guest.getId == id) result = result + newGuest
      else result = result + guest
    }
    guests = result
  }

  override def byId(id: Int): Option[Guest] =
    guests.find(guest => guest.getId == id)

  override def deleteById(id: Int): Boolean = {
    val maybeGuest = guests.find(_.getId == id)
    maybeGuest.foreach { found =>
      guests = guests - found
    }
    maybeGuest.isDefined
  }
}
