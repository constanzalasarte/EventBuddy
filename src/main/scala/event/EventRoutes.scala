package event

import element.service.CheckElements
import guest.CheckGuests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import user.CheckUsers

import scala.concurrent.ExecutionContext

case class EventRoutes(events: Events, users: CheckUsers, guests: CheckGuests, elements: CheckElements) extends EventJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

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
      },
      put{
        parameters("id"){id => {
          entity(as[EventPatchRequest]){eventPatch =>
            updateEvent(id, eventPatch)
          }
        }}
      }
    )
  }

  private def updateEvent(id: String, eventPatch: EventPatchRequest): Route = {
    try {
      val optEvent: Option[Event] = checkEvent(id.toInt)
      if (optEvent.isEmpty) return IDNotFoundResponse("event", id.toInt)
      else if(eventPatch.hasCreatorId){
        if(!userExist(eventPatch.creatorId.get)) return IDNotFoundResponse("user", eventPatch.creatorId.get)
      }
      val event = updateEventVariables(eventPatch, optEvent)
      events.changeEvent(id.toInt, event)
      complete(StatusCodes.OK, event)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def updateEventVariables(eventPatch: EventPatchRequest, optEvent: Option[Event]): Event = {
    val event = optEvent.get
    if (eventPatch.hasName) event.changeName(eventPatch.name.get)
    if (eventPatch.hasDescription) event.changeDescription(eventPatch.description.get)
    if (eventPatch.hasCreatorId) event.changeCreatorId(eventPatch.creatorId.get)
    if (eventPatch.hasDate) event.changeDate(eventPatch.date.get)
    event
  }

  private def getEventById(id: String): Route = {
    try {
      val event: Option[Event] = checkEvent(id.toInt)
      if (event.isEmpty) return IDNotFoundResponse("event", id.toInt)
      complete(StatusCodes.OK, event.get)
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def deleteEvent(id: String): Route = {
    try {
      val deleted: Boolean = events.deleteById(id.toInt)
      if (!deleted) return IDNotFoundResponse("event", id.toInt)
      guests.deleteByEventId(id.toInt)
      elements.deleteByEventId(id.toInt)
      complete(StatusCodes.OK, s"Event deleted")
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def checkEvent(id: Int): Option[Event] =
    events.byId(id)

  private def createEvent(eventRequest: EventRequest): Route = {
    if(!userExist(eventRequest.getCreatorId)) {
      return IDNotFoundResponse("user", eventRequest.getCreatorId)
    }
    val event = eventRequest.getEvent
    events.addEvent(event)
    complete(StatusCodes.Created, event)
  }

  private def userExist(id: Int) = users.byID(id).isDefined

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
}
