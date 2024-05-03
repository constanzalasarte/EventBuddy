package event

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import util.DataMarshalling.DateJsonFormat

trait EventJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat5(Event.apply)
  implicit val eventRequestFormat: RootJsonFormat[EventRequest] = jsonFormat4(EventRequest.apply)
}
