package user

import element.UseElementRoute
import event.UseEventRoute
import guest.UseGuestRoute
import modules.element.controller.Element
import modules.event.{Event, EventJsonProtocol}
import modules.guest.{ConfirmationStatus, Guest}
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
import testing.{DBLifecycle, H2Capabilities}

class UserDBTest extends AsyncWordSpec
  with Matchers
  with BeforeAndAfterEach
  with ScalatestRouteTest
  with UserJsonProtocol
  with EventJsonProtocol
  with H2Capabilities
  with DBLifecycle {
  private var users = Server.setUpUsersDB(db)
  private var events = Server.setUpEventDB(db, users)
  private var guests = Server.setUpGuestsDB(events, users, db)
  private var elements = Server.setUpElementsDB(db, events, users)
  private var route = Server.combinedRoutes(users, events, guests, elements)

  private var eventRoute = UseEventRoute(events)
  private var guestRoute = UseGuestRoute(guests)
  private var elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

  override protected def beforeEach(): Unit = {
    users = Server.setUpUsersDB(db)
    events = Server.setUpEventDB(db, users)
    guests = Server.setUpGuestsDB(events, users, db)
    elements = Server.setUpElementsDB(db, events, users)

    route = Server.combinedRoutes(users, events, guests, elements)

    eventRoute = UseEventRoute(events)
    elementRoute = UseElementRoute(elements)
    guestRoute = UseGuestRoute(guests)
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
    Get("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    Get("/user/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
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
      status shouldEqual StatusCodes.UnprocessableEntity
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Put("/user/byId?id=hola", user) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "delete user by id" in {
    val event = eventRoute.createAEvent("name", "description", 1, date)
    val guest = guestRoute.createAGuest(1, event.getId, ConfirmationStatus.PENDING, isHost = false)
    val element = getElement(event, Set.empty)

    Delete("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "User deleted"
      getGuestById(guest.getId).isEmpty shouldEqual true
      getEventByID(event.getId).isEmpty shouldEqual true
      getElementById(element.getId).isEmpty shouldEqual true
    }
    Delete("/user/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
      responseAs[String] shouldEqual "There is no user with id 2"
    }
    Delete("/user/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
    Get("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.UnprocessableEntity
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
  private def getGuestById(id: Int): Option[Guest] = {
    val guest = guests.byId(id)
    Await.result(guest, Duration.Inf)
  }
}
