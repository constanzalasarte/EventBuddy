package guest

import event.{CheckEvents, Event}
import guest.repository.{GuestRepository, SetGuestRepo}
import user.CheckUsers
import util.{Created, Error, Ok, Result, Version}
import util.exceptions.IDNotFoundException

object GuestServiceFactory{
  def createService(version: Version, userService: CheckUsers, eventService: CheckEvents): Guests =
    version match {
      case Version.SetVersion => Guests(SetGuestRepo(Set.empty), userService, eventService)
    }
}

case class Guests(private val repository: GuestRepository, private val userService: CheckUsers, private val eventService: CheckEvents) extends CheckGuests {
  def addGuest(request: GuestRequest): Result[Guest] = {
    if(userNotExists(request.userId))
      Error(IDNotFoundException("user", request.userId))
    if (eventNotExists(request.eventId))
      Error(IDNotFoundException("event", request.eventId))
    val guest = request.getGuest
    repository.addGuest(guest)
    Created(guest)
  }

  def getGuests: Set[Guest] = repository.getGuests

  override def byId(id: Int): Option[Guest] =
    repository.byId(id)

  def changeGuest(id: Int, patch: GuestPatchRequest): Result[Guest] = {
    val maybeGuest: Option[Guest] = checkGuest(id)
    if(maybeGuest.isEmpty) Error(IDNotFoundException("guest", id))
    else if (patch.hasUserId && userNotExists(patch.userId.get)) Error(IDNotFoundException("user", patch.userId.get))
    else if(patch.hasEventId && eventNotExists(patch.eventId.get)) Error(IDNotFoundException("event", patch.eventId.get))
    else {
      val guest = updateGuest(patch, maybeGuest)
      repository.changeGuestById(id, guest)
      Ok(guest)
    }
  }

  private def eventNotExists(id: Int) =
    eventService.byId(id).isEmpty

  private def updateGuest(guestPatch: GuestPatchRequest, maybeGuest: Option[Guest]) = {
    val guest = maybeGuest.get
    if (guestPatch.hasUserId) guest.changeUserId(guestPatch.userId.get)
    if (guestPatch.hasEventId) guest.changeEventId(guestPatch.eventId.get)
    if (guestPatch.hasConfirmationStatus) guest.changeConfirmationStatus(guestPatch.confirmationStatus.get)
    if (guestPatch.hasIsHost) guest.changeIsHost(guestPatch.isHost.get)
    guest
  }

  private def checkGuest(id: Int) : Option[Guest] =
    repository.byId(id)

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

  private def userNotExists(userId: Int): Boolean = {
    userService.byID(userId).isEmpty
  }
}

