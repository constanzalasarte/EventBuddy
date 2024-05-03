package event

import org.apache.pekko.http.scaladsl.model.{DateTime, StatusCodes}
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
  private val route = PrincipalRoute.combinedRoutes(users, events)

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

  "delete event by id" in {
    Delete("/event/byId?id=1") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Event deleted"
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
