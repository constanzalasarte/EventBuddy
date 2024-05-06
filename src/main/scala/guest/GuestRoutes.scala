package guest

import event.CheckEvents
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, entity, get, parameters, path, pathEnd, post, put}
import user.CheckUsers

case class GuestRoutes(guests: Guests, events: CheckEvents, users: CheckUsers) extends GuestJsonProtocol {
  def guestRoute: Route =
    concat(
      pathEnd{
        concat(
          get{
            complete(StatusCodes.OK, guests.getGuests)
          },
          post{
            entity(as[GuestRequest]){ guestRequest =>
              createGuest(guestRequest)
            }
          }
        )
      },
      path("byId"){
        concat(
          get{
            parameters("id"){ id =>
              getGuestById(id)
            }
          },
          put{
            parameters("id"){ id =>
              entity(as[GuestPatchRequest]){ guestPatchRequest =>
                updateGuestById(id, guestPatchRequest)
              }
            }
          }
        )
      }
    )

  private def updateGuestById(id: String, guestPatch: GuestPatchRequest): Route = {
    try{
      val maybeGuest: Option[Guest] = checkGuest(id.toInt)
      if(maybeGuest.isEmpty) return IDNotFoundResponse("guest", id.toInt)
      else if(guestPatch.hasUserId) {
        if(userNotExists(guestPatch.userId.get)) return IDNotFoundResponse("user", guestPatch.userId.get)
      }
      else if(guestPatch.hasEventId)
        if(eventNotExists(guestPatch.eventId.get)) return IDNotFoundResponse("event", guestPatch.eventId.get)
      val guest: Guest = updateGuest(guestPatch, maybeGuest)
      guests.changeGuest(id.toInt, guest)
      complete(StatusCodes.OK, guest)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
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

  private def getGuestById(id: String) = {
    try{
      val guest: Option[Guest] = checkGuest(id.toInt)
      if(guest.isEmpty) IDNotFoundResponse("guest", id.toInt)
      else complete(StatusCodes.OK, guest.get)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def checkGuest(id: Int) = {
    guests.byId(id)
  }

  private def createGuest(guestRequest: GuestRequest) = {
   if(userNotExists(guestRequest.userId))
     IDNotFoundResponse("user", guestRequest.userId)
   else if(eventNotExists(guestRequest.eventId))
     IDNotFoundResponse("event", guestRequest.eventId)
   else{
     val guest = guestRequest.getGuest
     guests.addGuest(guest)
     complete(StatusCodes.Created, guest)
   }
  }

  private def userNotExists(userId: Int): Boolean = {
    users.byID(userId).isEmpty
  }
  private def eventNotExists(eventId: Int): Boolean = {
    events.byId(eventId).isEmpty
  }

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")
}
