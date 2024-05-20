package modules.guest

import modules.event.CheckEvents
import modules.user.CheckUsers
import org.apache.pekko.http.scaladsl.model.StatusCodes
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
      val future = guests.changeGuest(id.toInt, guestPatch)
      getResponse(future)
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
      val guest: Option[Guest] = guests.byId(id.toInt)
      if(guest.isEmpty) IDNotFoundResponse("guest", id.toInt)
      else complete(StatusCodes.OK, guest.get)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def createGuest(guestRequest: GuestRequest): Route = {
    val eventualGuest = guests.addGuest(guestRequest)
    getResponse(eventualGuest)
  }

  private def getResponse(eventualGuest: Future[Guest]) = {
    onComplete(eventualGuest) {
      case Success(guest) => complete(StatusCodes.OK, guest)
      case Failure(exception) => exception match {
        case e: IDNotFoundException => complete(StatusCodes.NotFound, e.getMessage)
      }
    }
  }

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")
}
