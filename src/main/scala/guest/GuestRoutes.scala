package guest

import event.CheckEvents
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, entity, get, pathEnd, post}
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
      }
    )

  private def createGuest(guestRequest: GuestRequest) = {
   if(userNotExists(guestRequest.userId))
     complete(StatusCodes.NotFound, s"There is no user with id ${guestRequest.userId}")
   if(eventNotExists(guestRequest.eventId))
     complete(StatusCodes.NotFound, s"There is no event with id ${guestRequest.eventId}")
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
}
