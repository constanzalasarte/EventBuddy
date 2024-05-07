package element

import event.CheckEvents
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.{Route, StandardRoute}
import user.CheckUsers

import scala.concurrent.ExecutionContext

case class ElementRoutes(elements: Elements, events: CheckEvents, users: CheckUsers) extends ElementJsonProtocol {
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
      val maybeElement: Option[Element] = checkElement(id.toInt)
      if(maybeElement.isEmpty) return IDNotFoundResponse("element", id.toInt)
      checkPatchValues(elementPatch, maybeElement) match {
        case Some(toReturn) => return toReturn
        case None =>
      }

      val element: Element = updateElement(elementPatch, maybeElement)
      elements.changeById(id.toInt, element)
      complete(StatusCodes.OK, element)
    }
    catch {
      case _: NumberFormatException => intExpectedResponse
    }
  }

  private def checkPatchValues(elementPatch: ElementPatchRequest, maybeElement: Option[Element]): Option[Route] = {
    if (elementPatch.hasEventId) {
      if (!eventExist(elementPatch.eventId.get))
        return Some(IDNotFoundResponse("event", elementPatch.eventId.get))
    }
    if (elementPatch.hasUsers) {
      checkUsers(elementPatch.users.get) match {
        case Some(toReturn) => return Some(toReturn)
        case None =>
      }
    }
    if (elementPatch.hasMaxUsers) {
      if (checkUsersPatchSize(elementPatch) || checkUsersSize(elementPatch, maybeElement.get))
        return Some(unacceptableMaxUsers)
    }
    None
  }

  private def unacceptableMaxUsers = {
    complete(StatusCodes.NotAcceptable, "Max users can not be greater than users size")
  }

  private def updateElement(elementPatch: ElementPatchRequest, maybeElement: Option[Element]) = {
    val element = maybeElement.get
    if (elementPatch.hasName) element.changeName(elementPatch.name.get)
    if (elementPatch.hasQty) element.changeQuantity(elementPatch.quantity.get)
    if (elementPatch.hasEventId) element.changeEventId(elementPatch.eventId.get)
    if (elementPatch.hasUsers) element.changeUsers(elementPatch.users.get)
    if (elementPatch.hasMaxUsers) element.changeMaxUsers(elementPatch.maxUsers.get)
    element
  }

  private def getElementById(id: String): Route = {
    try{
      val element: Option[Element] = checkElement(id.toInt)
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
    if(!eventExist(elementRequest.eventId)) {
      return IDNotFoundResponse("event", elementRequest.eventId)
    }

    checkUsersAndMaxUsers(elementRequest) match {
      case Some(toReturn) => return toReturn
      case None =>
    }

    val element = elementRequest.getElement
    elements.addElement(element)
    complete(StatusCodes.Created, element)
  }

  private def checkUsersAndMaxUsers(elementRequest: ElementRequest): Option[Route] = {
    checkUsers(elementRequest.users) match {
      case Some(toReturn) => return Some(toReturn)
      case None =>
    }

    if (elementRequest.users.size > elementRequest.maxUsers)
      return Some(unacceptableMaxUsers)
    None
  }

  private def checkUsers(users: Set[Int]): Option[Route] = {
    val id: Option[Int] = usersExist(users)
    if (id.isDefined) return Some(IDNotFoundResponse("user", id.get))
    None
  }

  private def checkElement(id: Int) : Option[Element] =
    elements.byId(id)

  private def eventExist(id: Int) = events.byId(id).isDefined

  private def usersExist(ids: Set[Int]) : Option[Int] = {
    for(id <- ids)
      if(users.byID(id).isEmpty) return Some(id)
    None
  }
  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.NotFound, s"There is no $name with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")

  private def checkUsersPatchSize(elementPatch: ElementPatchRequest) =
    elementPatch.hasUsers && checkUsersSizeIsLessThanMaxUser(elementPatch)

  private def checkUsersSizeIsLessThanMaxUser(elementPatch: ElementPatchRequest) =
    elementPatch.users.get.size > elementPatch.maxUsers.get

  private def checkUsersSize(elementPatch: ElementPatchRequest, element: Element) : Boolean =
    element.getUsers.size > elementPatch.maxUsers.get
}
