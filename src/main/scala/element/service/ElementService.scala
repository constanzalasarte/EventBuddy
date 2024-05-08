package element
import element.repository.{ElementsRepository, ElementsSetRepo}
import event.{CheckEvents, Event, Events}
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

    checkUsersAndMaxUsers(elementRequest)

    val element = elementRequest.getElement
    repository.addElement(element)
    element
  }

  private def checkEvent(eventId: Int): Unit =
    if (!eventExist(eventId)) throw IDNotFoundException("event", eventId)

  private def checkUsersAndMaxUsers(elementRequest: ElementRequest): Unit = {
    checkUsers(elementRequest.users)

    if (elementRequest.users.size > elementRequest.maxUsers)
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

  def changeById(id: Int, newElem: Element): Unit =
    repository.changeById(id, newElem)

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