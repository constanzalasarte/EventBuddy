package element

import modules.element.controller.Element
import modules.element.controller.json.input.ElementRequest
import modules.element.service.ElementService

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
