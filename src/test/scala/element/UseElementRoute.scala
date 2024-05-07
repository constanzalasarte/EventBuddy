package element

import guest.ConfirmationStatus.ConfirmationStatus

case class UseElementRoute(elements: Elements) {
  def createAElement(
                      name: String,
                      quantity: Int,
                      eventId: Int,
                      maxUsers: Int,
                      users: Set[Int]): Element = {
    val elementRequest = ElementRequest(name, quantity, eventId, maxUsers, users)
    val element = elementRequest.getElement
    elements.addElement(element)
    element
  }
}
