package event

import element.UseElementRoute
import guest.UseGuestRoute
import modules.element.controller.Element
import modules.event.{Event, EventJsonProtocol, EventPatchRequest, EventRequest}
import modules.guest.ConfirmationStatus
import modules.user.{User, UserJsonProtocol, UserRequest}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import server.Server
import slick.jdbc.JdbcBackend.Database
import util.DBTables.{createSchema, dropSchema}

import java.time.{Instant, LocalDate, ZoneId}
import java.util.Date
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class EventDBTest extends AnyWordSpec with Matchers with BeforeAndAfterEach with ScalatestRouteTest with EventJsonProtocol with UserJsonProtocol{
  private var users = Server.setUpUsers()
  private var events = Server.setUpEvents(users)
  private var guests = Server.setUpGuests(events, users)
  private var elements = Server.setUpElements(events, users)
  private var route = Server.combinedRoutes(users, events, guests, elements)

  private var guestRoute = UseGuestRoute(guests)
  private var elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(1.seconds)

  var db: Database = _

  override protected def beforeEach(): Unit = {
    db = createSchema()
    users = Server.setUpUsersDB(db)
    events = Server.setUpEventDB(db, users)
    guests = Server.setUpGuests(events, users)
    elements = Server.setUpElements(events, users)
    elementRoute = UseElementRoute(elements)
    guestRoute = UseGuestRoute(guests)
    route = Server.combinedRoutes(users, events, guests, elements)
  }

  override protected def afterEach(): Unit = {
    dropSchema(db)
  }

  "get no events" in {
    Get("/event") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[]"
    }
  }

  "create event" in {
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
    val eventWDiffId = EventRequest("event name", "event description", 2, date)
    Post("/event", eventWDiffId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
  }

  "get event" in {
    Get("/event") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      val jsonString = responseAs[String]
      val json = Unmarshal(jsonString).to[Set[Event]]
      val eventsSet = Await.result(json, 1.second)
      eventsSet shouldEqual getEvents
    }
  }

  private def getEvents = {
    val eventsFuture = events.getEvents
    Await.result(eventsFuture, Duration.Inf)
  }

  "get event1" in{
    Get("/event/byId") ~> route ~> check {
      responseAs[String] shouldEqual "/event/byId"
    }
  }

  "get event by id" in {
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
    Get("/event/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual getDate
      responseAs[Event].getId shouldEqual 1
    }
    Get("/event/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 2"
    }
    Get("/event/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "modify event by id" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val eventReq = EventRequest("event name", "event description", 1, date)
    Post("/event", eventReq) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val event = EventPatchRequest(Some("new event name"), Some("new event description"), None, None)

    Put("/event/byId?id=1", event) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Event].getName shouldEqual "new event name"
      responseAs[Event].getDescription shouldEqual "new event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual getDate
      responseAs[Event].getId shouldEqual 1
    }
    Put("/event/byId?id=2", event) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 2"
    }
    Put("/event/byId?id=hola", event) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }

    val eventWCreatorId = EventPatchRequest(None, None, Some(2), None)
    Put("/event/byId?id=1", eventWCreatorId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id 2"
    }
  }

  "delete event by id" in {
    val user = UserRequest("user@mail.com", "userName")
    Post("/user", user) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[User].getEmail shouldEqual "user@mail.com"
      responseAs[User].getUserName shouldEqual "userName"
      responseAs[User].getId shouldEqual 1
    }
    val eventReq = EventRequest("event name", "event description", 1, date)
    Post("/event", eventReq) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
      responseAs[Event].getId shouldEqual 1
    }

    val guest = guestRoute.createAGuest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    val element: Element = getElement
    Delete("/event/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Event deleted"
      guests.byId(guest.getId).isEmpty shouldEqual true
      val optElem = getElementById(element.getId)
      print(optElem)
      optElem.isEmpty shouldEqual true
    }
    Delete("/event/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 2"
    }
    Delete("/event/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  private def getElement = {
    val element = elementRoute.createAElement("element name", 1, 1, 1, Set.empty)
    Await.result(element, Duration.Inf)
  }

  private def getElementById(id: Int): Option[Element] = {
    val eventualMaybeElement: Future[Option[Element]] = elements.byId(id)
    Await.result(eventualMaybeElement, Duration.Inf)
  }

  private def getDate: Date = {
    val local = LocalDate.ofInstant(date.toInstant, ZoneId.systemDefault())
    Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant)
  }
}
