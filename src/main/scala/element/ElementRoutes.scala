package element

import event.CheckEvents
import guest.CheckGuests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import user.CheckUsers

import scala.concurrent.ExecutionContext

case class ElementRoutes(elements: Elements, events: CheckEvents) extends ElementJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def elementRoute: Route =
    concat(
      pathEnd{
        concat(
          get {
            complete(StatusCodes.OK, elements.getElements)
          },
          post {
            entity(as[ElementRequest]) { elementRequest =>
              createElement(elementRequest)
            }
          },
        )
      },
//      path("byId") {
//        getByIdRoute
//      },
    )

  private def createElement(elementRequest: ElementRequest): Route = {
    if(!userExist(elementRequest.eventId)) {
      return IDNotFoundResponse("event", elementRequest.eventId)
    }
    val element = elementRequest.getElement
    elements.addElement(element)
    complete(StatusCodes.Created, element)
  }

  private def userExist(id: Int) = events.byId(id).isDefined

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
}
