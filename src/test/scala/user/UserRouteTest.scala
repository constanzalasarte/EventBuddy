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
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import server.Server

import java.time.Instant
import java.util.Date
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

class UserRouteTest extends AsyncWordSpec with Matchers with ScalatestRouteTest with UserJsonProtocol with EventJsonProtocol{
  private val users = Server.setUpUsers()
  private val events = Server.setUpEvents()
  private val guests = Server.setUpGuests(events, users)
  private val elements = Server.setUpElements(events, users)
  private val route = Server.combinedRoutes(users, events, guests, elements)

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
      val usersSet = parseUsers(jsonString)
      usersSet shouldEqual getUserSet()
    }
  }

  private def getElement(id: Int): Option[Element] = {
    val futureSet: Future[Option[Element]] = elements.byId(id)
    Await.result(futureSet, Duration.Inf)
  }

  private def parseUsers(jsonString: String) = {
    val json = Unmarshal(jsonString).to[Set[User]]
    val usersSet = Await.result(json, 1.second)
    usersSet
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
    val element = getElement(event, Set.empty)
    val elementOfUser = getElement(event2, Set(1))

    Delete("/user/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "User deleted"
      guests.byId(guest.getId).isEmpty shouldEqual true
      events.byId(event.getId).isEmpty shouldEqual true
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

  private def isUserInUsers(userId: Int, elementId: Int): Boolean = {
    val boolean = elements.isUserInUsers(userId, elementId)
    Await.result(boolean, Duration.Inf)
  }

  private def getUserSet(): Set[User] = {
    val futureSet: Future[Set[User]] = users.getUsers()
    Await.result(futureSet, Duration.Inf)
  }
  private def getElement(event: Event, userSet: Set[Int]) = {
    val element = elementRoute.createAElement("element name", 1, event.getId, 1, userSet)
    Await.result(element, Duration.Inf)
  }
  private def getElementById(id: Int): Option[Element] = {
    val futureSet: Future[Option[Element]] = elements.byId(id)
    Await.result(futureSet, Duration.Inf)
  }

}
