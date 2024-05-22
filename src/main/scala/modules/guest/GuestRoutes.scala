package modules.guest

import modules.event.CheckEvents
import modules.user.CheckUsers
import org.apache.pekko.http.scaladsl.model.{StatusCode, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, parameters, path, pathEnd, post, put}
import util.exceptions.IDNotFoundException

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
      val futureDeleted = guests.deleteById(id.toInt)
      onComplete(futureDeleted) {
        case Success(deleted) => {
          if (!deleted) IDNotFoundResponse("guest", id.toInt)
          else complete(StatusCodes.OK, s"Guest deleted")
        }
        case Failure(_) => {
          internalServerError
        }
      }
    }
    catch {
      case _: NumberFormatException => {
        intExpectedResponse
      }
    }
  }

  private def updateGuestById(id: String, guestPatch: GuestPatchRequest): Route = {
    try{
      val future = guests.changeGuest(id.toInt, guestPatch)
      getResponse(future, StatusCodes.OK)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
      case msg: IDNotFoundException =>
        complete(StatusCodes.NotFound, msg.getMessage)
    }
  }

  private def getGuestById(id: String): Route = {
    try {
      val eventualMaybeGuest = guests.byId(id.toInt)
      onComplete(eventualMaybeGuest) {
        case Success(maybeGuest) =>
          if (maybeGuest.isEmpty) IDNotFoundResponse("guest", id.toInt)
          else complete(StatusCodes.OK, maybeGuest.get)
        case Failure(_) => internalServerError
      }
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def createGuest(guestRequest: GuestRequest): Route = {
    val eventualGuest = guests.addGuest(guestRequest)
    getResponse(eventualGuest, StatusCodes.Created)
  }

  private def getResponse(eventualGuest: Future[Guest], statusCode: StatusCode): Route = {
    onComplete(eventualGuest) {
      case Success(guest) => complete(statusCode, guest)
      case Failure(exception) => exception match {
        case e: IDNotFoundException => complete(StatusCodes.NotFound, e.getMessage)
      }
    }
  }

  private def internalServerError =
    complete(StatusCodes.InternalServerError, "")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")
}
