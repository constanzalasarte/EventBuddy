package modules.guest.repository

import modules.guest.Guest
import server.Server.executionContext

import scala.concurrent.Future

case class SetGuestRepo(private var guests: Set[Guest]) extends GuestRepository {

  override def addGuest(guest: Guest): Future[Unit] = {
    guests = guests + guest
    Future {}
  }

  override def getGuests: Future[Set[Guest]] = Future {
    guests
  }

  override def changeGuestById(id: Int, newGuest: Guest): Future[Unit] = {
    var result: Set[Guest]= Set.empty
    for (guest <- guests) {
      if(guest.getId == id) result = result + newGuest
      else result = result + guest
    }
    guests = result
    Future {}
  }

  override def byId(id: Int): Future[Option[Guest]] = Future {
    guests.find(guest => guest.getId == id)
  }

  override def deleteById(id: Int): Future[Boolean] = {
    val maybeGuest = guests.find(_.getId == id)
    maybeGuest.foreach { found =>
      guests = guests - found
    }
    Future{maybeGuest.isDefined}
  }
}
