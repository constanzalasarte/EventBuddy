package modules.guest

import modules.event.{CheckEvents, Event}
import modules.guest.repository.{GuestDBRepo, GuestRepository}
import modules.user.{CheckUsers, User}
import server.Server.executionContext
import slick.jdbc.JdbcBackend.Database
import util.Version
import util.Version.DBVersion
import util.exceptions.IDNotFoundException

import scala.concurrent.Future

object GuestServiceFactory{
  def createService(version: Version, userService: CheckUsers, eventService: CheckEvents, db: Option[Database] = None): Guests =
    version match {
      case DBVersion => Guests(GuestDBRepo(db.get), userService, eventService)
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

  def getGuests: Future[Set[Guest]] = repository.getGuests

  override def byId(id: Int): Future[Option[Guest]] =
    repository.byId(id)

  def changeGuest(id: Int, patch: GuestPatchRequest): Future[Guest] = {
    for{
      _ <- checkPatchValues(patch)
      maybeGuest: Option[Guest] <- checkGuest(id)
    } yield{
      if(maybeGuest.isEmpty) throw IDNotFoundException("guest", id)
      else {
        val guest = updateGuest(patch, maybeGuest)
        repository.changeGuestById(id, guest)
        guest
      }
    }
  }

  private def checkPatchValues(patch: GuestPatchRequest): Future[Unit] = {
    for{
      _ <- checkEvent(patch)
      _ <- checkUser(patch)
    } yield {}
  }

  private def checkUser(patch: GuestPatchRequest): Future[Unit] = {
    if (patch.hasUserId) {
      for {
        userNotExist <- userNotExists(patch.userId.get)
      } yield {
        if (userNotExist) throw IDNotFoundException("user", patch.userId.get)
      }
    } else Future{}
  }

  private def checkEvent(patch: GuestPatchRequest): Future[Unit] = {
    if (patch.hasEventId)
      for{
        eventNotExists <- eventNotExists(patch.eventId.get)
      } yield{
        if(eventNotExists)
          throw IDNotFoundException("event", patch.eventId.get)
      }
    else Future{}
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

  private def checkGuest(id: Int) : Future[Option[Guest]] =
    repository.byId(id)

  override def deleteById(id: Int): Future[Boolean] =
    repository.deleteById(id)

  override def deleteByUserId(id: Int): Future[Unit] = {
    for{
      guests <- getGuests
    } yield{
      guests
        .filter(guest => guest.getUserId == id)
        .foreach(guest => deleteById(guest.getId))
    }
  }

  override def deleteByEventId(id: Int): Future[Unit] =
    for{
      guests <- getGuests
    } yield{
      guests
        .filter(guest => guest.getEventId == id)
        .foreach(guest => deleteById(guest.getId))
    }

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

