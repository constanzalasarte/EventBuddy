package element
import element.repository.{ElementsRepository, ElementsSetRepo}
import event.{CheckEvents, Event}
import org.apache.pekko.http.scaladsl.server.Route
import user.CheckUsers
import util.exceptions.{IDNotFoundException, UnacceptableException}

object CreateElementService{
  def createElementService(version: Version, eventService: CheckEvents, userService: CheckUsers): ElementService = version match {
    case Version.SetVersion => ElementService(ElementsSetRepo(Set.empty), eventService, userService)
  }
}

case class ElementService(private val repository: ElementsRepository, private val eventService: CheckEvents, userService: CheckUsers) extends CheckElements{
  def addElement(elementRequest: ElementRequest): Element = {
    checkEvent(elementRequest.eventId)

    checkUsersAndMaxUsers(elementRequest.users, elementRequest.maxUsers)

    val element = elementRequest.getElement
    repository.addElement(element)
    element
  }

  private def checkEvent(eventId: Int): Unit =
    if (!eventExist(eventId)) throw IDNotFoundException("event", eventId)

  private def checkUsersAndMaxUsers(users: Set[Int], maxUsers: Int): Unit = {
    checkUsers(users)

    if (users.size > maxUsers)
      throw UnacceptableException(unacceptableMaxUsers)
  }

  private def checkUsers(users: Set[Int]): Unit = {
    val id: Option[Int] = idThatDoesntExist(users)
    if (id.isDefined) throw IDNotFoundException("user", id.get)
  }

  private def idThatDoesntExist(ids: Set[Int]) : Option[Int] =
    ids.find(id => userService.byID(id).isEmpty)

  private def eventExist(id: Int): Boolean = eventService.byId(id).isDefined

  private def unacceptableMaxUsers: String =
    "Max users can not be greater than users size"

  def getElements: Set[Element] =
    repository.getElements

  def updateById(id: Int, elemPatch: ElementPatchRequest): Element = {
    val maybeElement: Option[Element] = byId(id)
    if (maybeElement.isEmpty) throw IDNotFoundException("element", id)
    checkPatchValues(elemPatch, maybeElement)

    val element: Element = updateElement(elemPatch, maybeElement)
    repository.changeById(id, element)
    element
  }

  private def updateElement(elementPatch: ElementPatchRequest, maybeElement: Option[Element]) = {
    val element = maybeElement.get
    if (elementPatch.hasName) element.changeName(elementPatch.name.get)
    if (elementPatch.hasQty) element.changeQuantity(elementPatch.quantity.get)
    if (elementPatch.hasEventId) element.changeEventId(elementPatch.eventId.get)
    if (elementPatch.hasUsers) element.changeUsers(elementPatch.users.get)
    if (elementPatch.hasMaxUsers) element.changeMaxUsers(elementPatch.maxUsers.get)
    element
  }

  private def checkPatchValues(elementPatch: ElementPatchRequest, maybeElement: Option[Element]) = {
    if (elementPatch.hasEventId) {
      checkEvent(elementPatch.eventId.get)
    }
    if (elementPatch.hasUsers) {
      checkUsers(elementPatch.users.get)
      if (elementPatch.hasMaxUsers)
        checkUsersAndMaxUsers(elementPatch.users.get, elementPatch.maxUsers.get)
    }
    if (elementPatch.hasMaxUsers)
      if (checkUsersSize(elementPatch, maybeElement.get))
        throw UnacceptableException(unacceptableMaxUsers)
    None
  }
  private def checkUsersSize(elementPatch: ElementPatchRequest, element: Element) : Boolean =
    element.getUsers.size > elementPatch.maxUsers.get

  def isUserInUsers(idUser: Int, idElement: Int): Boolean = {
    byId(idElement)
      .exists(element => element.getUsers.contains(idUser))
  }

  override def byId(id: Int): Option[Element] =
    repository.byId(id)

  override def deleteById(id: Int): Boolean =
    repository.deleteById(id)

  override def deleteByEventId(id: Int): Unit = {
    val elements = repository.getElements
    for (elem <- elements) {
      if(elem.getEventId == id) repository.deleteById(id)
    }
  }

  override def deleteUserInUsers(id: Int): Unit =
    repository.getElements.foreach(elem => elem.deleteUserInUsers(id))

  override def deleteInEvents(deletedEvents: Set[Event]): Unit =
    deletedEvents.foreach(event => deleteById(event.getId))
}