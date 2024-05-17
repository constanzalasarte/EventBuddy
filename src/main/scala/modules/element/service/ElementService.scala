package modules.element.service

import modules.element.controller.Element
import modules.element.controller.json.input.{ElementPatchRequest, ElementRequest}
import modules.element.repository.{ElementsRepository, ElementsSetRepo}
import modules.event.{CheckEvents, Event}
import modules.user.{CheckUsers, User}
import server.Server.executionContext
import util.Version
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.concurrent.Future
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
  override def byId(id: Int): Future[Option[Element]] =
    repository.byId(id)

  override def deleteById(id: Int): Future[Unit] =
    repository.deleteById(id)

  override def deleteByEventId(id: Int): Future[Unit] =
    for{
      elements <- repository.getElements
    } yield {
      elements.foreach(elem =>
        if(elem.getEventId == id) repository.deleteById(id)
      )
    }

  override def deleteUserInUsers(id: Int): Future[Unit] =
    for{
      elements <- repository.getElements
    } yield {
      elements.foreach(elem => elem.deleteUserInUsers(id))
    }


  override def deleteInEvents(deletedEvents: Set[Event]): Future[Unit] = {
    deletedEvents.foreach(event => deleteById(event.getId))
    Future {}
  }


  def addElement(elementRequest: ElementRequest): Element = {
    checkRequestValues(elementRequest)

    val element = elementRequest.getElement
    repository.addElement(element)
    element
  }

  def getElements(): Future[Set[Element]] =
    repository.getElements

  def updateById(id: Int, elemPatch: ElementPatchRequest): Future[Element] = {
    for {
      element <- getElement(id)
      _ <- checkPatchValues(elemPatch)
    } yield {
      checkPatchAndElementValues(elemPatch, element)

      updateElement(elemPatch, element)

      repository.changeById(id, element)

      element
    }
  }

  def isUserInUsers(idUser: Int, idElement: Int): Future[Boolean] = {
    for{
      element <- byId(idElement)
    } yield {
      if(element.isDefined) {
        if(element.get.getUsers.contains(idUser))
          return Future { true }
      }
      false
    }
  }

  private def getElement(id: Int) : Future[Element] = {
    for{
      maybeElem <- byId(id)
    } yield {
      if (maybeElem.isEmpty) throw IDNotFoundException("element", id)
      maybeElem.get
    }
  }

  private def checkRequestValues(elementRequest: ElementRequest): Unit = {
    checkEvent(elementRequest.eventId)

    checkUsersAndMaxUsers(elementRequest.users, elementRequest.maxUsers)
  }


  private def checkEvent(eventId: Int): Unit =
    if (!eventExist(eventId)) throw IDNotFoundException("event", eventId)

  private def checkUsersAndMaxUsers(users: Set[Int], maxUsers: Int): Unit = {
    checkUsers(users)

    if (users.size > maxUsers)
      throw UnacceptableException(unacceptableMaxUsers)
  }

  private def checkUser(id: Int) : Future[Option[User]] = {
    userService.byID(id)
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

  private def checkPatchValues(elementPatch: ElementPatchRequest): Future[Unit] = {
    if (elementPatch.hasEventId)
      checkEvent(elementPatch.eventId.get)
    if (elementPatch.hasUsers) {
      for{
        _ <- checkUsers(elementPatch.users.get)
      } yield{
        if (elementPatch.hasMaxUsers)
          checkUsersAndMaxUsers(elementPatch.users.get, elementPatch.maxUsers.get)
      }
    }
    else Future{}
  }
  private def checkUsers(users: Set[Int]): Future[Unit] = {
    for{
      id: Option[Int] <- idThatDoesntExist(users)
    } yield {
      if (id.isDefined) throw IDNotFoundException("user", id.get)
    }
  }

  private def idThatDoesntExist(ids: Set[Int]) : Future[Option[Int]] = {
    for {
      noUserIds: Option[Set[Int]] <- userService.noUserIds(ids)
    } yield{
      if(noUserIds.isEmpty) None
      else Some(noUserIds.get.head)
    }
  }

  private def checkPatchAndElementValues(elementPatch: ElementPatchRequest, element: Element): Unit = {
    if (elementPatch.hasMaxUsers)
      if (checkUsersSize(elementPatch, element))
        throw UnacceptableException(unacceptableMaxUsers)
  }
  private def checkUsersSize(elementPatch: ElementPatchRequest, element: Element) : Boolean =
    element.getUsers.size > elementPatch.maxUsers.get

  private def unacceptableMaxUsers: String =
    "Max users can not be greater than users size"
}