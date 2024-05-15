package modules.element.service

import modules.element.controller.Element
import modules.element.controller.json.input.{ElementPatchRequest, ElementRequest}
import modules.element.repository.{ElementsRepository, ElementsSetRepo}
import modules.event.{CheckEvents, Event}
import modules.user.{CheckUsers, User}
import server.Server.executionContext
import util.Version
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.util.{Failure, Success}

object CreateElementService{
  def createElementService(
                            version: Version,
                            eventService: CheckEvents,
                            userService: CheckUsers): ElementService =
    version match {
      case Version.SetVersion => ElementService(ElementsSetRepo(Set.empty), eventService, userService)
    }
}

case class ElementService(
                           private val repository: ElementsRepository,
                           private val eventService: CheckEvents,
                           private val userService: CheckUsers) extends CheckElements{
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

  def addElement(elementRequest: ElementRequest): Element = {
    checkRequestValues(elementRequest)

    val element = elementRequest.getElement
    repository.addElement(element)
    element
  }

  def getElements: Set[Element] =
    repository.getElements

  def updateById(id: Int, elemPatch: ElementPatchRequest): Element = {
    val element: Element = getElement(id)

    checkPatchValues(elemPatch, element)

    updateElement(elemPatch, element)

    repository.changeById(id, element)

    element
  }

  def isUserInUsers(idUser: Int, idElement: Int): Boolean = {
    byId(idElement)
      .exists(element => element.getUsers.contains(idUser))
  }

  private def getElement(id: Int) : Element = {
    val maybeElement: Option[Element] = byId(id)
    if (maybeElement.isEmpty) throw IDNotFoundException("modules/element", id)
    maybeElement.get
  }

  private def checkRequestValues(elementRequest: ElementRequest): Unit = {
    checkEvent(elementRequest.eventId)

    checkUsersAndMaxUsers(elementRequest.users, elementRequest.maxUsers)
  }


  private def checkEvent(eventId: Int): Unit =
    if (!eventExist(eventId)) throw IDNotFoundException("modules/event", eventId)

  private def checkUsersAndMaxUsers(users: Set[Int], maxUsers: Int): Unit = {
    checkUsers(users)

    if (users.size > maxUsers)
      throw UnacceptableException(unacceptableMaxUsers)
  }

  private def checkUsers(users: Set[Int]): Unit = {
    val id: Option[Int] = idThatDoesntExist(users)
    if (id.isDefined) throw IDNotFoundException("user", id.get)
  }

  private def idThatDoesntExist(ids: Set[Int]) : Option[Int] = {
    ids.foreach(id => {
      val result = checkUser(id)
      if(result.isDefined) return result
    })
    None
  }

  private def checkUser(id: Int) : Option[Int] = {
    val futureUser = userService.byID(id)
    futureUser.foreach { value: Option[User] =>
      if (value.isEmpty) return Some(id)
    }
    None
  }

  private def eventExist(id: Int): Boolean = eventService.byId(id).isDefined

  private def updateElement(elementPatch: ElementPatchRequest, element: Element) = {
    if (elementPatch.hasName) element.changeName(elementPatch.name.get)
    if (elementPatch.hasQty) element.changeQuantity(elementPatch.quantity.get)
    if (elementPatch.hasEventId) element.changeEventId(elementPatch.eventId.get)
    if (elementPatch.hasUsers) element.changeUsers(elementPatch.users.get)
    if (elementPatch.hasMaxUsers) element.changeMaxUsers(elementPatch.maxUsers.get)
    element
  }

  private def checkPatchValues(elementPatch: ElementPatchRequest, element: Element) = {
    if (elementPatch.hasEventId)
      checkEvent(elementPatch.eventId.get)
    if (elementPatch.hasUsers) {
      checkUsers(elementPatch.users.get)
      if (elementPatch.hasMaxUsers)
        checkUsersAndMaxUsers(elementPatch.users.get, elementPatch.maxUsers.get)
    }
    if (elementPatch.hasMaxUsers)
      if (checkUsersSize(elementPatch, element))
        throw UnacceptableException(unacceptableMaxUsers)
    None
  }
  private def checkUsersSize(elementPatch: ElementPatchRequest, element: Element) : Boolean =
    element.getUsers.size > elementPatch.maxUsers.get

  private def unacceptableMaxUsers: String =
    "Max users can not be greater than users size"
}