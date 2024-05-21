package modules.event

import modules.event.repository.{EventDBRepo, EventRepository, EventSetRepo}
import modules.user.CheckUsers
import server.Server.executionContext
import slick.jdbc.JdbcBackend.Database
import util.Version
import util.Version.DBVersion
import util.exceptions.IDNotFoundException

import scala.concurrent.Future

object EventServiceFactory{
  def createService(version: Version, userService: CheckUsers, db: Option[Database] = None): Events =
    version match {
      case Version.SetVersion => Events(EventSetRepo(Set.empty), userService)
      case DBVersion => Events(EventDBRepo(db.get), userService)
    }
}

case class Events(private var repository: EventRepository, private val userService: CheckUsers) extends CheckEvents {
  def addEvent(request: EventRequest): Future[Event] = {
    for{
      _ <- userExist(request.getCreatorId)
    } yield {
      val event = request.getEvent
      repository.addEvent(event)
      event
    }
  }

  def getEvents: Future[Set[Event]] = repository.getEvents

  override def byId(id: Int): Future[Option[Event]] = {
    repository.byId(id)
  }

  override def deleteById(id: Int): Future[Unit] = {
    for{
      deleted <- repository.deleteById(id)
    } yield {
      if (!deleted) throw IDNotFoundException("event", id)
    }
  }


  override def byCreatorId(id: Int): Future[Set[Event]] = {
    for{
      events <- getEvents
    } yield {
      events.filter(event => event.getCreatorId == id)
    }
  }

  override def deleteByCreatorId(id: Int): Future[Set[Event]] = {
    for{
      events <- getEvents
    } yield {
      val eventsCreator = events.filter(event => event.getCreatorId == id)
      eventsCreator.foreach(event => deleteById(event.getId))
      eventsCreator
    }
  }

  def changeEvent(id: Int, patchRequest: EventPatchRequest): Future[Event] = {
    for{
      event <- updateEvent(id, patchRequest)
    } yield {
      repository.changeEvent(id, event)
      event
    }
  }

  def updateEvent(id: Int, patchRequest: EventPatchRequest): Future[Event] = {
    for{
      _ <- checkPatchValues(patchRequest)
      optEvent <- byId(id)
    } yield {
      if(optEvent.isEmpty) throw IDNotFoundException("event", id)
      val event = updateEventVariables(patchRequest, optEvent)
      event
    }
  }

  private def checkPatchValues(patchRequest: EventPatchRequest): Future[Unit] = {
    if (patchRequest.hasCreatorId)
      for {
        _ <- userExist(patchRequest.creatorId.get)
      } yield {}
    else Future {}
  }

  private def userExist(id: Int): Future[Unit] = {
    for {
      user <- userService.byID(id)
    } yield{
      if(user.isEmpty) throw IDNotFoundException("user", id)
    }
  }
  private def updateEventVariables(eventPatch: EventPatchRequest, optEvent: Option[Event]): Event = {
    val event = optEvent.get
    if (eventPatch.hasName) event.changeName(eventPatch.name.get)
    if (eventPatch.hasDescription) event.changeDescription(eventPatch.description.get)
    if (eventPatch.hasCreatorId) event.changeCreatorId(eventPatch.creatorId.get)
    if (eventPatch.hasDate) event.changeDate(eventPatch.date.get)
    event
  }
}