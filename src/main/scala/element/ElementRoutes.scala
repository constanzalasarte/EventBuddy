package element

import element.service.ElementService
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.concurrent.ExecutionContext

case class ElementRoutes(service: ElementService) extends ElementJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def elementRoute: Route =
    concat(
      pathEnd{
        concat(
          get {
            complete(StatusCodes.OK, service.getElements)
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
      val element = service.updateById(id.toInt, elementPatch)
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
      val element: Option[Element] = service.byId(id.toInt)
      if(element.isEmpty) return IDNotFoundResponse("element", id.toInt)
      complete(StatusCodes.OK, element.get)
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
    }
  }

  private def deleteElementById(id: String): Route = {
    try{
      val deleted: Boolean = service.deleteById(id.toInt)
      if(!deleted) return IDNotFoundResponse("element", id.toInt)
      complete(StatusCodes.OK, "element deleted")
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
    }
  }

  private def createElement(elementRequest: ElementRequest): Route = {
    try {
      val element = service.addElement(elementRequest)
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
