package user

import element.UseElementRoute
import event.UseEventRoute
import guest.UseGuestRoute
import modules.element.controller.Element
import modules.event.{Event, EventJsonProtocol}
import modules.guest.ConfirmationStatus
import modules.user.{User, UserJsonProtocol, UserPatchRequest, UserRequest}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import server.Server
import slick.jdbc.JdbcBackend.Database
import util.DBTables.userTable

import java.time.Instant
import java.util.Date
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.jdbc.PostgresProfile.api._

class UserDBTest extends AsyncWordSpec with Matchers with BeforeAndAfterEach with ScalatestRouteTest with UserJsonProtocol with EventJsonProtocol{
  private var users = Server.setUpUsers()
  private val events = Server.setUpEvents(users)
  private var guests = Server.setUpGuests(events, users)
  private var elements = Server.setUpElements(events, users)
  private var route = Server.combinedRoutes(users, events, guests, elements)

  private var guestRoute = UseGuestRoute(guests)
  private val eventRoute = UseEventRoute(events)
  private var elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

  var db: Database = _


  override protected def beforeEach(): Unit = {
    db = Database.forConfig("eventBuddy-db")
    Await.result(db.run(userTable.schema.create), Duration.Inf)
    users = Server.setUpUsersDB(db)
    guests = Server.setUpGuests(events, users)
    elements = Server.setUpElements(events, users)
    elementRoute = UseElementRoute(elements)
    guestRoute = UseGuestRoute(guests)
    route = Server.combinedRoutes(users, events, guests, elements)
  }

  override protected def afterEach(): Unit = {
    Await.result(db.run(userTable.schema.drop), Duration.Inf)
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
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
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
    val userReq = UserRequest("user@mail.com", "userName")
    Post("/user", userReq) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
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
    val userReq = UserRequest("user@mail.com", "userName")
    Post("/user", userReq) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val event = eventRoute.createAEvent("name", "description", 1, date)
    val event2 = eventRoute.createAEvent("name", "description", 2, date)
    val guest = guestRoute.createAGuest(1, event.getId, ConfirmationStatus.PENDING, isHost = false)
    val element = getElement(event, Set.empty)
    val elementOfUser = getElement(event2, Set(1))

    Delete("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "User deleted"
      guests.byId(guest.getId).isEmpty shouldEqual true
      getEventByID(event.getId).isEmpty shouldEqual true
      getElementById(element.getId).isEmpty shouldEqual true
      isUserInUsers(1, elementOfUser.getId) shouldEqual false
    }
    Delete("/user/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Delete("/user/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
    Get("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 1"
    }
  }

  private def getEventByID(eventId: Int) = {
    val event : Future[Option[Event]] = events.byId(eventId)
    Await.result(event, Duration.Inf)
  }

  private def getElement(event: Event, userSet: Set[Int]) = {
    val element = elementRoute.createAElement("element name", 1, event.getId, 1, userSet)
    Await.result(element, Duration.Inf)
  }

  private def getElementById(id: Int): Option[Element] = {
    val futureSet: Future[Option[Element]] = elements.byId(id)
    Await.result(futureSet, Duration.Inf)
  }
  private def isUserInUsers(userId: Int, elementId: Int): Boolean = {
    val boolean = elements.isUserInUsers(userId, elementId)
    Await.result(boolean, Duration.Inf)
  }
}
