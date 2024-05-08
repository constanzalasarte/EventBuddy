package element
import element.repository.{ElementsRepository, ElementsSetRepo}
import event.Event

object CreateElements{
  def createElements(version: Version): Elements = version match {
    case Version.SetVersion => Elements(ElementsSetRepo(Set.empty))
  }
}
case class Elements(private var repository: ElementsRepository) extends CheckElements{
  def addElement(element: Element): Unit =
    repository.addElement(element)

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