package routes

import element.Version.SetVersion
import element.service.{CreateElementService, ElementService}
import event.{CheckEvents, Events}
import guest.Guests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import user.Users

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object PrincipalRoute extends ServerRoutes {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val config = system.settings.config
  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  def setUpElements(eventService: CheckEvents, userService: Users): ElementService ={
    val createElem = CreateElementService
    createElem.createElementService(SetVersion, eventService, userService)
  }

  def startRoutes(): Unit = {
    val userService = Users(Set.empty)
    val eventService = Events(Set.empty)
    val guestService = Guests(Set.empty)
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
