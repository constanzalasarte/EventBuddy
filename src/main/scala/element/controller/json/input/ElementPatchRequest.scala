package element.controller.json.input

case class ElementPatchRequest(
                                name: Option[String] = None,
                                quantity: Option[Int] = None,
                                eventId: Option[Int] = None,
                                maxUsers: Option[Int] = None,
                                users: Option[Set[Int]] = None
                              ){
  def hasName: Boolean = name.isDefined
  def hasQty: Boolean = quantity.isDefined
  def hasEventId: Boolean = eventId.isDefined
  def hasMaxUsers: Boolean = maxUsers.isDefined
  def hasUsers: Boolean = users.isDefined
}
