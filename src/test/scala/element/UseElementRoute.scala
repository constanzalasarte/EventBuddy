package element

import element.controller.Element
import element.controller.json.input.ElementRequest
import element.service.ElementService

case class UseElementRoute(elements: ElementService) {
  def createAElement(
                      name: String,
                      quantity: Int,
                      eventId: Int,
                      maxUsers: Int,
                      users: Set[Int]): Element = {
    val elementRequest = ElementRequest(name, quantity, eventId, maxUsers, users)
    elements.addElement(elementRequest)
  }
}
