package guest

import event.Events
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


class GuestRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with GuestJsonProtocol {
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val guests = Guests(Set.empty)
  private val route = PrincipalRoute.combinedRoutes(users, events, guests)

  "get no guests" in {
    Get("/guest") ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "[]"
    }
  }
}
