package event

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, extractRequest, get, onSuccess, parameters, path, pathEnd, post}
import org.apache.pekko.http.scaladsl.server.Route
import user.CheckUsers

import scala.concurrent.ExecutionContext

case class EventRoutes(events: Events, users: CheckUsers) extends EventJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private def createEvent(eventRequest: EventRequest) = {
    if(!userExist(eventRequest.getCreatorId)) {
      complete(StatusCodes.NotFound, s"There is no user with id ${eventRequest.getCreatorId}")
    }
    else{
      val event = eventRequest.getEvent
      events.addEvent(event)
      complete(StatusCodes.Created, event)
    }
  }

  private def userExist(id: Int) = users.byID(id).isDefined

  def eventRoute: Route =
    concat(
      pathEnd{
        concat(
          get {
            complete(StatusCodes.OK, events.getEvents)
          },
          post {
            entity(as[EventRequest]) { eventRequest =>
              createEvent(eventRequest)
            }
          },
        )
      },
      path("byId") {
        getByIdRoute
      },
    )

  private def getByIdRoute = {
    concat(
      get {
        parameters("id") { id => {
          getEventById(id)
        }
        }
      },
      get{
        complete("/event/byId")
      },
      delete {
        parameters("id") { id => {
          deleteEvent(id)
        }
        }
      }
    )
  }

  private def getEventById(id: String) = {
    try {
      val event: Option[Event] = checkEvent(id.toInt)
      if (event.isEmpty) complete(StatusCodes.NotFound, s"There is no event with id ${id.toInt}")
      else
        complete(StatusCodes.OK, event.get)
    }
    catch {
      case _: NumberFormatException =>
        complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
    }
  }

  private def deleteEvent(id: String) = {
    try {
      val deleted: Boolean = events.deleteById(id.toInt)
      if (!deleted) complete(StatusCodes.NotFound, s"There is no event with id ${id.toInt}")
      else complete(StatusCodes.OK, s"Event deleted")
    }
    catch {
      case _: NumberFormatException =>
        complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
    }
  }

  private def checkEvent(id: Int): Option[Event] =
    events.byId(id)
}
