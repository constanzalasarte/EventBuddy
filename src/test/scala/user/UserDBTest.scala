package user

import element.UseElementRoute
import event.UseEventRoute
import guest.UseGuestRoute
import modules.event.EventJsonProtocol
import modules.guest.ConfirmationStatus
import modules.user.{User, UserJsonProtocol, UserPatchRequest, UserRequest}
import modules.user.repository.UserTable
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import server.Server
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.time.Instant
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UserDBTest extends AsyncWordSpec with Matchers with BeforeAndAfterEach with ScalatestRouteTest with UserJsonProtocol with EventJsonProtocol{
  private val users = Server.setUpUsers()
  private val events = Server.setUpEvents()
  private val guests = Server.setUpGuests(events, users)
  private val elements = Server.setUpElements(events, users)
  private val route = Server.combinedRoutes(users, events, guests, elements)

  private val guestRoute = UseGuestRoute(guests)
  private val eventRoute = UseEventRoute(events)
  private val elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

  var db: Database = Database.forConfig("eventBuddy-db")

  val userTable = TableQuery[UserTable]

  override protected def beforeEach(): Unit = {
    db = Database.forConfig("eventBuddy-db")
    Await.result(db.run(userTable.schema.create), 2.seconds)
  }

  override protected def afterEach(): Unit = {
    Await.result(db.run(userTable.schema.drop), 2.seconds)
    db.close
  }

  "get no users" in {
    Get("/user") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[]"
    }
  }

  "create user" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
  }

  "get user by id" in {
    Get("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    Get("/user/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Get("/user/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "modify user by id" in {
    val user = UserPatchRequest(Some("changedEmail@mail.com"), None)

    Put("/user/byId?id=1", user) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User].getEmail shouldEqual "changedEmail@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    Put("/user/byId?id=2", user) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Put("/user/byId?id=hola", user) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "delete user by id" in {
    val event = eventRoute.createAEvent("name", "description", 1, date)
    val event2 = eventRoute.createAEvent("name", "description", 2, date)
    val guest = guestRoute.createAGuest(1, event.getId, ConfirmationStatus.PENDING, isHost = false)
    val element = elementRoute.createAElement("element name", 1, event.getId, 1, Set.empty)
    val elementOfUser = elementRoute.createAElement("element name", 1, event2.getId, 1, Set(1))

    Delete("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "User deleted"
      guests.byId(guest.getId).isEmpty shouldEqual true
      events.byId(event.getId).isEmpty shouldEqual true
      elements.byId(element.getId).isEmpty shouldEqual true
      elements.isUserInUsers(1, elementOfUser.getId) shouldEqual false
    }
    Delete("/user/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Delete("/user/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }
}