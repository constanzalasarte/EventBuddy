package element

import modules.element.controller.Element
import modules.element.controller.json.input.ElementRequest
import modules.element.service.ElementService
import server.Server.executionContext

import scala.concurrent.Future

case class UseElementRoute(elements: ElementService) {
  def createAElement(
                      name: String,
                      quantity: Int,
                      eventId: Int,
                      maxUsers: Int,
                      users: Set[Int]): Future[Element] = {
    val elementRequest = ElementRequest(name, quantity, eventId, maxUsers, users)
    for {
      element <- elements.addElement(elementRequest)
    } yield{
      element
    }
  }
}
