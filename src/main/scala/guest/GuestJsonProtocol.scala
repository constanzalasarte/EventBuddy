package guest

import guest.ConfirmationStatusMarshalling.ConfirmationStatusJsonFormat
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait GuestJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val eventFormat: RootJsonFormat[Guest] = jsonFormat5(Guest.apply)
}
