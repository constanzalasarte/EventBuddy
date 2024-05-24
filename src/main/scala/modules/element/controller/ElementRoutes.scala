package modules.element.controller

import modules.element.controller.json.ElementJsonProtocol
import modules.element.controller.json.input.{ElementPatchRequest, ElementRequest}
import modules.element.service.ElementService
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object ElementRouteFactory{
  def create(service: ElementService): ElementRoutes =
    ElementRoutes(service)
}
case class ElementRoutes(service: ElementService) extends ElementJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def elementRoute: Route =
    concat(
      pathEnd{
        concat(
          get {
            getElements
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

  private def getElements: Route = {
    val futureSet: Future[Set[Element]] = service.getElements
    onComplete(futureSet) {
      case Success(elemSet) => complete(StatusCodes.OK, elemSet)
      case Failure(_) => complete(StatusCodes.NoContent, "There is a failure getting the element set")
    }
  }

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
      val futureElement : Future[Element] = service.updateById(id.toInt, elementPatch)
      onComplete(futureElement) {
        case Success(element) => complete(StatusCodes.OK, element)
        case Failure(exception) => exception match{
          case _: NumberFormatException => intExpectedResponse
          case msg: IDNotFoundException =>
            complete(StatusCodes.UnprocessableEntity, msg.getMessage)
          case msg: UnacceptableException =>
            complete(StatusCodes.NotAcceptable, msg.getMessage)
        }
      }
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
      case msg: IDNotFoundException =>
        complete(StatusCodes.UnprocessableEntity, msg.getMessage)
      case msg: UnacceptableException =>
        complete(StatusCodes.NotAcceptable, msg.getMessage)
    }
  }

  private def getElementById(id: String): Route = {
    val inCaseElemExist = (optElem: Option[Element]) => {
      if (optElem.isEmpty) return elementIDNotFound(id.toInt)
      complete(StatusCodes.OK, optElem.get)
    }

    checkIfElementExist(id, inCaseElemExist)
  }

  private def deleteElementById(id: String): Route = {
    val inCaseElemExist = (_: Option[Element]) => {
      service.deleteById(id.toInt)
      complete(StatusCodes.OK, "Element deleted")
    }
    checkIfElementExist(id, inCaseElemExist)
  }

  private def createElement(elementRequest: ElementRequest): Route = {
    try {
      val element = service.addElement(elementRequest)
      onComplete(element) {
        case Success(element) => complete(StatusCodes.Created, element)
        case Failure(exception) => exception match{
          case _: NumberFormatException => intExpectedResponse
          case msg: IDNotFoundException =>
            complete(StatusCodes.UnprocessableEntity, msg.getMessage)
          case msg: UnacceptableException =>
            complete(StatusCodes.NotAcceptable, msg.getMessage)
        }
      }
    }
    catch {
      case msg: IDNotFoundException =>
        complete(StatusCodes.UnprocessableEntity, msg.getMessage)
      case msg: UnacceptableException =>
        complete(StatusCodes.NotAcceptable, msg.getMessage)
    }
  }

  private def elementIDNotFound(id: Int) =
    complete(StatusCodes.UnprocessableEntity, s"There is no element with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def checkIfElementExist(id: String, inCaseElemExist: Option[Element] => Route) = {
    try {
      val futureElem: Future[Option[Element]] = service.byId(id.toInt)
      onComplete(futureElem) {
        case Success(optElem) =>
          if (optElem.isEmpty) elementIDNotFound(id.toInt)
          else {
            inCaseElemExist(optElem)
          }
        case Failure(_) => elementIDNotFound(id.toInt)
      }
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }
}
