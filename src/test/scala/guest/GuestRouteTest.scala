package guest

import event.{Event, EventJsonProtocol, EventRequest, Events}
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


class GuestRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with GuestJsonProtocol with UserJsonProtocol with EventJsonProtocol{
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val guests = Guests(Set.empty)
  private val route = PrincipalRoute.combinedRoutes(users, events, guests)

  private val date = Date.from(Instant.now())

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
}
