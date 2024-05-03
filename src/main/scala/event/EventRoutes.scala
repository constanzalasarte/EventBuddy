package event

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, parameters, post}
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
      parameters("id") {id =>
        get{
          getEventById(id)
        }
      },
      get {
        complete(StatusCodes.OK, events.getEvents)
      },
      post {
        entity(as[EventRequest]) { eventRequest =>
          createEvent(eventRequest)
        }
      },
    )

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

  private def checkEvent(id: Int): Option[Event] =
    events.byId(id)
}
