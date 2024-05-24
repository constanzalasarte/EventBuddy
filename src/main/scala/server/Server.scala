package server

import modules.element.controller.Element
import modules.element.service.{CreateElementService, ElementService}
import modules.event.{CheckEvents, Event, EventServiceFactory, Events}
import modules.guest.{Guest, GuestServiceFactory, Guests}
import modules.user.{CheckUsers, User, UserServiceFactory, Users}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import routes.ServerRoutes
import slick.jdbc.JdbcBackend.Database
import util.DBTables
import util.DBTables.{close, dropSchema}
import util.Version.DBVersion

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn

object Server extends ServerRoutes {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val config = system.settings.config
  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  def setUpGuestsDB(eventService: CheckEvents, userService: CheckUsers, db: Database): Guests ={
    val guestFactory = GuestServiceFactory
    Guest.start()
    guestFactory.createService(DBVersion, userService, eventService, Some(db))
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
    var db = Database.forConfig("eventBuddy-db")
    val futureDB = DBTables.createSchema(db)
    db = Await.result(futureDB, Duration.Inf)
    val userService = setUpUsersDB(db)
    val eventService = setUpEventDB(db, userService)
    val guestService = setUpGuestsDB(eventService, userService, db)
    val elementService = setUpElementsDB(db, eventService, userService)
    val bindingFuture = Http().newServerAt(interface, port).bind(combinedRoutes(userService, eventService, guestService, elementService))
    println(s"Server online\nPress RETURN to stop...")
    var str = StdIn.readLine() // let it run until user presses return
    while(str != "\n"){
      str = StdIn.readLine() // let it run until user presses return
    }
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => {
        println("dropping schema...")
        dropSchema(db)
        println("closing db...")
        close(db)
        system.terminate()
      })
  }

  def main(args: Array[String]): Unit = {
    startRoutes()
  }

}
