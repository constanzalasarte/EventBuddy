package user

import element.UseElementRoute
import event.{EventJsonProtocol, Events, UseEventRoute}
import guest.{ConfirmationStatus, Guests, UseGuestRoute}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import routes.PrincipalRoute

import java.time.Instant
import java.util.Date
import scala.concurrent.Await

class UserRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with UserJsonProtocol with EventJsonProtocol{
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val guests = Guests(Set.empty)
  private val elements = PrincipalRoute.setUpElements(events, users)
  private val route = PrincipalRoute.combinedRoutes(users, events, guests, elements)

  private val guestRoute = UseGuestRoute(guests)
  private val eventRoute = UseEventRoute(events)
  private val elementRoute = UseElementRoute(elements)

  private val date = Date.from(Instant.now())

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

  "get users" in {
    Get("/user") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      val jsonString = responseAs[String]
      val json = Unmarshal(jsonString).to[Set[User]]
      val usersSet = Await.result(json, 1.second)
      usersSet shouldEqual users.getUsers
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
