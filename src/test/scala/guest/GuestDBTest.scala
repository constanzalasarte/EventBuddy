package guest

import element.UseElementRoute
import modules.event.{Event, EventJsonProtocol, EventRequest}
import modules.guest._
import modules.user.{User, UserJsonProtocol, UserRequest}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import server.Server
import slick.jdbc.JdbcBackend.Database
import util.DBTables.{createSchema, dropSchema}

import java.time.Instant
import java.util.Date


class GuestDBTest extends AnyWordSpec with BeforeAndAfterEach with Matchers with ScalatestRouteTest with GuestJsonProtocol with UserJsonProtocol with EventJsonProtocol{
  private var users = Server.setUpUsers()
  private var events = Server.setUpEvents(users)
  private var guests = Server.setUpGuests(events, users)
  private var elements = Server.setUpElements(events, users)
  private var route = Server.combinedRoutes(users, events, guests, elements)

  private val date = Date.from(Instant.now())

  var db: Database = _

  override protected def beforeEach(): Unit = {
    db = createSchema()
    users = Server.setUpUsersDB(db)
    events = Server.setUpEventDB(db, users)
    guests = Server.setUpGuestsDB(events, users, db)
    elements = Server.setUpElements(events, users)
    route = Server.combinedRoutes(users, events, guests, elements)
  }

  override protected def afterEach(): Unit = {
    dropSchema(db)
  }
  "get no guests" in {
    Get("/guest") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[]"
    }
  }

  "create guest" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val event = EventRequest("event name", "event description", 1, date)
    Post("/event", event) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val guest = GuestRequest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    Post("/guest", guest) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.PENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }
  }

  "get guest by id" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val event = EventRequest("event name", "event description", 1, date)
    Post("/event", event) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val guest = GuestRequest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    Post("/guest", guest) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.PENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }


    Get("/guest/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.PENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }
    Get("/guest/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no guest with id 2"
    }
    Get("/guest/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "modify user by id" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val event = EventRequest("event name", "event description", 1, date)
    Post("/event", event) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val guestRequest = GuestRequest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    Post("/guest", guestRequest) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.PENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }

    val guest = GuestPatchRequest(None, None, Some(ConfirmationStatus.ATTENDING), None)

    Put("/guest/byId?id=1", guest) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.ATTENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }
    Put("/guest/byId?id=2", guest) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no guest with id 2"
    }
    Put("/guest/byId?id=hola", guest) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }

    val guestWUserId = GuestPatchRequest(Some(2), None, None, None)
    Put("/guest/byId?id=1", guestWUserId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }

    val guestWEventId = GuestPatchRequest(None, Some(2), None, None)
    Put("/guest/byId?id=1", guestWEventId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 2"
    }

    Get("/guest/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.ATTENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }
  }

  "delete guest by id" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val event = EventRequest("event name", "event description", 1, date)
    Post("/event", event) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val guest = GuestRequest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    Post("/guest", guest) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Guest].getUserId shouldEqual 1
      responseAs[Guest].getEventId shouldEqual 1
      responseAs[Guest].getConfirmationStatus shouldEqual ConfirmationStatus.PENDING
      responseAs[Guest].getIsHost shouldEqual false
      responseAs[Guest].getId shouldEqual 1
    }

    Delete("/guest/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Guest deleted"
    }
    Delete("/guest/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no guest with id 2"
    }
    Delete("/guest/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }
}
