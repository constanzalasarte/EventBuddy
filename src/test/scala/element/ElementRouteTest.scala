package element

import element.controller.Element
import element.controller.json.ElementJsonProtocol
import element.controller.json.input.{ElementPatchRequest, ElementRequest}
import event.{EventJsonProtocol, Events, UseEventRoute}
import guest.Guests
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import routes.PrincipalRoute
import user.{UseUserRoute, UserJsonProtocol, Users}

import java.time.Instant
import java.util.Date
import scala.concurrent.Await


class ElementRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with EventJsonProtocol with UserJsonProtocol with ElementJsonProtocol {
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val guests = PrincipalRoute.setUpGuests()
  private val elements = PrincipalRoute.setUpElements(events, users)
  private val route = PrincipalRoute.combinedRoutes(users, events, guests, elements)

  private val userRoute = UseUserRoute(users)
  private val eventRoute = UseEventRoute(events)

  private val date = Date.from(Instant.now())

  private val user = userRoute.createAUser("email", "name")
  private val event = eventRoute.createAEvent("event name", "event description", user.getId, date)

  "get no elements" in {
    Get("/element") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[]"
    }
  }

  "create element" in {
    val element = ElementRequest("name", 1, eventId = event.getId, maxUsers = 2, users = Set.empty)
    Post("/element", element) ~> route ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Element].getName shouldEqual "name"
      responseAs[Element].getEventId shouldEqual event.getId
      responseAs[Element].getMaxUsers shouldEqual 2
      responseAs[Element].getUsers shouldEqual Set.empty
      responseAs[Element].getId shouldEqual 1
    }
    val elementWDiffId = ElementRequest("name", 1, eventId = 100000, maxUsers = 2, users = Set.empty)
    Post("/element", elementWDiffId) ~> route ~> check {
//      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 100000"
    }
  }

  "get elements" in {
    Get("/element") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      val jsonString = responseAs[String]
      val json = Unmarshal(jsonString).to[Set[Element]]
      val elementsSet = Await.result(json, 1.second)
      elementsSet shouldEqual elements.getElements
      elementsSet.size shouldEqual 1
    }
  }

  "get element by id" in {
    Get("/element/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Element].getName shouldEqual "name"
      responseAs[Element].getEventId shouldEqual event.getId
      responseAs[Element].getMaxUsers shouldEqual 2
      responseAs[Element].getUsers shouldEqual Set.empty
      responseAs[Element].getId shouldEqual 1
    }
    Get("/element/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no element with id 2"
    }
    Get("/element/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "modify element by id" in {
    val elementPatch = ElementPatchRequest(name = Some("new name"))
    Put("/element/byId?id=1", elementPatch) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Element].getName shouldEqual "new name"
      responseAs[Element].getEventId shouldEqual event.getId
      responseAs[Element].getMaxUsers shouldEqual 2
      responseAs[Element].getUsers shouldEqual Set.empty
      responseAs[Element].getId shouldEqual 1
    }
    Put("/element/byId?id=2", elementPatch) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no element with id 2"
    }
    Put("/element/byId?id=hola", elementPatch) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }

  "update element by id with invalid arguments" in {
    val elementPatchWEventId = ElementPatchRequest(eventId = Some(-1))
    Put("/element/byId?id=1", elementPatchWEventId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id -1"
    }
    val elementPatchWUsersId = ElementPatchRequest(users = Some(Set(-1)))
    Put("/element/byId?id=1", elementPatchWUsersId) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id -1"
    }

    val elementPatchMaxUsers = ElementPatchRequest(maxUsers = Some(0), users = Some(Set(1)))
    Put("/element/byId?id=1", elementPatchMaxUsers) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Max users can not be greater than users size"
    }
  }

  "create element by id with invalid arguments" in {
    val element = ElementRequest("name", 1, eventId = event.getId, maxUsers = -1, users = Set.empty)
    Post("/element", element) ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Max users can not be greater than users size"
    }
    val elementWUsers = ElementRequest("name", 1, eventId = event.getId, maxUsers = 1, users = Set(-1))
    Post("/element", elementWUsers) ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no user with id -1"
    }
  }

  "delete element by id" in {
    Delete("/element/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "element deleted"
    }
    Delete("/element/byId?id=2") ~> route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no element with id 2"
    }
    Delete("/element/byId?id=hola") ~> route ~> check {
      status shouldEqual StatusCodes.NotAcceptable
      responseAs[String] shouldEqual "Int expected, received a no int type id"
    }
  }
}
