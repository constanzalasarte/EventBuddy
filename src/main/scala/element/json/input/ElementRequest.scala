package element.json.input

import element.Element

case class ElementRequest(
                           name: String,
                           quantity: Int,
                           eventId: Int,
                           maxUsers: Int,
                           users: Set[Int])
{
  def getElement: Element = Element(name, quantity, eventId, maxUsers, users)
}

