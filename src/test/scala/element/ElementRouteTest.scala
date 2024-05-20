package element

import event.UseEventRoute
import modules.element.controller.Element
import modules.element.controller.json.ElementJsonProtocol
import modules.element.controller.json.input.{ElementPatchRequest, ElementRequest}
import modules.event.EventJsonProtocol
import modules.user.UserJsonProtocol
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import server.Server
import slick.jdbc.JdbcBackend.Database
import user.UseUserRoute
import util.DBTables.userTable

import java.time.Instant
import java.util.Date
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import slick.jdbc.PostgresProfile.api._


class ElementRouteTest extends AnyWordSpec with Matchers with BeforeAndAfterEach with ScalatestRouteTest with EventJsonProtocol with UserJsonProtocol with ElementJsonProtocol {
  private var users = Server.setUpUsers()
  private val events = Server.setUpEvents()
  private val guests = Server.setUpGuests(events, users)
  private val elements = Server.setUpElements(events, users)
  private var route = Server.combinedRoutes(users, events, guests, elements)

  private val userRoute = UseUserRoute(users)
  private val eventRoute = UseEventRoute(events)

  private val date = Date.from(Instant.now())

  private val user = userRoute.createAUser("email", "name")
  private val event = eventRoute.createAEvent("event name", "event description", user.getId, date)

  var db: Database = _

  override protected def beforeEach(): Unit = {
    db = Database.forConfig("eventBuddy-db")
    Await.result(db.run(userTable.schema.create), 2.seconds)
    users = Server.setUpUsersDB(db)
    route = Server.combinedRoutes(users, events, guests, elements)
  }

  override protected def afterEach(): Unit = {
    Await.result(db.run(userTable.schema.drop), 2.seconds)
    db.close
  }

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
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "There is no event with id 100000"
    }
  }

  "get elements" in {
    Get("/element") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      val jsonString = responseAs[String]
      val json = Unmarshal(jsonString).to[Set[Element]]
      val elementsSet = Await.result(json, 1.second)
      elementsSet shouldEqual getElementSet()
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
      responseAs[String] shouldEqual "Element deleted"
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

  private def getElementSet(): Set[Element] = {
    val futureSet: Future[Set[Element]] = elements.getElements()
    Await.result(futureSet, Duration.Inf)
  }
}
