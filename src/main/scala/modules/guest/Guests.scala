package modules.guest

import modules.event.{CheckEvents, Event}
import modules.guest.repository.{GuestRepository, SetGuestRepo}
import modules.user.{CheckUsers, User}
import server.Server.executionContext
import util.Version
import util.exceptions.IDNotFoundException

import scala.concurrent.Future

object GuestServiceFactory{
  def createService(version: Version, userService: CheckUsers, eventService: CheckEvents): Guests =
    version match {
      case Version.SetVersion => Guests(SetGuestRepo(Set.empty), userService, eventService)
    }
}

case class Guests(private val repository: GuestRepository, private val userService: CheckUsers, private val eventService: CheckEvents) extends CheckGuests {
  def addGuest(request: GuestRequest): Future[Guest] =
    for{
      userNotExist <- userNotExists(request.userId)
      eventNotExist <- eventNotExists(request.eventId)
    } yield {
      checkReqValues(request, userNotExist, eventNotExist)
      val guest = request.getGuest
      repository.addGuest(guest)
      guest
    }

  def getGuests: Set[Guest] = repository.getGuests

  override def byId(id: Int): Option[Guest] =
    repository.byId(id)

  def changeGuest(id: Int, patch: GuestPatchRequest): Future[Guest] = {
    for{
      _ <- checkPatchValues(patch)
    } yield{
      val maybeGuest: Option[Guest] = checkGuest(id)
      if(maybeGuest.isEmpty) throw IDNotFoundException("guest", id)
      else {
        val guest = updateGuest(patch, maybeGuest)
        repository.changeGuestById(id, guest)
        guest
      }
    }
  }

  private def checkPatchValues(patch: GuestPatchRequest): Future[Unit] = {
    if(patch.hasEventId)
      for{
      _ <- checkEvent(patch)
      } yield {}
    if (patch.hasUserId) {
      for{
        _ <- checkUser(patch)
      } yield {}
    }
    Future{}
  }

  private def checkUser(patch: GuestPatchRequest): Future[Unit] = {
    for {
      userNotExist <- userNotExists(patch.userId.get)
    } yield {
      if (userNotExist) throw IDNotFoundException("user", patch.userId.get)
    }
  }

  private def checkEvent(patch: GuestPatchRequest): Future[Unit] = {
    for{
      eventNotExists <- eventNotExists(patch.eventId.get)
    } yield{
      if(eventNotExists)
        throw IDNotFoundException("event", patch.eventId.get)
    }
  }

  private def eventNotExists(id: Int): Future[Boolean] = {
    for {
      event <- eventService.byId(id)
    } yield {
      event.isEmpty
    }
  }

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

  private def userNotExists(userId: Int): Future[Boolean] = {
    for{
      user: Option[User] <- userService.byID(userId)
    } yield {
      user.isEmpty
    }
  }
  private def checkReqValues(request: GuestRequest, userNotExist: Boolean, eventNotExist: Boolean): Unit = {
    if (userNotExist)
      throw IDNotFoundException("user", request.userId)
    if (eventNotExist)
      throw IDNotFoundException("event", request.eventId)
  }
}

