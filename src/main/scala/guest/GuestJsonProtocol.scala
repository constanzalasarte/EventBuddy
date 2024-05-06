package guest

import guest.ConfirmationStatusMarshalling.ConfirmationStatusJsonFormat
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait GuestJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val guestFormat: RootJsonFormat[Guest] = jsonFormat5(Guest.apply)
  implicit val guestRequestFormat: RootJsonFormat[GuestRequest] = jsonFormat4(GuestRequest.apply)
  implicit val guestPatchRequestFormat: RootJsonFormat[GuestPatchRequest] = jsonFormat4(GuestPatchRequest.apply)
}
