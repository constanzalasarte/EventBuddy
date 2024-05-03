package event

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, post}
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
      get {
        complete(StatusCodes.OK, events.getEvents)
      },
      post {
        entity(as[EventRequest]) { eventRequest =>
          createEvent(eventRequest)
        }
      },
    )
}
