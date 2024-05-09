package guest

import event.CheckEvents
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import user.CheckUsers
import util.{Created, Error, Ok, Result}
import util.exceptions.IDNotFoundException

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
          },
          delete{
            parameters("id"){ id =>
              deleteGuestById(id)
            }
          },
        )
      }
    )

  private def deleteGuestById(id: String): Route = {
    try{
      val deleted: Boolean = guests.deleteById(id.toInt)
      if (!deleted) return IDNotFoundResponse("guest", id.toInt)
      complete(StatusCodes.OK, s"Guest deleted")
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def updateGuestById(id: String, guestPatch: GuestPatchRequest): Route = {
    try{
      val result = guests.changeGuest(id.toInt, guestPatch)
      result match {
        case Ok(okResult) => complete(result.getStatusCode, okResult)
        case Error(error) => complete(result.getStatusCode, error.getMessage)
      }
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
      case msg: IDNotFoundException =>
        complete(StatusCodes.NotFound, msg.getMessage)
    }
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
    try {
      else {
        val result: Result[Guest] = guests.addGuest(guestRequest)
        getResponse(result)
      }
    }
    catch{
      case msg: IDNotFoundException => complete(StatusCodes.NotFound, msg.getMessage)
    }
  }

  private def getResponse(result: Result[Guest]) = {
    result match {
      case Error(error) => complete(result.getStatusCode, error.getMessage)
      case Ok(ok) => complete(result.getStatusCode, ok)
      case Created(created) => complete(result.getStatusCode, created)
    }
  }

  private def eventNotExists(eventId: Int): Boolean = {
    events.byId(eventId).isEmpty
  }

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")
}
