package user

import event.Events
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import routes.PrincipalRoute

import scala.concurrent.Await

class UserRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with UserJsonProtocol{
  private val users = Users(Set.empty)
  private val events = Events(Set.empty)
  private val route = PrincipalRoute.combinedRoutes(users, events)

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
}
