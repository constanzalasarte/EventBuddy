package modules.user

import modules.element.service.CheckElements
import modules.event.CheckEvents
import modules.guest.CheckGuests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{Segment, as, complete, concat, delete, entity, extractRequest, get, onComplete, onSuccess, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.{Route, StandardRoute}
import util.exceptions.IDNotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UserRoutes(users: Users, events: CheckEvents, guests: CheckGuests, elements: CheckElements) extends UserJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def userRoute: Route =
    concat(
      userByIdRoute,
      pathEnd{
        concat(
          get {
            getUsers
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
      (get & path(Segment)) { id => {
        getUserById(id)
        }
      },
      (put & path(Segment)) { id => {
        entity(as[UserPatchRequest]) { userPatchRequest =>
            updateUserById(id, userPatchRequest)
          }
        }
      },
      (delete & path(Segment)) { id => {
          deleteUser(id)
        }
      }
    )
  }

  private def getUsers: Route = {
    val futureSet: Future[Set[User]] = users.getUsers()
    onComplete(futureSet) {
      case Success(userSet) => complete(StatusCodes.OK, userSet)
      case Failure(_) => complete(StatusCodes.NoContent, "There is a failure getting the user set")
    }
  }

  private def updateUserById(id: String, userPatch: UserPatchRequest) = {
    val inCaseUserExist = (optUser: Option[User]) => {
      val user: User = updateUserVariables(userPatch, optUser)
      users.changeUser(id.toInt, user)
      complete(StatusCodes.OK, user)
    }
    checkIfUserExist(id, inCaseUserExist)
  }

  private def deleteUser(id: String): Route = {
    val inCaseUserExist = (_: Option[User]) => {
      val future = deleteGuestsEventsAndElements(id.toInt)
      onComplete(future) {
        case Success(_) => {
          val eventual = users.deleteById(id.toInt)
          onComplete(eventual){
            case Success(_) => complete(StatusCodes.OK, "User deleted")
          }
        }
        case Failure(exception) => exception match {
          case e: IDNotFoundException => complete(StatusCodes.UnprocessableEntity, e.getMessage)
        }
      }
    }
    checkIfUserExist(id, inCaseUserExist)
  }

  private def createUser(userRequest: UserRequest): Future[User] = {
    val user = userRequest.getUser
    users.addUser(user)
    Future { user }
  }

  private def getUserById(id: String): Route = {
    val inCaseUserExist = (user: Option[User]) => complete(StatusCodes.OK, user.get)

    checkIfUserExist(id, inCaseUserExist)
  }

  private def checkIfUserExist(id: String, inCaseUserExist: Option[User] => Route): Route = {
    try {
      val futureUser: Future[Option[User]] = checkUser(id.toInt)
      onComplete(futureUser) {
        case Success(optUser) => {
          if (optUser.isEmpty) notFoundResponse(id)
          else {
            inCaseUserExist(optUser)
          }
        }
        case Failure(_) => {
          notFoundResponse(id)
        }
      }
    }
    catch {
      case _: NumberFormatException => {
        IntExpectedResponse
      }
    }
  }


  private def deleteGuestsEventsAndElements(id: Int): Future[Unit] = {
    for{
      _ <- deleteGuestAndElements(id)
      _ <- deleteEvents(id)
    } yield {}
  }

  private def deleteEvents(id: Int): Future[Unit] =
    for {
      deletedEvents <- events.deleteByCreatorId(id)
    } yield {
      guests.deleteByEvents(deletedEvents)
      elements.deleteInEvents(deletedEvents)
    }

  private def deleteGuestAndElements(id: Int) = {
    for{
      _ <- elements.deleteUserInUsers(id)
      _ <- guests.deleteByUserId(id)
    } yield {}
  }

  private def checkUser(id: Int): Future[Option[User]] =
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
    complete(StatusCodes.UnprocessableEntity, s"There is no user with id ${id.toInt}")
  }
}