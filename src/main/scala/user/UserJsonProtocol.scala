package user

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
  implicit val userRequestFormat: RootJsonFormat[UserRequest] = jsonFormat2(UserRequest.apply)
}

