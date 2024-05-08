package event

import element.{ElementService, UseElementRoute}
import guest.{ConfirmationStatus, Guests, UseGuestRoute}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import routes.PrincipalRoute
import user.{User, UserJsonProtocol, UserRequest, Users}

import java.time.Instant
import java.util.Date
import scala.concurrent.Await


class EventRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with EventJsonProtocol with UserJsonProtocol{
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val guests = Guests(Set.empty)
  private val elements = PrincipalRoute.setUpElements(events, users)
  private val route = PrincipalRoute.combinedRoutes(users, events, guests, elements)

  private val guestRoute = UseGuestRoute(guests)
  private val elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

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
      eventsSet shouldEqual events.getEvents
    }
  }

  "get event1" in{
    Get("/event/byId") ~> route ~> check {
      responseAs[String] shouldEqual "/event/byId"
    }
  }

  "get event by id" in {
    Get("/event/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Event].getName shouldEqual "event name"
      responseAs[Event].getDescription shouldEqual "event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
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

  "modify user by id" in {
    val event = EventPatchRequest(Some("new event name"), Some("new event description"), None, None)

    Put("/event/byId?id=1", event) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Event].getName shouldEqual "new event name"
      responseAs[Event].getDescription shouldEqual "new event description"
      responseAs[Event].getCreatorId shouldEqual 1
      responseAs[Event].getDate shouldEqual date
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
    val guest = guestRoute.createAGuest(1, 1, ConfirmationStatus.PENDING, isHost = false)
    val element = elementRoute.createAElement("element name", 1, 1, 1, Set.empty)
    Delete("/event/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Event deleted"
      guests.byId(guest.getId).isEmpty shouldEqual true
      elements.byId(element.getId).isEmpty shouldEqual true
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
}
