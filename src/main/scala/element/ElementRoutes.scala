package element

import element.service.ElementService
import event.CheckEvents
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import user.CheckUsers
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.concurrent.ExecutionContext

case class ElementRoutes(elements: ElementService, events: CheckEvents, users: CheckUsers) extends ElementJsonProtocol {
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
      path("byId") {
        getByIdRoute
      },
    )

  private def getByIdRoute: Route =
    concat(
      get {
        parameters("id") { id => {
          getElementById(id)
        }}
      },
      delete {
        parameters("id") { id => {
          deleteElementById(id)
        }}
      },
      put {
        parameters("id") { id => {
          entity(as[ElementPatchRequest]) { elementPatchRequest =>
            updateElementById(id, elementPatchRequest)
          }
        }}
      },
    )

  private def updateElementById(id: String, elementPatch: ElementPatchRequest): Route = {
    try{
      val element = elements.updateById(id.toInt, elementPatch)
      complete(StatusCodes.OK, element)
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
      case msg: IDNotFoundException =>
        complete(StatusCodes.NotFound, msg.getMessage)
      case msg: UnacceptableException =>
        complete(StatusCodes.NotAcceptable, msg.getMessage)
    }
  }

  private def getElementById(id: String): Route = {
    try{
      val element: Option[Element] = elements.byId(id.toInt)
      if(element.isEmpty) return IDNotFoundResponse("element", id.toInt)
      complete(StatusCodes.OK, element.get)
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
    }
  }

  private def deleteElementById(id: String): Route = {
    try{
      val deleted: Boolean = elements.deleteById(id.toInt)
      if(!deleted) return IDNotFoundResponse("element", id.toInt)
      complete(StatusCodes.OK, "element deleted")
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
    }
  }

  private def createElement(elementRequest: ElementRequest): Route = {
    try {
      val element = elements.addElement(elementRequest)
      complete(StatusCodes.Created, element)
    }
    catch {
      case msg: IDNotFoundException =>
        complete(StatusCodes.NotFound, msg.getMessage)
      case msg: UnacceptableException =>
        complete(StatusCodes.NotAcceptable, msg.getMessage)
    }
  }

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
}
