package user

import element.service.CheckElements
import event.CheckEvents
import guest.CheckGuests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onSuccess, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}

case class UserRoutes(users: Users, events: CheckEvents, guests: CheckGuests, elements: CheckElements) extends UserJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def userRoute: Route =
    concat(
      path("byId"){
        userByIdRoute
      },
      pathEnd{
        concat(
          get {
            complete(StatusCodes.OK, users.getUsers)
          },
          post  {
            entity(as[UserRequest]) { userRequest =>
              val userSaved: Future[User] = createUser(userRequest)
              onSuccess(userSaved) { _ =>
                complete(StatusCodes.Created, userSaved)
              }
            }
          },
        )
      }
    )

  private def userByIdRoute = {
    concat(
      get {
        parameters("id") { id =>
          getUserById(id)
        }
      },
      put {
        parameters("id") { id =>
          entity(as[UserPatchRequest]) { userPatchRequest =>
            updateUserById(id, userPatchRequest)
          }
        }
      },
      delete {
        parameters("id") { id =>
        {
          deleteUser(id)
        }
        }
      }
    )
  }

  private def updateUserById(id: String, userPatch: UserPatchRequest) = {
    try {
      val optUser: Option[User] = checkUser(id.toInt)
      if (optUser.isEmpty) notFoundResponse(id)
      else {
        val user: User = updateUserVariables(userPatch, optUser)
        users.changeUser(id.toInt, user)
        complete(StatusCodes.OK, user)
      }
    }
    catch {
      case _: NumberFormatException =>
        IntExpectedResponse
    }
  }

  private def deleteUser(id: String) = {
    try {
      val user = checkUser(id.toInt)
      if(user.isEmpty) notFoundResponse(id)
      else{
        deleteGuestsEventsAndElements(id.toInt)
        users.deleteById(id.toInt)
        complete(StatusCodes.OK, s"User deleted")
      }
    }
    catch {
      case _: NumberFormatException =>
        IntExpectedResponse
    }
  }

  private def deleteGuestsEventsAndElements(id: Int): Unit = {
    guests.deleteByUserId(id)
    elements.deleteUserInUsers(id)
    val deletedEvents = events.deleteByCreatorId(id)
    guests.deleteByEvents(deletedEvents)
    elements.deleteInEvents(deletedEvents)
  }

  private def createUser(userRequest: UserRequest): Future[User] = {
    val user = userRequest.getUser
    users.addUser(user)
    Future { user }
  }

  private def getUserById(id: String) = {
    try {
      val user: Option[User] = checkUser(id.toInt)
      if (user.isEmpty) notFoundResponse(id)
      else
        complete(StatusCodes.OK, user.get)
    }
    catch {
      case _: NumberFormatException =>
        IntExpectedResponse
    }
  }

  private def checkUser(id: Int): Option[User] =
    users.byID(id)

  private def updateUserVariables(userPatch: UserPatchRequest, optUser: Option[User]) = {
    val user = optUser.get
    if (userPatch.hasUserName) user.changeUserName(userPatch.userName.get)
    if (userPatch.hasEmail) user.changeEmail(userPatch.email.get)
    user
  }

  private def IntExpectedResponse = {
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
  }

  private def notFoundResponse(id: String) = {
    complete(StatusCodes.NotFound, s"There is no user with id ${id.toInt}")
  }
}