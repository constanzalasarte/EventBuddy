package modules.element.controller.json

import modules.element.controller.Element
import modules.element.controller.json.input.{ElementPatchRequest, ElementRequest}
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ElementJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val elementFormat: RootJsonFormat[Element] = jsonFormat6(Element.apply)
  implicit val elementRequestFormat: RootJsonFormat[ElementRequest] = jsonFormat5(ElementRequest.apply)
  implicit val elementPatchRequestFormat: RootJsonFormat[ElementPatchRequest] = jsonFormat5(ElementPatchRequest.apply)
}
