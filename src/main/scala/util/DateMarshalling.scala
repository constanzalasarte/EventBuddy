package util

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

object DateMarshalling {
  implicit object DateJsonFormat extends JsonFormat[Date]{
    override def write(date: Date): JsValue = JsString(dateToIsoString(date))

    override def read(json: JsValue): Date = json match {
      case JsString(rawdate) =>
        val opt = parseIsoDateString(rawdate)
        if(opt.isEmpty) deserializationError(s"Expected ISO Date format, got $rawdate")
        opt.get
      case error => deserializationError(s"Expected JsString, got $error")
    }

    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
      override def initialValue(): SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    private def parseIsoDateString(date: String) =
      Try{ localIsoDateFormatter.get().parse(date) }.toOption

    private def dateToIsoString(date: Date) =
      localIsoDateFormatter.get().format(date)
  }
}
