package server

import modules.element.controller.Element
import modules.element.service.{CreateElementService, ElementService}
import modules.event.{CheckEvents, Event, EventServiceFactory, Events}
import modules.guest.{GuestServiceFactory, Guests}
import modules.user.{CheckUsers, User, UserServiceFactory, Users}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import routes.ServerRoutes
import slick.jdbc.JdbcBackend.Database
import util.Version.{DBVersion, SetVersion}

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Server extends ServerRoutes {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val config = system.settings.config
  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  def setUpElements(eventService: CheckEvents, userService: Users): ElementService ={
    val createElem = CreateElementService
    createElem.createElementService(SetVersion, eventService, userService)
  }

  def setUpGuests(eventService: CheckEvents, userService: CheckUsers): Guests ={
    val guestFactory = GuestServiceFactory
    guestFactory.createService(SetVersion, userService, eventService)
  }

  def setUpUsers(): Users = {
    val userFactory = UserServiceFactory
    userFactory.createService(SetVersion)
  }

  def setUpEvents(userService: CheckUsers): Events = {
    val eventFactory = EventServiceFactory
    eventFactory.createService(SetVersion, userService)
  }

  def setUpUsersDB(db: Database): Users = {
    val userFactory = UserServiceFactory
    User.start()
    userFactory.createService(DBVersion, Some(db))
  }

  def setUpElementsDB(db: Database, eventService: CheckEvents, userService: Users): ElementService = {
    val elementFactory = CreateElementService
    Element.start()
    elementFactory.createElementService(DBVersion, eventService, userService, Some(db))
  }

  def setUpEventDB(db: Database, users: CheckUsers): Events = {
    val eventService = EventServiceFactory
    Event.start()
    eventService.createService(DBVersion, users, Some(db))
  }

  def startRoutes(): Unit = {
    val userService = setUpUsers()
    val eventService = setUpEvents(userService)
    val guestService = setUpGuests(eventService, userService)
    val elementService = setUpElements(eventService, userService)
    val bindingFuture = Http().newServerAt(interface, port).bind(combinedRoutes(userService, eventService, guestService, elementService))
    println(s"Server online\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  def main(args: Array[String]): Unit = {
    startRoutes()
  }

}
