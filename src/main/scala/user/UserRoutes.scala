package user

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}

case class UserRoutes(users: Users) extends UserJsonProtocol {
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
          complete("todo")
        }
      },
    )
  }

  private def createUser(userRequest: UserRequest): Future[User] = {
    val user = userRequest.getUser
    users.addUser(user)
    Future { user }
  }

  private def getUserById(id: String) = {
    try {
      val user: Option[User] = checkUser(id.toInt)
      if (user.isEmpty) complete(StatusCodes.NotFound, s"There is no user with id ${id.toInt}")
      else
        complete(StatusCodes.OK, user.get)
    }
    catch {
      case _: NumberFormatException =>
        complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
    }
  }

  private def checkUser(id: Int): Option[User] =
    users.byID(id)

}