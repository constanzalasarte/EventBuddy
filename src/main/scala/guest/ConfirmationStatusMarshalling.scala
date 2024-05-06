package guest

import guest.ConfirmationStatus.ConfirmationStatus
import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

object ConfirmationStatusMarshalling {
  implicit object ConfirmationStatusJsonFormat extends JsonFormat[ConfirmationStatus] {
    override def write(confirmationStatus: ConfirmationStatus): JsValue =
      JsString(confirmationStatus.toString)

    override def read(json: JsValue): ConfirmationStatus = json match {
      case JsString(rawdate) =>
        val confirmationStatus = rawdate.toString
        confirmationStatus match {
          case "PENDING" => ConfirmationStatus.PENDING
          case "ATTENDING" => ConfirmationStatus.ATTENDING
          case "NOT_ATTENDING" => ConfirmationStatus.NOT_ATTENDING
        }

      case error => deserializationError(s"Expected JsString, got $error")
    }
  }
}
